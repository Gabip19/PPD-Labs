public class ColumnWorkerThread extends Thread {
    private final ProblemDetails problem;
    private final int startColumn;
    private final int endColumn;

    public ColumnWorkerThread(ProblemDetails problem, int startColumn, int endColumn) {
        this.problem = problem;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    @Override
    public void run() {
        var n = problem.getN();

        for (int j = startColumn; j < endColumn; j++) {
            for (int i = 0; i < n; i++) {
                problem.updatePosition(i, j);
            }
        }
    }
}
