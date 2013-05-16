package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampTempDoc;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class ArchiveTempDoc implements Runnable {

    private AsyncContext ac;
    private final WcampTempDoc tempDocument;

    public ArchiveTempDoc(AsyncContext ac, WcampTempDoc tempDocument) {
        this.ac = ac;
        this.tempDocument = tempDocument;
    }

    @Override
    public void run() {
        try {
            tempDocument.archive();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
