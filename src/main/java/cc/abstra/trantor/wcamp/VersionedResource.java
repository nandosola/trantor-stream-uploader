package cc.abstra.trantor.wcamp;


import java.io.IOException;

public interface VersionedResource {

    public static final String VERSION = "version";
    public static final String VERSION_CMD = "/addversion";
    public static final String NEEDED_PERM ="update_doc";

    public abstract void addVersion(String filesInfo) throws IOException;
}
