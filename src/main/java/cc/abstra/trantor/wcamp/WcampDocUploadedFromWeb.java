package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;

public class WcampDocUploadedFromWeb extends WcampDocumentResource implements WorklistResource {

    //TODO: move NEEDED_PER to each Interface if finer-grained auth is required
    public static final String NEEDED_PERM ="upload_doc";

    public WcampDocUploadedFromWeb(String authToken, String docIdentifier, String uploadIdentifier)
            throws IOException, MissingClientHeadersException {

        super(HttpHeaders.COOKIE, authToken, NEEDED_PERM, uploadIdentifier);

        if (null != docIdentifier) {
            this.id = docIdentifier;
            if (VERSION.equals(uploadIdentifier)) {
                verify(docIdentifier, RESOURCE_PATH);
            } else {
                throw new MissingClientHeadersException(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE);
            }
        }
    }

    @Override
    public void addToWorklist(String uploadedDocsInfo) throws IOException {
        headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, uploadedDocsInfo);
        headers.put(HttpHeaders.COOKIE, authToken);
        restRequest(WCAMP_URI + WORKLIST_PATH + ADD_CMD, HttpMethods.PUT);
    }

    @Override
    public void addVersion(String filesInfo) throws IOException {
        if(VERSION.equals(uploadType)){
            headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, filesInfo);
            restRequest(WCAMP_URI + RESOURCE_PATH + id + VERSION_CMD, HttpMethods.PUT);
        } else {
            throw new UnsupportedOperationException("Cannot verify version identifier for non-versioned docs");
        }
    }

    @Override
    public void removeFromWorklist(String docId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}