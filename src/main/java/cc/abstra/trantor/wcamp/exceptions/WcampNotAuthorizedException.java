package cc.abstra.trantor.wcamp.exceptions;

import javax.servlet.http.HttpServletResponse;

public class WcampNotAuthorizedException extends WcampConnectionException {
    private static final String MSG="Not authorized";

    public WcampNotAuthorizedException(String authToken) {
        super(MSG+". Auth token: "+authToken, HttpServletResponse.SC_UNAUTHORIZED);
    }
}
