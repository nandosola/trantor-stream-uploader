package cc.abstra.trantor.wcamp;

import java.io.IOException;

public interface AuthorizedResource {

    public static final String PERMISSION = "/permission/";

    public abstract void authorize(String neededPerm) throws IOException;
    public abstract void verify(String id, String path) throws IOException;

}
