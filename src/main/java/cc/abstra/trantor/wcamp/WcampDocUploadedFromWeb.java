package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;

public class WcampDocUploadedFromWeb extends WcampDocumentResource implements WorklistResource, VersionedResource {

    public WcampDocUploadedFromWeb(String authToken, String docIdentifier, String uploadIdentifier)
            throws IOException, MissingClientHeadersException {

        super(HttpHeaders.COOKIE, authToken, uploadIdentifier, docIdentifier);

        if (null != docIdentifier) {
            if (!VERSION.equals(uploadIdentifier)) {
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
    public void removeFromWorklist(String docId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void addVersion(String filesInfo) throws IOException {
        verify(docId, DOCUMENTS_RESOURCE_PATH);
        authorize(VersionedResource.NEEDED_PERM);
        if(VERSION.equals(uploadType)){
            headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, filesInfo);
            restRequest(WCAMP_URI + DOCUMENTS_RESOURCE_PATH + docId + VERSION_CMD, HttpMethods.PUT);
        } else {
            throw new UnsupportedOperationException("Cannot verify version identifier for non-versioned docs");
        }
    }

}