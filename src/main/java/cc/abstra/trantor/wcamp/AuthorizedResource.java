package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;

import java.io.IOException;

public interface AuthorizedResource {

    public static final String PERMISSION = "/permission/";

    public void authorize() throws WcampConnectionException, IOException;

}
