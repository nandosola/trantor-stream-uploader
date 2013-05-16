package cc.abstra.trantor.wcamp;

import cc.abstra.trantor.HttpMethods;
import cc.abstra.trantor.wcamp.exceptions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class WcampTempDoc extends WcampRestResource {
    private static final String PATH = "/documents/tmp/";
    private static final String ARCHIVE_CMD = "/archive";

    public static void verify(String tmpDocId) throws WcampConnectionException, WcampRestRequestIOException {
        restRequest(WCAMP_URI + PATH + tmpDocId, HttpMethods.HEAD);
    }

    public static void archive(String tmpDocId) throws WcampConnectionException, WcampRestRequestIOException {
        restRequest(WCAMP_URI + PATH + tmpDocId + ARCHIVE_CMD, HttpMethods.PUT);
    }
}