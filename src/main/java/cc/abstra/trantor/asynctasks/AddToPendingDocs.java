package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampPendingDoc;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class AddToPendingDocs implements Runnable {

    private AsyncContext ac;
    private final WcampPendingDoc pendingDoc;
    private final String filesInfo;

    public AddToPendingDocs(AsyncContext ac, WcampPendingDoc pendingDoc, String filesInfo) {
        this.ac = ac;
        this.pendingDoc = pendingDoc;
        this.filesInfo = filesInfo;
    }

    @Override
    public void run() {
        try {
            pendingDoc.add(filesInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
