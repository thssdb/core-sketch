import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;

import java.io.IOException;

import static utils.FileHelper.READ;

public class Main {
    public static void main(String[] args) throws IOException {
        double[] data = READ("/Users/howardguan/Documents/THU/DQ/" +
                        "Query/CORE-Sketch/dataset/bitcoin.csv",
                10000000, 10000000);
//        Random r = new Random();
//        double[] data = new double[1000000];
//        double mu = 4;
//        double sigma = 2;
        double minimum = 100000;
        double maximum = -100000;
        for (int i = 0; i < data.length; i++) {
//             data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
            // data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
            if (data[i] < minimum) minimum = data[i];
            if (data[i] > maximum) maximum = data[i];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] - minimum + 1;
        }
        double time_exact = System.nanoTime();
        double exact = EXACT_MAD.exact_mad(data, data.length)[0];
        time_exact = System.nanoTime() - time_exact;
        System.out.println("Exact");
        double time_core_ori = System.nanoTime();
        double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                1, 1000, false, false)[0];
        time_core_ori = System.nanoTime() - time_core_ori;
        System.out.println("Ori");
        double time_core_opt = System.nanoTime();
        double core_opt = CORE_MAD.core_mad(data, maximum - minimum + 1,
                1, 1000, false, true)[0];
        time_core_opt = System.nanoTime() - time_core_opt;
//        double time_dd = System.nanoTime();
//        double dd = DD_MAD.dd_mad(data, maximum - minimum + 1,
//                1, 0.1, 500, false);
//        time_dd = System.nanoTime() - time_dd;
        System.out.println("EXACT_TIME: " + time_exact);
        System.out.println("CORE_TIME: " + time_core_ori);
        System.out.println("OPT_CORE_TIME: " + time_core_opt);
//        System.out.println("DD_TIME: " + time_dd);
        System.out.println("EXACT_MAD: " + exact);
        System.out.println("CORE_MAD: " + core_opt);
//        System.out.println("DD_MAD: " + dd);
    }
}