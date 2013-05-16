package cc.abstra.trantor.wcamp;


import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;

import java.io.IOException;

public interface ArchiveResource {

    public static final String PATH = "/documents/tmp/";
    public static final String ARCHIVE_CMD = "/archive/";
    public static final String NEEDED_PERM ="upload_doc";

    public void verify() throws WcampConnectionException, IOException;
    public void archive() throws WcampConnectionException, IOException;
}
