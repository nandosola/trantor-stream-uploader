package cc.abstra.trantor.wcamp.exceptions;

import cc.abstra.trantor.wcamp.CustomHttpHeaders;

public class MissingClientHeadersException extends Exception {
    public MissingClientHeadersException() {
        super("Make sure the auth header (Cookie or Authorization) and/or "+ CustomHttpHeaders.X_TRANTOR_CLIENT_ID+" and "+
                CustomHttpHeaders.X_TRANTOR_ASSIGNED_UPLOAD_ID+" are set and have a correct value!");
    }
}
