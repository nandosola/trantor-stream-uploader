package cc.abstra.trantor.asynctasks;


import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrantorAsyncListener implements AsyncListener {

    private final static Logger LOGGER = Logger.getLogger(TrantorAsyncListener.class.getName());

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        LOGGER.log(Level.INFO, "task completed");
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        LOGGER.log(Level.SEVERE, "task timed out!");
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        LOGGER.log(Level.SEVERE, "task ended with errors!");
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        LOGGER.log(Level.INFO, "task started");
    }
}
