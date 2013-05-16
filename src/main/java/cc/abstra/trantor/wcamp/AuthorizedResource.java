package cc.abstra.trantor.wcamp;

import java.io.IOException;

public interface AuthorizedResource {

    public static final String PERMISSION = "/permission/";

    public void authorize() throws IOException;

}
