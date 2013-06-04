package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WcampDocUploadedFromWeb extends WcampDocumentResource implements WorklistResource {

    public WcampDocUploadedFromWeb(String authToken, String docCode) throws MissingClientHeadersException, IOException {
        super(HttpHeaders.COOKIE, authToken, NEEDED_PERM, docCode);
    }

    @Override
    public void add(String uploadedDocsInfo) throws IOException {
        headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, uploadedDocsInfo);
        headers.put(HttpHeaders.COOKIE, authToken);
        restRequest(WCAMP_URI + PATH + ADD_CMD, HttpMethods.PUT);
    }

    @Override
    public void addVersion(String filesInfo) throws IOException {
        if(VERSION.equals(uploadType)){
            headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, filesInfo);
            restRequest(WCAMP_URI + DOCS_BY_CODE + code + VERSION_CMD, HttpMethods.PUT);
        } else {
            throw new UnsupportedOperationException("Cannot verify version code for non-versioned docs");
        }
    }

    @Override
    public void remove(String docId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}