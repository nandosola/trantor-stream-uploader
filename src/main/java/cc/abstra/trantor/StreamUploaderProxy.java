/*
 * Copyright abstra.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.abstra.trantor;

import cc.abstra.trantor.asynctasks.AddNewVersion;
import cc.abstra.trantor.asynctasks.AddToPendingDocs;
import cc.abstra.trantor.asynctasks.ArchiveTempDoc;
import cc.abstra.trantor.asynctasks.TrantorAsyncListener;
import cc.abstra.trantor.exceptions.EvilHeaderException;
import cc.abstra.trantor.wcamp.WcampDocUploadedFromAPI;
import cc.abstra.trantor.wcamp.WcampDocUploadedFromWeb;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;
import cc.abstra.trantor.wcamp.CustomHttpHeaders;
import cc.abstra.trantor.wcamp.WcampDocumentResource;
import cc.abstra.trantor.wcamp.exceptions.DocumentNotFoundException;
import cc.abstra.trantor.wcamp.exceptions.WcampNotAuthorizedException;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class StreamUploaderProxy extends HttpServlet implements JsonErrorResponses {

    static final int ASYNC_EXECUTOR_THREAD_POOL_SIZE = 10;
    static final int DEFAULT_CHUNK_SIZE = 1024;
    static final int FIVE_MIN = 300000;  //msec

    private static ScheduledThreadPoolExecutor executor =
            new ScheduledThreadPoolExecutor(ASYNC_EXECUTOR_THREAD_POOL_SIZE);

    private URL targetUrl = null;
    private String testUrl;  //injected from Mockito test.
                             // note: Servlets may not have parametrized constructors
    private boolean containerExists = false;  // again, used for testing

    @Override
    public String getServletInfo() {
        return "A proxy Servlet that streams file uploads to the Trantor backend file server.";
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        if (null != servletConfig.getServletContext()){
            containerExists = true;
        }

        try {
            this.targetUrl = new URL(servletConfig.getInitParameter("targetUri"));
        } catch (Exception e) {
            throw new RuntimeException("Trying to process targetUri init parameter: "+e,e);
        }
    }

    @Override
    public void destroy(){
        executor.shutdownNow();
    }

    @Override
    public void log(String msg){
        if(containerExists) {  // use container's logging facility
            super.log(msg);
        } else {
            System.out.println(msg);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        WcampDocumentResource wcampDocument = null;

        if (!containerExists && null == targetUrl){ // testing environment
            try {
                this.targetUrl = new URL(testUrl);
            } catch (MalformedURLException e) {
                log("Injected targetUrl from tests is malformed!");
            }
        }
        log("POST " + req.getRequestURI() + " --> " + targetUrl.toString());

        URLConnection targetConnection = null;
        try {

            String documentCode =  req.getHeader(CustomHttpHeaders.X_TRANTOR_DOCUMENT_CODE);  // case-sensitive

            if(null != req.getHeader(CustomHttpHeaders.X_TRANTOR_CLIENT_ID)){
                String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
                String clientId = req.getHeader(CustomHttpHeaders.X_TRANTOR_CLIENT_ID);
                String trantorFileId = req.getHeader(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID);
                wcampDocument = new WcampDocUploadedFromAPI(auth, clientId, trantorFileId, documentCode);
                log("Received POST request from API. Client id: "+ clientId+" Authorization: "+
                        wcampDocument.getAuthToken());
            } else {
                wcampDocument = new WcampDocUploadedFromWeb(req.getHeader(HttpHeaders.COOKIE), documentCode);
                //TODO check origin and raise 403 if not from WCAMP??
                log("Received POST request from web session" + "Cookie: " + wcampDocument.getAuthToken());
            }

            wcampDocument.authorize();
            log("Authorized "+wcampDocument.getNeededPerm()+" for session "+wcampDocument.getAuthToken());

            String uploadType = req.getHeader(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE);
            if (null != uploadType) {
                wcampDocument.setUploadType(uploadType.toLowerCase());
            }

            targetConnection = targetUrl.openConnection();
            copyRequestHeaders(req, targetConnection);
            //  Note: These headers below would've been sent by default:
            // {
            //   "User-Agent": "Java/1.a.b_cd",
            //   "Host": targetUrl.host() & .port(),
            //   "Accept": "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2",
            //   "Connection": "keep-alive",
            //   "Version": "HTTP/1.1",
            //   "Origin": null
            // }
            //
            // Note: If you need to rewrite headers, please see:
            //   http://stackoverflow.com/questions/7648872

            setStreamingMode(req.getContentLength(), targetConnection);
            targetConnection.setDoOutput(true);  //implicitly sets the request method to POST
            targetConnection.setReadTimeout(FIVE_MIN);

            try (InputStream clientRequestIS = req.getInputStream()){
                OutputStream targetRequestOS = targetConnection.getOutputStream();
                byte[] buffer = new byte[DEFAULT_CHUNK_SIZE]; // Uses only 1KB of memory
                for (int length = 0; (length = clientRequestIS.read(buffer)) > 0;) {
                    targetRequestOS.write(buffer, 0, length);
                    targetRequestOS.flush();
                }
            }

            try (InputStream targetResponseIS = targetConnection.getInputStream()) {
                byte[] outBytes = extractByteArray(targetResponseIS);  // note: byte[] holds max 2 GB (Java default)

                copyResponseHeaders(targetConnection, res, outBytes.length);

                String trantorUploadedFilesInfo = targetConnection.
                        getHeaderField(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO);

                AsyncContext ac = req.startAsync();
                ac.addListener(new TrantorAsyncListener());

                if (WcampDocumentResource.VERSION.equals(wcampDocument.getUploadType())){
                    executor.execute(new AddNewVersion(ac, wcampDocument, trantorUploadedFilesInfo));
                } else {
                    if (wcampDocument instanceof WcampDocUploadedFromAPI){
                        executor.execute(new ArchiveTempDoc(ac, (WcampDocUploadedFromAPI)wcampDocument,
                                trantorUploadedFilesInfo));
                    } else {
                        executor.execute(new AddToPendingDocs(ac, (WcampDocUploadedFromWeb)wcampDocument,
                                trantorUploadedFilesInfo));
                    }
                }

                res.getOutputStream().write(outBytes);
            }
        } catch (UnsupportedOperationException e) {
            String message = e.getMessage();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_FORBIDDEN, message);
        } catch (EvilHeaderException e) {  // PowerMockito does not handle multicatches well
            String message = e.getMessage();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_FORBIDDEN, message);
        } catch (MissingClientHeadersException e) {
            String message = e.getMessage();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_PRECONDITION_FAILED, message);
        } catch (DocumentNotFoundException e) {
            String message = e.getMessage();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_NOT_FOUND, message);
        } catch (WcampNotAuthorizedException e){
            String message = wcampDocument.getAuthToken()+" is not authorized to "+wcampDocument.getNeededPerm();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_UNAUTHORIZED, message);
        } catch (ConnectException e) {
            String message = "Received unexpected response from "+targetUrl.toString()+": "+e.getMessage();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_BAD_GATEWAY, message);
        } catch (SocketTimeoutException e) {
            String message = "Timed out waiting for "+targetUrl.toString();
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_GATEWAY_TIMEOUT, message);
        } catch (FileNotFoundException e){
            String uri;
            if(containerExists){
                uri = targetUrl.toString();
            } else {
                uri = testUrl;
            }
            String message = "Please check that "+uri+" exists";
            log(message, e);
            writeErrorAsJson(res, HttpServletResponse.SC_NOT_FOUND, message);
        } catch (IOException e) {
            if (null != targetConnection) {
                int responseStatus = 0;
                try {
                    responseStatus = ((HttpURLConnection)targetConnection).getResponseCode();
                    if(400<=responseStatus && 500>responseStatus) {
                        // Useful info for the client
                        InputStream responseErrStream = ((HttpURLConnection) targetConnection).getErrorStream();
                        // TODO refactor the lines below so that the same code is used for the servlet response in case of success
                        byte errBytes[] = extractByteArray(responseErrStream);
                        copyResponseHeaders(targetConnection, res, errBytes.length);
                        res.getOutputStream().write(errBytes);
                    } else {
                        String nonce = getErrorNonce();
                        String message = "Please review the logs for code "+ nonce;
                        log("---- Begin ErrorMessage for error code "+nonce+"\nReceived unexpected response status "
                                +responseStatus+" from POST "+targetUrl.toString());
                        writeErrorAsJson(res, HttpServletResponse.SC_BAD_GATEWAY, message);
                    }
                } catch (IOException e1) {
                    logServerErrorWithNonce(res);
                    e1.printStackTrace();
                }
            } else {
                logServerErrorWithNonce(res);
                e.printStackTrace();
            }
        } catch (RuntimeException e){
            logServerErrorWithNonce(res);
            e.printStackTrace();
        }
    }

    private static void setStreamingMode(int contentLength, URLConnection conn) {

        // See: http://stackoverflow.com/questions/2793150#2793153 (Streaming mode)
        // Set streaming mode, else HttpURLConnection will buffer everything before actually sending it.
        if (contentLength > -1) {
            //Content length is known beforehand, so no internal buffering will be taken place.
            ((HttpURLConnection) conn).setFixedLengthStreamingMode(contentLength);
        } else {
            // Content length is unknown, so send in 1KB chunks (which will also be the internal buffer size).
            // Note: the dst endpoint must be able to accept an InputStream!
            ((HttpURLConnection) conn).setChunkedStreamingMode(DEFAULT_CHUNK_SIZE);
        }
    }

    private static void copyRequestHeaders(HttpServletRequest req, URLConnection proxyReq) throws EvilHeaderException {
        Enumeration enumerationOfHeaderNames = req.getHeaderNames();

        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();

            if (HttpHeaders.evilHeadersLc.containsKey(headerName.toLowerCase()) &&
                    HttpHeaders.evilHeadersLc.get(headerName.toLowerCase()).equals(
                            req.getHeader(headerName).toLowerCase())) {
                throw new EvilHeaderException(headerName, req.getHeader(headerName));
            }

            if (HttpHeaders.hopByHopHeadersLc.contains(headerName.toLowerCase()))
                continue;

            Enumeration headers = req.getHeaders(headerName);
            while (headers.hasMoreElements()) {
              String headerValue = (String) headers.nextElement();
              proxyReq.setRequestProperty(headerName, headerValue);
            }
        }
    }

    private static void copyResponseHeaders(URLConnection proxyRes, HttpServletResponse res, int streamLength) {
        Map<String,List<String>> responseMap = proxyRes.getHeaderFields();
        String statusLine = proxyRes.getHeaderField(0);
        // i.e. "HTTP/1.1 201 Created"
        //TODO: 502 if npe
        res.setStatus(Integer.parseInt(statusLine.split(" ")[1]));

        for (String headerName : responseMap.keySet()) {
            if (null == headerName ||  // null is the "Status" header. It's been processed already
                HttpHeaders.hopByHopHeadersLc.contains(headerName.toLowerCase()) ||
                    CustomHttpHeaders.doNotCopyLc.contains(headerName.toLowerCase()))
                continue;

            // Do not use getHeaderField() here: in case of multivalued headers, it returns only the last value!
            // Servlet's setHeader() deals OK with a multivalued header, joining its values with commas
            List<String> hValues = responseMap.get(headerName);
            for (String headerValue : hValues) {
                if (HttpHeaders.integerValuedHeadersLc.contains(headerName.toLowerCase())) {
                    // In a streaming URLConnection (setDoOutput = true), .getContentLength() is mostly wrong,
                    // so the HttpServletResponse would be blocked forever.
                    // The real "Content-Length" comes from .getInputStream()
                    if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                        res.setContentLength(streamLength);
                    } else {
                        res.setIntHeader(headerName, Integer.parseInt(headerValue));
                        // the only (standard) allowed Integer header is Content-Length
                        // but maybe the response includes a custom one
                    }
                } else {
                    res.setHeader(headerName, headerValue);
                }
            }
        }
    }

    private static byte[] extractByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
        int read = 0;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }
        baos.flush();
        return  baos.toByteArray();
    }

    @Override
    public void writeErrorAsJson(HttpServletResponse response, int code, String msg) {
        response.setStatus(code);
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(UTF_8);
        try {
            response.getWriter().write("{\"msg\":\"" + msg + "\"}");
        } catch (IOException e) {
            log("Could not write response!!");
            e.printStackTrace();
        }
    }

    @Override
    public String getErrorNonce() {
        String errorNonce = null;
        try {
            SecureRandom sr = SecureRandom.getInstance(SHA1PRNG);
            errorNonce = Integer.toString(sr.nextInt());
        } catch (NoSuchAlgorithmException e) {
            log("getErrorNonce: Algorithm "+SHA1PRNG+"is not valid!");
            e.printStackTrace();
        }
        return errorNonce;
    }

    private void logServerErrorWithNonce(HttpServletResponse res) {
        String nonce = getErrorNonce();
        String message = "Please review the logs for code "+ nonce;
        writeErrorAsJson(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        log("---- Begin StackTrace for error code "+nonce);
    }
}
