package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampDocUploadedFromAPI;

import javax.servlet.AsyncContext;
import java.io.IOException;

public class ArchiveTempDoc implements Runnable {

    private AsyncContext ac;
    private final WcampDocUploadedFromAPI tempDocument;
    private final String filesInfo;

    public ArchiveTempDoc(AsyncContext ac, WcampDocUploadedFromAPI tempDocument, String filesInfo) {
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
