package cc.abstra.trantor.wcamp.exceptions;


public class WcampClientErrorException extends WcampConnectionException {
    private static final String MSG="Client Error";

    public WcampClientErrorException(int status) {
        super(MSG, status);
    }
}
