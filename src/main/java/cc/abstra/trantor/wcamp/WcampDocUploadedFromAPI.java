package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import java.io.IOException;


public class WcampDocUploadedFromAPI extends WcampDocumentResource implements ArchiveResource {

    //TODO: move NEEDED_PER to each Interface if finer-grained auth is required
    public static final String NEEDED_PERM ="upload_doc";

    private String clientId;
    private String tempDocId;

    public WcampDocUploadedFromAPI(String authToken, String clientId, String tempDocId, String uploadIdentifier)
            throws IOException, MissingClientHeadersException {

        super(HttpHeaders.AUTHORIZATION, authToken, NEEDED_PERM, uploadIdentifier);

        if (null != clientId && null != tempDocId) {
            this.clientId = clientId;
            this.tempDocId = tempDocId;
            verify(tempDocId, TEMP_PATH);  //never call overridable methods in a constructor
        } else {
            throw new MissingClientHeadersException(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID);
        }
    }

    @Override
    public void addVersion(String filesInfo) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void archive(String fileInfo) throws IOException {
        this.headers.put(CustomHttpHeaders.X_TRANTOR_UPLOADED_FILES_INFO, fileInfo);
        this.headers.put(CustomHttpHeaders.X_TRANTOR_CLIENT_ID, clientId);
        restRequest(WCAMP_URI + TEMP_PATH + tempDocId + ARCHIVE_CMD, HttpMethods.PUT);
    }
}