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
package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** This class interacts with Trantor's metadata front-end API, internally known as WCAMP */
public abstract class WcampDocumentResource implements AuthorizedResource, VersionedResource {

    protected static final String WCAMP_URI = "http://localhost:8080";
    private static final int TEN_SEC = 10000;  //msec
    private static final String CHARSET = "UTF-8";
    private static final String APPLICATION_JSON = "application/json";

    public static final String RESOURCE_PATH = "/documents/";

    protected Map<String, String> headers = new HashMap<>();
    protected String authToken;
    protected String neededPerm;
    protected String id;
    protected String uploadType;

    protected WcampDocumentResource(String headerName, String token, String neededPerm, String uploadIdentifier)
            throws MissingClientHeadersException, IOException {

        if (null != token) {
            this.authToken = token;
            this.neededPerm = neededPerm;
            headers.put(headerName, authToken);
            // Our OAuth provider will send 403 to API clients, or else 302 to /login.html
            headers.put(HttpHeaders.ACCEPT, APPLICATION_JSON);

        } else {
            throw new MissingClientHeadersException(CustomHttpHeaders.X_TRANTOR_CLIENT_ID + ", " +
                    CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID);
        }

        if (null != uploadIdentifier) {
            this.setUploadType(uploadIdentifier.toLowerCase());
        }
    }

    protected void verify(String id, String path) throws IOException {
        restRequest(WCAMP_URI + path + id, HttpMethods.HEAD);
    }

    @Override
    public void authorize() throws IOException {
        restRequest(WCAMP_URI + PERMISSION + neededPerm, HttpMethods.GET);
    }

    protected void restRequest(String url, String method) throws IOException {
        restRequest(url, method, headers);  //Sending "Cookie" or "Authorization" header by default
    }

    private void restRequest(String url, String method, Map<String, String> headers) throws IOException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
                    Map.Entry pairs = (Map.Entry) stringStringEntry;
                    connection.setRequestProperty((String) pairs.getKey(), (String) pairs.getValue());
                }
            }
            connection.setConnectTimeout(TEN_SEC);
            connection.setReadTimeout(TEN_SEC);
            connection.setRequestMethod(method);

            if (HttpMethods.PUT.equals(method) || HttpMethods.POST.equals(method)){
                String body = "";
                connection.setDoOutput(true);
                try(OutputStream output = connection.getOutputStream()){
                    output.write(body.getBytes(CHARSET));
                }
            }
            // TODO: process response body?
            //InputStream response = connection.getInputStream();

            int responseCode = connection.getResponseCode();
            if (400 <= responseCode) {
                if (HttpServletResponse.SC_NOT_FOUND == responseCode) {
                    throw new DocumentNotFoundException(url);
                } else {
                    if (HttpServletResponse.SC_UNAUTHORIZED == responseCode ||
                            HttpServletResponse.SC_FORBIDDEN == responseCode) {
                        throw new WcampNotAuthorizedException(authToken);
                    } else {
                        if (500 < responseCode) {
                            throw new WcampClientErrorException(responseCode);
                        } else {
                            throw new WcampServerErrorException(responseCode);
                        }
                    }
                }
            }
        } finally {
            if (null != connection)
                connection.disconnect();
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getNeededPerm() {
        return neededPerm;
    }

    private void setUploadType(String type) {
        if(CustomHttpHeaders.validUploadTypesLc.contains(type.toLowerCase())){
            this.uploadType = type;
        } else {
            throw new UnsupportedOperationException("Unknown upload type");
        }
    }

    public String getUploadType() {
        return uploadType;
    }
}
