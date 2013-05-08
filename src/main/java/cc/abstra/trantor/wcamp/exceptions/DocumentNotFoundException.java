package cc.abstra.trantor.wcamp.exceptions;


import javax.servlet.http.HttpServletResponse;

public class DocumentNotFoundException extends WcampConnectionException {
    private static final String MSG="Document not found";

    public DocumentNotFoundException() {
        super(MSG, HttpServletResponse.SC_NOT_FOUND);
    }
}
