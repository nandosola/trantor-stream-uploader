package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampPendingDoc;
import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;
import cc.abstra.trantor.wcamp.exceptions.WcampRestRequestIOException;

import javax.servlet.AsyncContext;

public class AddToPendingDocs implements Runnable {

    private AsyncContext ac;
    private final String filesInfo;
    private final String cookie;

    public AddToPendingDocs(AsyncContext ac, String cookie, String filesInfo) {
        this.ac = ac;
        this.filesInfo = filesInfo;
        this.cookie = cookie;
    }

    @Override
    public void run() {
        try {
            WcampPendingDoc.add(filesInfo, cookie);
        } catch (WcampRestRequestIOException|WcampConnectionException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
