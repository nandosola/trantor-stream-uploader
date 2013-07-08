package cc.abstra.trantor.wcamp;


import java.io.IOException;

public interface WorklistResource {

    public static final String WORKLIST_PATH = "/pendingdocuments";
    public static final String ADD_CMD = "/add/";
    public static final String REMOVE_CMD = "/remove/";

    public void addToWorklist(String docIds) throws IOException;
    public void removeFromWorklist(String docId);
}
