package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import mad.CORESketch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class SpaceLimitExp {
    static int size=(int)1e8,TEST_CASE=32,GC_SLEEP=0;//2000;
    static HashMap<String, LongArrayList> all_time_record = new HashMap<>();
    static int[] bucketList = new int[]{11000,12000,13000,14000,15000,16000,17000,18000,19000,20000};

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
        }System.out.println();
    }


    public static void old_test()throws IOException{

        long ALL_ST = new Date().getTime();
        // double[] nums = READ("E:/dataset/bitcoin.csv", 296128541, 296128541);
        Random r = new Random();
        int[] spaces = new int[] {1000, 2000, 3000, 4000, 5000, 6000};
//        int[] spaces = new int[] {2000, 3000, 4000, 5000, 6000, 7000};
        double mu = 4;
        double sigma = 2;
        double minimum = 100000;
        double maximum = -100000;
//         [] data = READ("E:\\MAD-data\\norm1.csv",100000000, 100000000);
//        double[] data = READ("E:\\MAD-data\\chi1.csv",100000000, 100000000);
        double[] data = READ("E:\\MAD-data\\pareto1.csv",100000000, 100000000);
        for (int i = 0; i < data.length; i++) {
//                data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
//                data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
            if (data[i] < minimum) minimum = data[i];
            if (data[i] > maximum) maximum = data[i];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] - minimum + 1;
        }
        double times = 4;
        double spaces_core;
        double spaces_dd;
        double spaces_exact;
        double times_core;
        double times_dd;
        double times_exact;
        for (int space : spaces) {
            spaces_core = 0;
            spaces_exact = 0;
            times_core = 0;
            times_exact = 0;
            CORESketch sketch = new CORESketch();
//            System.out.println("Space Limit: " + space);
            for (int T = 0; T < times; T++) {

//                if(space==spaces[0])
//                {
//                    double time_exact = System.nanoTime();
//                    double exact = EXACT_MAD.exact_mad(data, data.length);
//                    times_exact += (System.nanoTime() - time_exact) / 1000000;
//                    spaces_exact += exact;
//                }

                double time_core = System.nanoTime();
                double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                    1, space, false, true)[0];
                times_core += (System.nanoTime() - time_core) / 1000000;
                spaces_core += core;
            }
            System.out.print("memory_limit:" + space);
            System.out.print("\tSPACE:\t" + (spaces_core / times) + "\t" + (spaces_exact / times));
            System.out.print("\tTIME:\t" + (times_core / times) + "\t" + (times_exact / times));
            System.out.println();
        }
        System.out.println("\n\nALL_TIME:"+(new Date().getTime()-ALL_ST));
    }

    public static void test_separate_CORE(int[] bucketList) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (String dataset_name : new String[]{/*"norm1", "chi1", */"pareto1"}) {
            long[] time = new long[bucketList.length];
            double[] space = new double[bucketList.length];

            for (int T = 0; T < TEST_CASE; T++) {
                double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size*2);
                double minimum = 100000,maximum = -100000;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }

                for(int bucketID=0;bucketID<bucketList.length;bucketID++) {
                    try {
                        System.gc();
                        Thread.sleep(GC_SLEEP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long tmp_time = new Date().getTime();
                    space[bucketID]+=CORE_MAD.core_mad(data, maximum - minimum + 1, 1, bucketList[bucketID], false, true)[0];
                    time[bucketID] += (new Date().getTime() - tmp_time);
                    add_record("core_" + dataset_name + "_" + bucketList[bucketID], new Date().getTime() - tmp_time);
                }
            }
            for(int bucketID=0;bucketID<bucketList.length;bucketID++)
                System.out.println("data:" + dataset_name + "\t|bucket|:\t" + bucketList[bucketID] + "\t\t" + "TIME:\t" + "\t" + time[bucketID] / TEST_CASE+"\t\tSPACE:\t"+space[bucketID]/TEST_CASE);
            System.out.println();
        }
        show_records();
    }

    public static void test_separate_DD(int[] bucketList) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
            long[] time = new long[bucketList.length];
            double[] space = new double[bucketList.length];
            double[] relErr_dd = new double[bucketList.length];

            for (int T = 0; T < TEST_CASE; T++) {
                double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size * 2);
                double minimum = 100000,maximum = -100000;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }
                try {
                    System.gc();
                    Thread.sleep(GC_SLEEP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                double exact_mad_v = EXACT_MAD.exact_mad(data, size)[1];

                System.out.print("case" + T + "\t\t\t\ttmpErr:\t");
                for (int bucketID = 0; bucketID < bucketList.length; bucketID++) {
                    try {
                        System.gc();
                        Thread.sleep(GC_SLEEP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long tmp_time = new Date().getTime();
                    double[] approx_mad_v = DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, bucketList[bucketID]);
                    time[bucketID] += (new Date().getTime() - tmp_time);
                    space[bucketID]+=DD_MAD.memory;
                    add_record("DD_" + dataset_name + "_" + bucketList[bucketID], new Date().getTime() - tmp_time);
                    relErr_dd[bucketID] += Math.abs(approx_mad_v[1] - exact_mad_v) / exact_mad_v;
                    System.out.print("\t" + (Math.abs(approx_mad_v[1] - exact_mad_v) / exact_mad_v));
                }
                System.out.println();
            }
            for (int bucketID = 0; bucketID < bucketList.length; bucketID++)
                System.out.println("data:" + dataset_name + "\t|bucket|:\t" + bucketList[bucketID] + "\t\t" + "TIME:\t" + "\t" + time[bucketID] / TEST_CASE + "\t\tREL_ERR:\t" + relErr_dd[bucketID] / TEST_CASE+"\t\tSPACE:\t"+space[bucketID]/TEST_CASE);
            System.out.println();
        }
//        show_records();
    }

    public static void main(String[] args) throws IOException {
        long MAIN_ALL_TIME = new Date().getTime();
//        test_separate_CORE(bucketList);
        test_separate_DD(bucketList);
        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(new Date().getTime()-MAIN_ALL_TIME));
    }
}
