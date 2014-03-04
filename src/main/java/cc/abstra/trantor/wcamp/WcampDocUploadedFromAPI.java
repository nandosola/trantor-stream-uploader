package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;


public class WcampDocUploadedFromAPI extends WcampDocumentResource implements ArchivedResource, VersionedResource {

    private String clientId;
    private String tempDocId;

    public WcampDocUploadedFromAPI(String authToken, String clientId, String tempDocId, String uploadIdentifier,
                                   String docIdentifier)
            throws IOException, MissingClientHeadersException {

        super(HttpHeaders.AUTHORIZATION, authToken, uploadIdentifier, docIdentifier);

        if (null == clientId)
            throw new MissingClientHeadersException(CustomHttpHeaders.X_TRANTOR_CLIENT_ID);

        if (null != tempDocId) {  // upload document (version=0)
            this.clientId = clientId;
            this.tempDocId = tempDocId;
        } else {
            if (!VersionedResource.VERSION.equals(uploadIdentifier)) {  // upload version (no new metadata)
                throw new MissingClientHeadersException(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE +
                ", "+ CustomHttpHeaders.X_TRANTOR_DOCUMENT_ID);
            }
        }
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public void archive(String fileInfo) throws IOException {
        verify(tempDocId, ArchivedResource.TEMP_PATH);
        authorize(ArchivedResource.NEEDED_PERM);
        this.headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, fileInfo);
        this.headers.put(CustomHttpHeaders.X_TRANTOR_CLIENT_ID, clientId);
        restRequest(WCAMP_URI + TEMP_PATH + tempDocId + ARCHIVE_CMD, HttpMethods.PUT);
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