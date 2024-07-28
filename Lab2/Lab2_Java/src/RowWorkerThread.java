import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class RowWorkerThread extends Thread {
    private final ProblemDetails problem;
    private final int startRow;
    private final int endRow;
    private final CyclicBarrier barrier;

    private int[] prevRow;
    private int[] lastRow;

    public RowWorkerThread(ProblemDetails problem, int startRow, int endRow, CyclicBarrier barrier) {
        this.problem = problem;
        this.startRow = startRow;
        this.endRow = endRow;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        var matrix = problem.getMatrix();
        var m = problem.getM();

        initCache();
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

        for (int i = startRow; i < endRow; i++) {
            int leftElem = matrix[i][0];
            int[] nextRow = i == endRow - 1 ? lastRow : matrix[i + 1];

            for (int j = 0; j < m; j++) {
                int result = problem.computePositionValue(i, j, leftElem, prevRow, nextRow);

                if (j > 0) {
                    prevRow[j - 1] = leftElem;
                }

                leftElem = matrix[i][j];
                matrix[i][j] = result;
            }

            prevRow[m - 1] = leftElem;
        }
    }

    private void initCache() {
        var n = problem.getN();
        var m = problem.getM();
        var matrix = problem.getMatrix();

        prevRow = new int[m];
        lastRow = new int[m];
        int prevRowIndex = startRow == 0 ? startRow : startRow - 1;
        int lastRowIndex = endRow == n ? n - 1 : endRow;

        for (int j = 0; j < m; j++) {
            prevRow[j] = matrix[prevRowIndex][j];
            lastRow[j] = matrix[lastRowIndex][j];
        }
    }
}
