package experiment;

import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;

import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class DispersionExp {
    public static void main(String[] args) throws IOException {
        Random r = new Random();
        double[] stds = new double[] {0.1, 1, 5, 10, 20};
        double mu = 4;
        double minimum = 100000;
        double maximum = -100000;
        double times = 10;
        double spaces_core;
        double spaces_dd;
        double spaces_exact;
        double times_core;
        double times_dd;
        double times_exact;
        for (double std : stds) {
            spaces_core = 0;
            spaces_dd = 0;
            spaces_exact = 0;
            times_core = 0;
            times_dd = 0;
            times_exact = 0;
            System.out.println("Standard Deviation: " + std);
            for (int j = 0; j < times; j++) {
                double data[] = new double[1000000];
                for (int i = 0; i < data.length; i++) {
                    data[i] = Math.pow(std, 2) * r.nextGaussian() + mu;
                    // data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }
                double time_exact = System.nanoTime();
                double exact = EXACT_MAD.exact_mad(data, data.length)[0];
                times_exact += (System.nanoTime() - time_exact) / 1000000;
                spaces_exact += exact;
                double time_core = System.nanoTime();
                double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                        1, 1000, false, true)[0];
                times_core += (System.nanoTime() - time_core) / 1000000;
                spaces_core += core;
            }
            System.out.println("SPACE: " + (spaces_core / times) + " " + (spaces_dd / times) + " " + (spaces_exact / times));
            System.out.println("TIME: " + (times_core / times) + " " + (times_dd / times) + " " + (times_exact / times));
//            System.out.println("SPACE: " + (spaces_core / times) + " " +  (spaces_exact / times));
//            System.out.println("TIME: " + (times_core / times) + " " + (times_exact / times));
        }
    }
}
