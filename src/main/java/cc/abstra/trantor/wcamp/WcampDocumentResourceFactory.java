package cc.abstra.trantor.wcamp;


import cc.abstra.trantor.HttpHeaders;
import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class WcampDocumentResourceFactory {

    private WcampDocumentResourceFactory(){}

    public static WcampDocumentResource create(HttpServletRequest req) throws MissingClientHeadersException, IOException {

        WcampDocumentResource wcampDocument;

        String uploadType = req.getHeader(CustomHttpHeaders.X_TRANTOR_UPLOAD_TYPE);
        String documentIdentifier =  req.getHeader(CustomHttpHeaders.X_TRANTOR_DOCUMENT_ID);
        String clientId = req.getHeader(CustomHttpHeaders.X_TRANTOR_CLIENT_ID);

        if(null != clientId){
            String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
            String trantorTempFileId = req.getHeader(CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID);

            wcampDocument = new WcampDocUploadedFromAPI(auth, clientId, trantorTempFileId, uploadType, documentIdentifier);

        } else {
            String auth = req.getHeader(HttpHeaders.COOKIE);
            wcampDocument = new WcampDocUploadedFromWeb(auth, documentIdentifier, uploadType);
        }

        return wcampDocument;
    }

}
