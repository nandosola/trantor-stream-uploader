package cc.abstra.trantor.wcamp;


import java.io.IOException;

public interface ArchiveResource {

    public static final String TEMP_PATH = "/documents/tmp/";
    public static final String ARCHIVE_CMD = "/archive/";

    public void archive(String fileInfo) throws IOException;
}
