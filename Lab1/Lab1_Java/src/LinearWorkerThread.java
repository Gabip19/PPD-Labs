public class LinearWorkerThread extends Thread {
    private final ProblemDetails problem;
    private final int start;
    private final int end;

    public LinearWorkerThread(ProblemDetails problem, int start, int end) {
        this.problem = problem;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        int m = problem.getM();

        for (int i = start; i < end; i++) {
            int row = i / m;
            int column = i % m;
            problem.updatePosition(row, column);
        }
    }
}
