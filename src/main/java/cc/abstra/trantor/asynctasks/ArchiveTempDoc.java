package cc.abstra.trantor.asynctasks;

import cc.abstra.trantor.wcamp.WcampDocUploadedFromAPI;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ArchiveTempDoc implements Callable {

    private AsyncContext ac;
    private final WcampDocUploadedFromAPI tempDocument;
    private final String filesInfo;

    public ArchiveTempDoc(AsyncContext ac, WcampDocUploadedFromAPI tempDocument, String filesInfo) {
        this.ac = ac;
        this.tempDocument = tempDocument;
        this.filesInfo = filesInfo;
    }

    @Override
    public TaskResult call() {
        boolean success = true;
        try {
            tempDocument.archive(filesInfo);
        } catch (IOException e) {
            success = false;
        } finally {
            ac.complete();
        }

        return TaskResult.generate(success);
    }
}
