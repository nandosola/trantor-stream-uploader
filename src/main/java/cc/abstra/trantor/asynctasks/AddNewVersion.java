package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.VersionedResource;
import cc.abstra.trantor.wcamp.WcampDocumentResource;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AddNewVersion implements Callable {

    private AsyncContext ac;
    private final WcampDocumentResource versionedDoc;
    private final String filesInfo;

    public AddNewVersion(AsyncContext ac, WcampDocumentResource pendingDoc, String filesInfo) {
        this.ac = ac;
        this.versionedDoc = pendingDoc;
        this.filesInfo = filesInfo;

    }

    @Override
    public TaskResult call() {
        boolean success = true;
        try {
            ((VersionedResource)versionedDoc).addVersion(filesInfo);
        } catch (IOException e) {
            success = false;
        } finally {
            ac.complete();
        }
        return TaskResult.generate(success);
    }
}