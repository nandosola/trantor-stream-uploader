package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampDocUploadedFromWeb;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AddToPendingDocs implements Callable {

    private AsyncContext ac;
    private final WcampDocUploadedFromWeb pendingDoc;
    private final String filesInfo;

    public AddToPendingDocs(AsyncContext ac, WcampDocUploadedFromWeb pendingDoc, String filesInfo) {
        this.ac = ac;
        this.pendingDoc = pendingDoc;
        this.filesInfo = filesInfo;
    }

    @Override
    public TaskResult call() {
        boolean success = true;
        try {
            pendingDoc.addToWorklist(filesInfo);
        } catch (IOException e) {
            success = false;
        } finally {
            ac.complete();
        }

        return TaskResult.generate(success);
    }
}
