import java.io.*;
import java.util.Scanner;

public class Solution {
    private int p;
    private ProblemDetails problem;

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

        var result = new int[n][m];

        problem.setN(n);
        problem.setM(m);
        problem.setMatrix(matrix);
        problem.setK(k);
        problem.setConvMatrix(convMatrix);
        problem.setResult(result);
    }

    private void writeResultToFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        int n = problem.getN();
        int m = problem.getM();
        var result = problem.getResult();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                writer.write(String.valueOf(result[i][j]));
                writer.write(' ');
            }
            writer.newLine();
        }

        writer.close();
    }

    private void runSequential() {
        for (int i = 0; i < problem.getN(); i++) {
            for (int j = 0; j < problem.getM(); j++) {
                problem.updatePosition(i, j);
            }
        }
    }

    private void compareResultsWithSequential() {
        int n = problem.getN();
        int m = problem.getM();

        var parallelResult = problem.getResult();

        problem.setResult(new int[n][m]);
        runSequential();
        var sequentialResult = problem.getResult();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (parallelResult[i][j] != sequentialResult[i][j]) {
                    System.err.println("Results do not match.");
                    return;
                }
            }
        }
    }

    private void runOnRows() throws InterruptedException {
        Thread[] threads = new Thread[p];
        int rowsPerThread = problem.getN() / p;
        int remainingRows = problem.getN() % p;
        int start = 0;
        int end;

        for (int i = 0; i < p; i++) {
            int currentRowsPerThread = rowsPerThread;

            if (remainingRows != 0) {
                remainingRows--;
                currentRowsPerThread++;
            }
            end = start + currentRowsPerThread;

            threads[i] = new RowWorkerThread(problem, start, end);
            threads[i].start();

            start += currentRowsPerThread;
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }
    }

    private void runOnColumns() throws InterruptedException {
        Thread[] threads = new Thread[p];
        int columnsPerThread = problem.getM() / p;
        int remainingColumns = problem.getM() % p;
        int start = 0;
        int end;

        for (int i = 0; i < p; i++) {
            int currentColumnsPerThread = columnsPerThread;

            if (remainingColumns != 0) {
                remainingColumns--;
                currentColumnsPerThread++;
            }
            end = start + currentColumnsPerThread;

            threads[i] = new ColumnWorkerThread(problem, start, end);
            threads[i].start();

            start += currentColumnsPerThread;
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }
    }

    private void runLinearDistribution() throws InterruptedException {
        Thread[] threads = new Thread[p];
        int totalNumOfElems = problem.getN() * problem.getM();
        int elemsPerThread = totalNumOfElems / p;
        int remainingElems = totalNumOfElems % p;
        int start = 0;
        int end;

        for (int i = 0; i < p; i++) {
            int currentElemsPerThread = elemsPerThread;

            if (remainingElems != 0) {
                remainingElems--;
                currentElemsPerThread++;
            }
            end = start + currentElemsPerThread;

            threads[i] = new LinearWorkerThread(problem, start, end);
            threads[i].start();

            start += currentElemsPerThread;
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }
    }

    private void runCyclicDistribution() throws InterruptedException {
        Thread[] threads = new Thread[p];

        for (int i = 0; i < p; i++) {
            threads[i] = new CyclicWorkerThread(problem, i, p);
            threads[i].start();
        }

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }
    }

    public void Solve(String[] args) throws InterruptedException, IOException {
        p = Integer.parseInt(args[0]);
        int fileNumber = Integer.parseInt(args[1]);
        int runOption = Integer.parseInt(args[2]);
        int checkResult = Integer.parseInt(args[3]);
        String filePath = "input" + fileNumber + ".txt";

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
            case 2 -> {
                long start = System.nanoTime();
                runOnColumns();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
            case 3 -> {
                long start = System.nanoTime();
                runLinearDistribution();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
            case 4 -> {
                long start = System.nanoTime();
                runCyclicDistribution();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
        }

        if (checkResult == 1) {
            compareResultsWithSequential();
            writeResultToFile();
        }
    }
}
