public class CyclicWorkerThread extends Thread {
    private final ProblemDetails problem;
    private final int start;
    private final int step;

    public CyclicWorkerThread(ProblemDetails problem, int start, int step) {
        this.problem = problem;
        this.start = start;
        this.step = step;
    }

    @Override
    public void run() {
        int totalElemsNum = problem.getN() * problem.getM();
        int m = problem.getM();

        for (int i = start; i < totalElemsNum; i += step) {
            int row = i / m;
            int column = i % m;
            problem.updatePosition(row, column);
        }
    }
}
