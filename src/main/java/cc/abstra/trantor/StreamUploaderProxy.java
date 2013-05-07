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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class StreamUploaderProxy extends HttpServlet {

    static final int DEFAULT_CHUNK_SIZE = 1024;
    static final int FIVE_MIN = 300000;  //msec
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
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if(containerExists) {  // use container's logging facility
          log("POST " + req.getRequestURI() + " --> " + targetUrl.toString());
        } else {  // testing environment
            if(null == targetUrl)
                this.targetUrl = new URL(testUrl);
        }

        URLConnection targetConnection = null;

        try {
            targetConnection = targetUrl.openConnection();
            copyRequestHeaders(req, targetConnection);
            //  Note: These headers below would've been sent by default:
            // {
            //   "User-Agent": "Java/1.7.0_17",
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
                res.getOutputStream().write(outBytes);
            }

        } catch (ConnectException e) {
            res.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        } catch (SocketTimeoutException e) {
            res.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        } catch (IOException e) {
            int responseStatus = 0;
            if (null != targetConnection) {
                responseStatus = ((HttpURLConnection)targetConnection).getResponseCode();
            }
            if (HttpServletResponse.SC_INTERNAL_SERVER_ERROR == responseStatus){
                res.sendError(HttpServletResponse.SC_BAD_GATEWAY);
            } else {
                throw e;
            }

        }
        // note: the uncaught exceptions will be shown as "500"

        // TODO: sanitize input urls
        // TODO: "cookie"=>"rack.session=… set again in the response: log cookie

        // Trantor-specific behavior:
        // TODO: check permissions remotely w/ headers Cookie or Authorization:
        //   GET /permissions/upload_doc
        //   GET /permissions/update_doc
        //   statuses: 200, 403
        // TODO: tell Wcamp to move metadata if 201
        //   PUT /documents/code/:code/metadata

    }

    private static void setStreamingMode(int contentLength, URLConnection conn) {
        // Set streaming mode, else HttpURLConnection will buffer everything.
        if (contentLength > -1) {
            // Content length is known beforehand, so no buffering will be taken place.
            ((HttpURLConnection) conn).setFixedLengthStreamingMode(contentLength);
        } else {
            // Content length is unknown, so send in 1KB chunks (which will also be the internal buffer size).
            ((HttpURLConnection) conn).setChunkedStreamingMode(DEFAULT_CHUNK_SIZE);
        }
    }

    private static void copyRequestHeaders(HttpServletRequest req, URLConnection proxyReq) {
        Enumeration enumerationOfHeaderNames = req.getHeaderNames();

        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();

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
                HttpHeaders.hopByHopHeadersLc.contains(headerName.toLowerCase()))
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
}
