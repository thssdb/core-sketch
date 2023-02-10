package experiment;

import benchmark.CORE_MAD;
import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import mad.CORESketch;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class DataSizeExp {
//    static int sizeMin=(int)1e9,sizeMax=(int)1e9,sizeDelta = (int)1e9;//,sizeCount=(sizeMax-sizeMin)/sizeDelta+1;
    static int sizeMin=(int)1e8,sizeMax=(int)5e8,sizeDelta = (int)1e9;//,sizeCount=(sizeMax-sizeMin)/sizeDelta+1;
    public static void main(String[] args) throws IOException {

        for (int mmpSize = sizeMin*10; mmpSize <= sizeMin*10; mmpSize += sizeMin) {
            for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
//            System.out.println("initial\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);
                long ALL_ST = new Date().getTime();
                Random r = new Random();
//        int[] sizes = new int[] {10000};
                double mu = 4;
                double sigma = 2;
                double T_opt = 6, T_exact = 8;
                double spaces_core;
                double spaces_dd;
                double spaces_exact;
                double times_core;
                double times_dd;
                double times_exact;
//            double[] all_data = READ("E:\\MAD-data\\" + dataset_name + ".csv", sizeMax, sizeMax);
//            double[] all_data = new double[sizeMax];
//            for(int i=0;i<sizeMax;i++)all_data[i]=i;

//                for (int size = sizeMin; size <= sizeMax; size += sizeDelta)
                for (int size = mmpSize; size <= mmpSize; size += mmpSize) {
//                double[] data = Arrays.copyOf(all_data, size);
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
//                double[] data = new double[size];
//                for(int i=0;i<size;i++)data[i]=(i%(size/1000))/20.0;
//                System.out.println("after read.\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);
                    spaces_core = 0;
                    spaces_dd = 0;
                    spaces_exact = 0;
                    times_core = 0;
                    times_dd = 0;
                    times_exact = 0;
                    CORESketch sketch = new CORESketch();
//            System.out.println("Data Size:\t" + size);
                    for (int T = 0; T < T_opt||T<T_exact; T++) {
//                double[] data = new double[size];
                        double minimum = 100000;
                        double maximum = -100000;
                        for (int i = 0; i < size; i++) {
//                     data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
                            // data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
                            if (data[i] < minimum) minimum = data[i];
                            if (data[i] > maximum) maximum = data[i];
                        }
                        for (int i = 0; i < size; i++) {
                            data[i] = data[i] - minimum + 1;
                        }
//                    System.out.println("after data[i] = data[i] - minimum + 1.\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);

                        if (T < T_exact/* && size <= sizeMin * 3*/) {
                            double time_exact = System.nanoTime();
                            double exact = EXACT_MAD.exact_mad(data, size)[0];
//                        System.out.println("\t\t\t\t\t\t\t\t\t\tsingle_time_exact:" + ((System.nanoTime() - time_exact) / 1000000));
                            if(T>0)times_exact += (System.nanoTime() - time_exact) / 1000000;
                            spaces_exact += exact;
                        }
                        if(T<T_opt) {
                            double time_core = System.nanoTime();
//                System.out.println("\nStart");
                            double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                1, 5000, false, true)[0];
//                    System.out.println("\t\t\t\t\t\t\t\t\t\tsingle_time_core:" + ((System.nanoTime() - time_core) / 1000000));
                            times_core += (System.nanoTime() - time_core) / 1000000;
                            spaces_core += core;
                        }
                    }
                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t" + (spaces_core / T_opt) + "\t" + (spaces_exact / T_exact) + "\t\t" + (times_core / (T_opt-1)) + "\t" + (times_exact / T_exact));
                }
            }
        }
    }
}
