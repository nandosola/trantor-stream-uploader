package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampTempDoc;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class ArchiveTempDoc implements Runnable {

    private AsyncContext ac;
    private final WcampTempDoc tempDocument;
    private final String filesInfo;

    public ArchiveTempDoc(AsyncContext ac, WcampTempDoc tempDocument, String filesInfo) {
        this.ac = ac;
        this.tempDocument = tempDocument;
        this.filesInfo = filesInfo;
    }

    @Override
    public void run() {
        try {
            tempDocument.archive(filesInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ac.complete();
        }
    }
}
