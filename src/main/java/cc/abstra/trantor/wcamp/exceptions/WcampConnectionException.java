package cc.abstra.trantor.wcamp.exceptions;


public class WcampConnectionException extends RuntimeException {
    public int errorCode;

    public WcampConnectionException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
