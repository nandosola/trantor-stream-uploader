package cc.abstra.trantor.wcamp;


import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** This class interacts with Trantor's metadata front-end API, internally known as WCAMP */
public class WcampClient {

    private static final int TEN_SEC = 10000;  //msec
    private static final String WCAMP_URI = "http://localhost:8080";

    public static class TempDoc {
        private static final String PATH = "/documents/tmp/";
        private static final String ARCHIVE_CMD = "/archive";

        public static void verify(String tmpDocId) throws WcampConnectionException, WcampRestRequestIOException {
            restRequest(WCAMP_URI+PATH+tmpDocId, HttpMethods.HEAD);
        }

        public static void archive(String tmpDocId) throws WcampConnectionException, WcampRestRequestIOException {
            restRequest(WCAMP_URI+PATH+tmpDocId+ARCHIVE_CMD, HttpMethods.PUT);
        }
    }

    private static void restRequest(String url, String method) throws WcampConnectionException, WcampRestRequestIOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TEN_SEC);
            connection.setReadTimeout(TEN_SEC);
            connection.setRequestMethod(method);
            int responseCode = connection.getResponseCode();
            if ( 400 >= responseCode ){
                if(HttpServletResponse.SC_NOT_FOUND == responseCode){
                    throw new DocumentNotFoundException();
                } else {
                    if ( 500 < responseCode ){
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
