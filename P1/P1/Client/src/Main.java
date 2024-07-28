import client.ClientTask;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
        int countryNum = Integer.parseInt(args[0]);
        int problemsNum = Integer.parseInt(args[1]);
        int chunkSize = Integer.parseInt(args[2]);
        int delayBetweenBlocksMs = Integer.parseInt(args[3]);

        long start = System.nanoTime();

        new ClientTask(countryNum, problemsNum, 2, chunkSize, delayBetweenBlocksMs).run();

        long end = System.nanoTime();
        double a = (double) (end - start) / 1E6;

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("output/client" + countryNum, true));
            writer.write(String.valueOf(a));
            writer.write('\n');
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        double[] sums = new double[24];
//        for (int i = 1; i <= 5; i++) {
//            try {
//                Scanner scanner = new Scanner(new FileReader("output/client" + i));
//                for (int j = 0; j < 24; j++) {
//                    sums[j] += scanner.nextDouble();
//                }
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        for (double sum : sums) {
//            System.out.println(sum / 5);
//        }
    }
}



//import client.ClientTask;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//public class Main {
//    public static void main(String[] args) {
//        int countriesNum = Integer.parseInt(args[0]);
//        int problemsNum = Integer.parseInt(args[1]);
//        int chunkSize = Integer.parseInt(args[2]);
//        int delayBetweenBlocksMs = Integer.parseInt(args[3]);
//
//        long start = System.nanoTime();
//        var executor = Executors.newFixedThreadPool(countriesNum);
//        for (int i = 1; i <= countriesNum; i++) {
//            executor.execute(new ClientTask(i, problemsNum, 2, chunkSize, delayBetweenBlocksMs));
//        }
//
//        executor.shutdown();
//        try {
//            var success = executor.awaitTermination(10, TimeUnit.MINUTES);
////            System.out.println("\nClients finished successfully.");
//            long end = System.nanoTime();
//            System.out.println((double) (end - start) / 1E6);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}