public class ProblemDetails {
    private int n;
    private int m;
    private int k;
    private int[][] matrix;
    private int[][] convMatrix;

    public int computePositionValue(int x, int y, int left, int[] prevRow, int[] nextRow) {
        int sum = 0;

        int leftIndex = y != 0 ? y - 1 : 0;
        int rightIndex = y != m - 1 ? y + 1 : m - 1;

        sum += prevRow[leftIndex] * convMatrix[0][0] + prevRow[y] * convMatrix[0][1] + prevRow[rightIndex] * convMatrix[0][2];
        sum += left * convMatrix[1][0] + matrix[x][y] * convMatrix[1][1] + matrix[x][rightIndex] * convMatrix[1][2];
        sum += nextRow[leftIndex] * convMatrix[2][0] + nextRow[y] * convMatrix[2][1] + nextRow[rightIndex] * convMatrix[2][2];

        return sum;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getConvMatrix() {
        return convMatrix;
    }

    public void setConvMatrix(int[][] convMatrix) {
        this.convMatrix = convMatrix;
    }
}
