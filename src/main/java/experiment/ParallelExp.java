package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import mad.CORESketch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static utils.FileHelper.READ;

public class ParallelExp {
    static int TEST_SIZE=(int)1e9,TEST_CASE=10,SPACE_LIMIT=20000;
    static HashMap<String, LongArrayList> all_time_record = new HashMap<>();



    public static void add_record(String name,long time){
        LongArrayList times = all_time_record.getOrDefault(name,new LongArrayList());
        times.add(time);
        times.sort(Long::compare);
        all_time_record.put(name,times);
    }

    public static void show_records(){
        ArrayList<String> record_k = new ArrayList<>(all_time_record.keySet());
        record_k.sort(CharSequence::compare);
        for(String key: record_k){
            System.out.print("\t\t"+key+":\t\t");
            LongArrayList time_list = all_time_record.get(key);
            for(long t: time_list)
                System.out.print("\t"+t);
            long mid_t = (time_list.getLong((TEST_CASE-1)/2)+time_list.getLong(TEST_CASE/2))/2;
            System.out.print("\t\t\tmid_t:\t"+mid_t);
            System.out.println();
        }
    }

    public static void test_separate() throws IOException {
        long ALL_ST = new Date().getTime();

        for (int MMP = 1; MMP <= 8; MMP++) {
            CORE_MAD.MMP = MMP;
            System.out.println();
            for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
                Random r = new Random();
                double mu = 4;
                double sigma = 2;
                double minimum = 100000;
                double maximum = -100000;
                long times_core;
                double space_core;
                int size = TEST_SIZE;

                double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                for (int i = 0; i < data.length; i++) {
//                        data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }

                times_core = 0;
                space_core = 0;

                CORE_MAD.core_mad_p(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, MMP >= 2);
                CORE_MAD.core_mad_p(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, MMP >= 2);
                for (int T = 0; T < TEST_CASE; T++) {
                    long time_core = new Date().getTime();
                    double[] result=CORE_MAD.core_mad_p(data, maximum - minimum + 1,
                        1, SPACE_LIMIT, false, MMP >= 2);
                    space_core+=result[0];
//                    System.out.println("\t\t\t\t"+result[0]+"\t\t\t"+result[1]);
                    times_core += (new Date().getTime() - time_core);
                    add_record("core_" + dataset_name + "_" + MMP, new Date().getTime() - time_core);
                }
                System.out.println("data:" + dataset_name + "\tTIME:\t" + (times_core / TEST_CASE) + "\tSPACE:\t" + space_core / TEST_CASE);

            }
//            show_records();
        }
        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
        show_records();
    }

    public static void old_test()throws IOException{
        for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
            long ALL_ST = new Date().getTime();
            Random r = new Random();
            double mu = 4;
            double sigma = 2;
            double minimum = 100000;
            double maximum = -100000;
            double times = 8;
            double spaces_core;
            double spaces_dd;
            double spaces_exact;
            double times_core;
            double times_dd;
            double times_exact;
            double[] data = READ("E:\\MAD-data\\"+dataset_name+".csv", 100000000, 100000000);
//            double[] data = READ("E:\\MAD-data\\norm1.csv", 1000000000, 1000000000);
//            double[] data = READ("E:\\MAD-data\\chi1.csv", 100000000, 100000000);
//            double[] data = READ("E:\\MAD-data\\pareto1.csv",100000000, 100000000);
            for (int i = 0; i < data.length; i++) {
//                     data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
//                data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
                if (data[i] < minimum) minimum = data[i];
                if (data[i] > maximum) maximum = data[i];
            }
            for (int i = 0; i < data.length; i++) {
                data[i] = data[i] - minimum + 1;
            }
            for (int MMP = 1; MMP <= 8; MMP++) {
                CORE_MAD.MMP = MMP;
                spaces_core = 0;
                spaces_dd = 0;
                spaces_exact = 0;
                times_core = 0;
                times_dd = 0;
                times_exact = 0;
                for (int T = 0; T < times; T++) {
                    CORESketch sketch = new CORESketch();
                    double time_core = System.nanoTime();
//            System.out.println("Start");
                    double core = CORE_MAD.core_mad_p(data, maximum - minimum + 1,
                        1, SPACE_LIMIT, false, MMP >= 2)[0];
                    times_core += (System.nanoTime() - time_core) / 1000000;
                    spaces_core += core;
                }

                System.out.print("thread:" + MMP);
                System.out.print("\tSPACE:\t" + (spaces_core / times) + "\t" + (spaces_exact / times));
                System.out.print("\tTIME:\t" + (times_core / times) + "\t" + (times_exact / times));
                System.out.println();
                //            System.out.println("SPACE: " + (spaces_core / times) + " " +  (spaces_exact / times));
                //            System.out.println("TIME: " + (times_core / times) + " " + (times_exact / times));
            }
            System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST)+"\n\n");
        }
    }


    public static void main(String[] args) throws IOException {
        test_separate();
    }
}
