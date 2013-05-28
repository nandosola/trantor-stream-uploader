package cc.abstra.trantor.wcamp;


import java.io.IOException;

public interface ArchiveResource {

    public static final String PATH = "/documents/tmp/";
    public static final String ARCHIVE_CMD = "/archive/";
    public static final String NEEDED_PERM ="upload_doc";

    public void archive(String fileInfo) throws IOException;
}
