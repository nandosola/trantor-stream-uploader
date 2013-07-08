package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampDocUploadedFromWeb;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class AddToPendingDocs implements Runnable {

    private AsyncContext ac;
    private final WcampDocUploadedFromWeb pendingDoc;
    private final String filesInfo;

    public AddToPendingDocs(AsyncContext ac, WcampDocUploadedFromWeb pendingDoc, String filesInfo) {
        this.ac = ac;
        this.pendingDoc = pendingDoc;
        this.filesInfo = filesInfo;
    }

    @Override
    public void run() {
        try {
            pendingDoc.addToWorklist(filesInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
