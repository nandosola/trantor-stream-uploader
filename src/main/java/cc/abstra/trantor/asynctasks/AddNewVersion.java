package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.VersionedResource;
import cc.abstra.trantor.wcamp.WcampDocumentResource;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class AddNewVersion implements Runnable {

    private AsyncContext ac;
    private final WcampDocumentResource versionedDoc;
    private final String filesInfo;

    public AddNewVersion(AsyncContext ac, WcampDocumentResource pendingDoc, String filesInfo) {
        this.ac = ac;
        this.versionedDoc = pendingDoc;
        this.filesInfo = filesInfo;
    }

    @Override
    public void run() {
        try {
            ((VersionedResource)versionedDoc).addVersion(filesInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}