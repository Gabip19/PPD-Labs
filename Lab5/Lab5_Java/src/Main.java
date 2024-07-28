import solution.Solution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class Main extends Solution {
    public static void main(String[] args) {
        try {
            Solution.Solve(args);
//            generateInputs(2, 5, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateInputs(int setNum, int countriesNum, int problemsNum) throws IOException {
        Random random = new Random();
        var id = 0;

        for (int i = 1; i <= countriesNum; i++) {
            var participantsNum = 80 + random.nextInt(21);

            var idsList = new ArrayList<Integer>();
            for (int j = 0; j < participantsNum; j++) {
                idsList.add(id + j);
            }

            id += participantsNum;

            for (int j = 1; j <= problemsNum; j++) {
                Collections.shuffle(idsList);

                var fileName = "input\\set" + setNum + "\\RezultateC" + i + "_P" + j + ".txt";
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

                for (var entryId : idsList) {
                    var score = 1 + random.nextInt(116);
                    if (100 < score && score < 111) {
                        continue;
                    } else if (score > 110) {
                        score = -1;
                    }

                    writer.write(String.valueOf(entryId));
                    writer.write(' ');
                    writer.write(String.valueOf(score));
                    writer.newLine();
                }

                writer.close();
            }
        }
    }
}