package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;
import cc.abstra.trantor.wcamp.exceptions.WcampRestRequestIOException;

import java.util.HashMap;
import java.util.Map;

public class WcampPendingDoc extends WcampRestResource {
    private static final String PATH = "/pendingdocuments";
    private static final String ADD_CMD = "/add/";
    private static final java.lang.String REMOVE_CMD = "/remove/";

    public static void add(String uploadedDocsInfo, String cookie)
            throws WcampConnectionException,WcampRestRequestIOException {

        Map<String, String> headers = new HashMap<>();
        headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, uploadedDocsInfo);
        headers.put(HttpHeaders.COOKIE, cookie);
        restRequest(WCAMP_URI + PATH + ADD_CMD, HttpMethods.PUT, headers);
    }

    public static void remove(String docId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}