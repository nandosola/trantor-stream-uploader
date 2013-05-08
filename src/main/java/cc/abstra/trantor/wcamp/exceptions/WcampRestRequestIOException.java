package cc.abstra.trantor.wcamp.exceptions;


import java.io.IOException;

public class WcampRestRequestIOException extends IOException {
    public WcampRestRequestIOException(IOException e) {
        super(e);
    }
}
