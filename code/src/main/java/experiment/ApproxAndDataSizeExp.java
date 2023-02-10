package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;
import java.util.*;

import static utils.FileHelper.READ;

public class ApproxAndDataSizeExp {
    static int sizeMin=(int)1e8,TEST_CASE=8,SPACE_LIMIT=20000,GC_SLEEP_BASE=28000;
    static HashMap<String,LongArrayList> all_time_record = new HashMap<>();

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

    public static void test_together() throws IOException {
        long ALL_ST = new Date().getTime();

        for (int mmpSize = sizeMin * 6; mmpSize <= sizeMin * 10; mmpSize += sizeMin) {
            String size_name = mmpSize==sizeMin*10?String.valueOf(mmpSize):('0'+String.valueOf(mmpSize));
            System.out.println();
            for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"}) {
                double minimum = 100000;
                double maximum = -100000;
                long times_exact,times_core,times_dd;

                double relErr_core,space_core,space_exact;
                double relErr_dd;

                for (int size = mmpSize; size <= mmpSize; size += mmpSize) {
                    times_exact=0;space_exact=0;
                    times_core = 0;relErr_core =space_core= 0;
                    times_dd = 0;relErr_dd = 0;

                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    try {
                        System.gc();
//                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double exact_mad_v = EXACT_MAD.exact_mad(data, size)[1];
                    CORE_MAD.core_mad(data, maximum - minimum + 1,1, SPACE_LIMIT, false, true);
                    DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);

                    for (int T = 0; T < TEST_CASE; T++) {
                        double old_space_exact = space_exact,old_space_core=space_core,old_relErr_dd=relErr_dd;
                        long old_times_exact=times_exact,old_times_core=times_core,old_times_dd=times_dd;
                        try {
//                            data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size * 2);
//                            for (int i = 0; i < data.length; i++) {
////                        data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
//                                if (data[i] < minimum) minimum = data[i];
//                                if (data[i] > maximum) maximum = data[i];
//                            }
//                            for (int i = 0; i < data.length; i++) {
//                                data[i] = data[i] - minimum + 1;
//                            }

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_exact = new Date().getTime();
                            double[] exact_result = EXACT_MAD.exact_mad(data, size);
                            space_exact += exact_result[0];
                            times_exact += (new Date().getTime() - time_exact);
                            add_record("exact_"+dataset_name+"_"+size_name,new Date().getTime() - time_exact);
                            exact_mad_v = exact_result[1];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_dd = new Date().getTime();
                            double dd_mad = DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                            times_dd += (new Date().getTime() - time_dd);
                            add_record("dd_"+dataset_name+"_"+size_name,new Date().getTime() - time_dd);
                            relErr_dd += Math.abs(dd_mad - exact_mad_v) / exact_mad_v;

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_core = new Date().getTime();
                            double[] core_result = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                1, SPACE_LIMIT, false, true);
                            double core_mad = core_result[1];
                            space_core += core_result[0];
                            times_core += (new Date().getTime() - time_core);
                            add_record("core_"+dataset_name+"_"+size_name,new Date().getTime() - time_core);
                            relErr_core += Math.abs(core_mad - exact_mad_v) / exact_mad_v;
                        } catch (Exception e){
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            T--;
                            space_exact=old_space_exact;
                            space_core=old_space_core;
                            times_exact=old_times_exact;
                            times_core=old_times_core;
                            times_dd=old_times_dd;
                            relErr_dd=old_relErr_dd;
                        }
                    }
                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core/ TEST_CASE) + "\t" + (times_dd/ TEST_CASE) + "\t"+times_exact/TEST_CASE
                        +"\t\trel Err:\t" + /*relErr_core / times + "\t" + */relErr_dd / TEST_CASE
                        +"\tSpace:\t"+space_core/TEST_CASE+"\t"+space_exact/TEST_CASE);

                }
            }
            show_records();
        }
        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
        show_records();
    }

    public static void test_separate_DD(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (int mmpSize = sizeMin * sizeL; mmpSize <= sizeMin * sizeR; mmpSize += sizeMin) {
            int GC_SLEEP = GC_SLEEP_BASE*(mmpSize/sizeMin)/10;
            String size_name = mmpSize == sizeMin * 10 ? String.valueOf(mmpSize) : ('0' + String.valueOf(mmpSize));
            System.out.println();
            for (String dataset_name : new String[]{"norm1"/*, "chi1", "pareto1"*/})
                for (int size = mmpSize; size <= mmpSize; size += mmpSize) {
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    double minimum = 100000,maximum = -100000;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    long time = 0;
                    double space=0;
                    for (int T = 0; T < TEST_CASE; T++) {

                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        long tmp_time = new Date().getTime();
                        DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                        time += (new Date().getTime() - tmp_time);
                        space+=DD_MAD.memory;
                        add_record("dd_" + dataset_name + "_" + size_name, new Date().getTime() - tmp_time);
                    }
                    System.out.println("dd data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + time / TEST_CASE+"\tSPACE:\t"+space/TEST_CASE);
                }
        }
//        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
        show_records();
    }
    public static void test_separate_EXACT(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (int size = sizeMin * sizeL; size <= sizeMin * sizeR; size += sizeMin) {
            int GC_SLEEP = GC_SLEEP_BASE*(size/sizeMin)/10;
            String size_name = size == sizeMin * 10 ? String.valueOf(size) : ('0' + String.valueOf(size));
            System.out.println();
            for (String dataset_name : new String[]{/*"norm1", "chi1", */"pareto1"}) {
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    double minimum = 100000,maximum = -100000;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    long times_exact = 0;
                    for (int T = 0; T < TEST_CASE; T++) {

                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        long time_exact = new Date().getTime();
                        EXACT_MAD.exact_mad(data, size);
                        times_exact += (new Date().getTime() - time_exact);//System.out.println("\tt\t\t\t\t\t"+((new Date().getTime() - time_exact)));
                        add_record("exact_" + dataset_name + "_" + size_name, new Date().getTime() - time_exact);
                    }
                    System.out.println("data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + times_exact / TEST_CASE);
                }
            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
        show_records();
    }
    public static void test_separate_CORE(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (int size = sizeMin * sizeL; size <= sizeMin * sizeR; size += sizeMin) {
            int GC_SLEEP = GC_SLEEP_BASE*(size/sizeMin)/10;
            String size_name = size == sizeMin * 10 ? String.valueOf(size) : ('0' + String.valueOf(size));
            System.out.println();
            for (String dataset_name : new String[]{"norm1"/*, "chi1", "pareto1"*/}) {
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    double minimum = 100000,maximum = -100000;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    long time = 0;
                    for (int T = 0; T < TEST_CASE; T++) {

                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        long tmp_time = new Date().getTime();
                        double[] result = CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
//                        System.out.println("\t\t\tspace:\t"+result[0]);
                        time += (new Date().getTime() - tmp_time);
                        // System.out.println("\tt\t\t\t\t\t"+(new Date().getTime() - tmp_time));
                        add_record("core_" + dataset_name + "_" + size_name, new Date().getTime() - tmp_time);
                    }
                    System.out.println("core data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + time / TEST_CASE);
                }
//            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
        show_records();
    }
    public static void main(String[] args) throws IOException{
//        test_together();
        long MAIN_ALL_TIME = new Date().getTime();
//        test_separate_EXACT(9,9);
//        test_separate_CORE(3,3);
        test_separate_DD(4,4);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(new Date().getTime()-MAIN_ALL_TIME));
    }
}
