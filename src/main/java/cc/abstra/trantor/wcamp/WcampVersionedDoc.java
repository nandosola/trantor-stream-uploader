package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.wcamp.exceptions.MissingClientHeadersException;

public class WcampVersionedDoc extends WcampDocumentResource {

    protected WcampVersionedDoc(String headerName, String token, String neededPerm) throws MissingClientHeadersException {
        super(headerName, token, neededPerm);
    }
}
