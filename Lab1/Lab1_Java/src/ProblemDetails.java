public class ProblemDetails {
    private int n;
    private int m;
    private int k;
    private int[][] matrix;
    private int[][] convMatrix;
    private int[][] result;

    public void updatePosition(int x, int y) {
        int sum = 0;
        int middle = k / 2;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int ii = x - middle + i;
                int jj = y - middle + j;

                ii = Math.max(ii, 0);
                ii = Math.min(ii, n - 1);

                jj = Math.max(jj, 0);
                jj = Math.min(jj, m - 1);

                sum += convMatrix[i][j] * matrix[ii][jj];
            }
        }
        result[x][y] = sum;
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

    public int[][] getResult() {
        return result;
    }

    public void setResult(int[][] result) {
        this.result = result;
    }
}
