import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    private static void writeNewMatrix(int n, int m, int maxValue, BufferedWriter writer) throws IOException {
        Random random = new Random();

        writer.write(String.valueOf(m));
        writer.newLine();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                var value = random.nextInt(maxValue);
                writer.write(String.valueOf(value));
                writer.write(' ');
            }
            writer.newLine();
        }
    }
    private static void generateInputFile(int n, int m, int k, int maxValue, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        writer.write(String.valueOf(n));
        writer.write(' ');
        writeNewMatrix(n, m, maxValue, writer);
        writeNewMatrix(k, k, maxValue, writer);

        writer.close();
    }

    public static void main(String[] args) {
        try {
//            generateInputFile(10000, 10000, 3, 1000, "input4.txt");
            new Solution().Solve(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}