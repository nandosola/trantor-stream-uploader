package cc.abstra.trantor.wcamp;


import cc.abstra.trantor.wcamp.exceptions.WcampConnectionException;

import java.io.IOException;

public interface WorklistResource {

    public static final String PATH = "/pendingdocuments";
    public static final String ADD_CMD = "/add/";
    public static final java.lang.String REMOVE_CMD = "/remove/";
    public static final String NEEDED_PERM ="upload_doc";

    public void add(String docIds) throws WcampConnectionException, WcampConnectionException, IOException;
    public void remove(String docId);
}
