package cc.abstra.trantor.wcamp.exceptions;

public class MissingClientHeadersException extends Exception {

    public MissingClientHeadersException(String headers) {
        super("Make sure the header(s) "+ headers +" are set and have a correct value!");
    }
}
