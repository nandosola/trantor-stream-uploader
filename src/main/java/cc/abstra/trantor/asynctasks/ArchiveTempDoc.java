package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampTempDoc;
import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;
import cc.abstra.trantor.wcamp.exceptions.WcampRestRequestIOException;

import javax.servlet.AsyncContext;

public class ArchiveTempDoc implements Runnable {

    private AsyncContext ac;
    private final String fileId;

    public ArchiveTempDoc(AsyncContext ac, String fileId) {
        this.ac = ac;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        try {
            WcampTempDoc.archive(fileId);
        } catch (WcampRestRequestIOException |WcampConnectionException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
