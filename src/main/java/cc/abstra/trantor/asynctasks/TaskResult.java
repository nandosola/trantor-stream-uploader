package cc.abstra.trantor.asynctasks;


public class TaskResult {

    private TaskResult() {
    }

    protected static TaskResult generate(boolean status) {
        TaskResult result = status ? new SuccessResult() : new FailureResult();
        return result;
    }

    public static class SuccessResult extends TaskResult {
        public SuccessResult() {
        }
    }

    public static class FailureResult extends TaskResult {
        public FailureResult() {
        }
    }

}
