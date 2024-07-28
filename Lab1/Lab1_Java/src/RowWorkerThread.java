public class RowWorkerThread extends Thread {
    private final ProblemDetails problem;
    private final int startRow;
    private final int endRow;

    public RowWorkerThread(ProblemDetails problem, int startRow, int endRow) {
        this.problem = problem;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        var m = problem.getM();

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < m; j++) {
                problem.updatePosition(i, j);
            }
        }
    }
}
