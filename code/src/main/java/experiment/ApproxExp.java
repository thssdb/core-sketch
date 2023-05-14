package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import static utils.FileHelper.READ;

public class ApproxExp {
    static int sizeMin=(int)1e8;
    public static void main(String[] args) throws IOException {
        long ALL_ST = new Date().getTime();
        for (int mmpSize = sizeMin * 10; mmpSize <= sizeMin * 10; mmpSize += sizeMin) {
            System.out.println();
            for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
                Random r = new Random();
                double mu = 4;
                double sigma = 2;
                double minimum = 100000;
                double maximum = -100000;
                double times = 6;
                double times_core, relErr_core;
                double times_dd, relErr_dd;

                for (int size = mmpSize; size <= mmpSize; size += mmpSize) {
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    times_core = relErr_core = 0;
                    times_dd = relErr_dd = 0;

                    for (int i = 0; i < data.length; i++) {
                        data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
//                        data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    double exact_mad_v = EXACT_MAD.exact_mad(data, size)[1];

                    for (int i = 0; i < times; i++) {

                        double time_core = System.nanoTime();
                        double core_mad = CORE_MAD.core_mad(data, maximum - minimum + 1,
                            1, 5000, false, true)[1];
                        times_core += (System.nanoTime() - time_core) / 1000000;
                        relErr_core += Math.abs(core_mad - exact_mad_v) / exact_mad_v;

                        double time_dd = System.nanoTime();
                        double dd_mad = DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, 5000)[1];
                        times_dd += (System.nanoTime() - time_dd) / 1000000;
                        relErr_dd += Math.abs(dd_mad - exact_mad_v) / exact_mad_v;
                    }
                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core / times) + "\t" + (times_dd / times) + "\t\trel Err:\t" + relErr_core / times + "\t" + relErr_dd / times);
                }
            }
        }
        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
    }
}
