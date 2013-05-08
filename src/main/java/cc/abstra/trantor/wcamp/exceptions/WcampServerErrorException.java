package cc.abstra.trantor.wcamp.exceptions;


public class WcampServerErrorException extends WcampConnectionException {
    private static final String MSG="Server Error";

    public WcampServerErrorException(int status) {
        super(MSG, status);
    }
}
