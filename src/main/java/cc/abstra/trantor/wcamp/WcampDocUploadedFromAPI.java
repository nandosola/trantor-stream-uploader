package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;


public class WcampDocUploadedFromAPI extends WcampDocumentResource implements ArchiveResource {

    private String clientId;
    private String tempDocId;

    public WcampDocUploadedFromAPI(String authToken, String clientId, String tempDocId, String docCode)
            throws IOException, MissingClientHeadersException {

        super(HttpHeaders.AUTHORIZATION, authToken, NEEDED_PERM, docCode);
        if (null != clientId && null != tempDocId) {
            this.clientId = clientId;
            this.tempDocId = tempDocId;
            verify();  //never call overridable methods in a constructor
        } else {
            throw new MissingClientHeadersException();
        }
    }

    private void verify() throws IOException {
        restRequest(WCAMP_URI + PATH + tempDocId, HttpMethods.HEAD);
    }

    @Override
    public void setUploadType(String type) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void addVersion(String filesInfo) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void archive(String fileInfo) throws IOException {
        this.headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, fileInfo);
        this.headers.put(CustomHttpHeaders.X_TRANTOR_CLIENT_ID, clientId);
        restRequest(WCAMP_URI + PATH + tempDocId + ARCHIVE_CMD, HttpMethods.PUT);
    }
}