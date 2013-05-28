package cc.abstra.trantor.exceptions;

public class EvilHeaderException extends Exception {
    public EvilHeaderException(String headerName, String headerValue) {
        super("The header "+headerName+": "+headerValue+
                " is pure evil, so I cannot work with it. Please don't use it!");
    }
}
