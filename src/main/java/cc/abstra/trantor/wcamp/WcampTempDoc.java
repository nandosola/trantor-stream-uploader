package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.HttpMethods;

import java.io.IOException;


public class WcampTempDoc extends WcampDocumentResource implements ArchiveResource {

    private String clientId;
    private String tempDocId;

    public WcampTempDoc(String authToken, String clientId, String tempDocId) {
        super(HttpHeaders.COOKIE, authToken, NEEDED_PERM);
        this.clientId = clientId;
        this.tempDocId = tempDocId;
    }

    @Override
    public void verify() throws IOException {
        restRequest(WCAMP_URI + PATH + tempDocId, HttpMethods.HEAD);
    }

    @Override
    public void archive() throws IOException {
        restRequest(WCAMP_URI + PATH + tempDocId + ARCHIVE_CMD, HttpMethods.PUT);
        // 410 -- Gone  tmpcollection expired ???
    }
}