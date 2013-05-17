package cc.abstra.trantor;

import javax.servlet.http.HttpServletResponse;

public interface JsonErrorResponses {
    public static final String CONTENT_TYPE = "application/json";
    public static final String UTF_8 = "UTF-8";
    public static final String SHA1PRNG = "SHA1PRNG";
    
    public void writeErrorAsJson(HttpServletResponse response, int code, String msg);
    public String getErrorNonce();
}
