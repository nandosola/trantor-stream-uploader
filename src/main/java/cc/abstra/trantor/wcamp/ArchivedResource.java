package cc.abstra.trantor.wcamp;


import java.io.IOException;

public interface ArchivedResource {

    public static final String TEMP_PATH = "/documents/tmp/";
    public static final String ARCHIVE_CMD = "/archive/";
    public static final String NEEDED_PERM ="upload_doc";

    public abstract void archive(String fileInfo) throws IOException;
}
