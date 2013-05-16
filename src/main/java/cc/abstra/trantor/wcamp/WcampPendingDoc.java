package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WcampPendingDoc extends WcampDocumentResource implements WorklistResource {

    public WcampPendingDoc(String authToken) {
        super(HttpHeaders.COOKIE, authToken, NEEDED_PERM);
    }

    @Override
    public void add(String uploadedDocsInfo) throws IOException {

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, uploadedDocsInfo);
        customHeaders.put(HttpHeaders.COOKIE, authToken);
        restRequest(WCAMP_URI + PATH + ADD_CMD, HttpMethods.PUT, customHeaders);
    }

    @Override
    public void remove(String docId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}