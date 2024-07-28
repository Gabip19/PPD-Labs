import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Solution {
    private int p;
    private ProblemDetails problem;
    private String filePath;

    private void readMatrix(int[][] matrix, int n, int m, Scanner scanner) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = scanner.nextInt();
            }
        }
    }

    private void readInput(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(filePath));
        problem = new ProblemDetails();

        var n = scanner.nextInt();
        var m = scanner.nextInt();
        var matrix = new int[n][m];
        readMatrix(matrix, n, m, scanner);

        var k = scanner.nextInt();
        var convMatrix = new int[k][k];
        readMatrix(convMatrix, k, k, scanner);

        problem.setN(n);
        problem.setM(m);
        problem.setMatrix(matrix);
        problem.setK(k);
        problem.setConvMatrix(convMatrix);
    }

    private void writeResultToFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        int n = problem.getN();
        int m = problem.getM();
        var result = problem.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                writer.write(String.valueOf(result[i][j]));
                writer.write(' ');
            }
            writer.newLine();
        }

        writer.close();
    }

    private void compareResultsWithSequential() throws FileNotFoundException {
        int n = problem.getN();
        int m = problem.getM();

        var parallelResult = problem.getMatrix();

        readInput(filePath);
        runSequential();

        var sequentialResult = problem.getMatrix();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (parallelResult[i][j] != sequentialResult[i][j]) {
                    System.err.println("Results do not match.");
                    return;
                }
            }
        }
    }

    private void runSequential() {
        var matrix = problem.getMatrix();
        var n = problem.getN();
        var m = problem.getM();
        int[] prevRow = new int[m];
        int[] lastRow = new int[m];
        int[] nextRow;

        for (int j = 0; j < m; j++) {
            prevRow[j] = matrix[0][j];
            lastRow[j] = matrix[n - 1][j];
        }

        for (int i = 0; i < n; i++) {
            int leftElem = matrix[i][0];
            nextRow = i == n - 1 ? lastRow : matrix[i + 1];

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

    private void runOnRows() throws InterruptedException {
        Thread[] threads = new Thread[p];
        int rowsPerThread = problem.getN() / p;
        int remainingRows = problem.getN() % p;
        int start = 0;
        int end;

        var barrier = new CyclicBarrier(p);

        for (int i = 0; i < p; i++) {
            int currentRowsPerThread = rowsPerThread;

            if (remainingRows != 0) {
                remainingRows--;
                currentRowsPerThread++;
            }
            end = start + currentRowsPerThread;

            threads[i] = new RowWorkerThread(problem, start, end, barrier);
            threads[i].start();

            start += currentRowsPerThread;
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }
    }

    public void Solve(String[] args) throws IOException, InterruptedException {
        p = Integer.parseInt(args[0]);
        int fileNumber = Integer.parseInt(args[1]);
        int runOption = Integer.parseInt(args[2]);
        int checkResult = Integer.parseInt(args[3]);

        filePath = "input" + fileNumber + ".txt";

        readInput(filePath);

        switch (runOption) {
            case 0 -> {
                long start = System.nanoTime();
                runSequential();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
            case 1 -> {
                long start = System.nanoTime();
                runOnRows();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
        }

        if (checkResult == 1) {
            writeResultToFile();
            compareResultsWithSequential();
        }
    }
}
