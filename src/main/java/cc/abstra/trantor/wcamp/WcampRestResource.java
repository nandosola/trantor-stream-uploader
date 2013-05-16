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

import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** This class interacts with Trantor's metadata front-end API, internally known as WCAMP */
public abstract class WcampRestResource {

    protected static final String WCAMP_URI = "http://localhost:8080";
    private static final int TEN_SEC = 10000;  //msec
    private static final String CHARSET = "UTF-8";

    protected static void restRequest(java.lang.String url, java.lang.String method)
            throws WcampConnectionException, WcampRestRequestIOException {

        Map<String, String> headers = new HashMap<String, String>();
        restRequest(url, method, headers);
    }

    protected static void restRequest(String url, String method, Map<String, String> headers)
            throws WcampConnectionException, WcampRestRequestIOException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (!headers.isEmpty()) {
                Iterator it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    connection.setRequestProperty((String) pairs.getKey(), (String) pairs.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
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
            // TODO: process response body
            //InputStream response = connection.getInputStream();

            int responseCode = connection.getResponseCode();
            if (400 <= responseCode) {
                if (HttpServletResponse.SC_NOT_FOUND == responseCode) {
                    throw new DocumentNotFoundException();
                } else {
                    if (500 < responseCode) {
                        throw new WcampClientErrorException(responseCode);
                    } else {
                        throw new WcampServerErrorException(responseCode);
                    }
                }
            }
        } catch (IOException e) {
            throw new WcampRestRequestIOException(e);
        } finally {
            if (null != connection)
                connection.disconnect();
        }
    }
}
