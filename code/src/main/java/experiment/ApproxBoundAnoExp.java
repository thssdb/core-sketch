package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.TP_MAD;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static utils.FileHelper.READ;

public class ApproxBoundAnoExp {
    static int DROP_COUNT = 4;
    static int TEST_CASE = 8, GC_SLEEP_BASE = 0;
    static HashMap<String, Integer> SIZES = new HashMap<>() {{
        put("bitcoin-s", (int) 1e7);
        put("gas-s", (int) 1e7);
        put("power-s", (int) 1e7);
        put("norm1", (int) 1e9);
        put("pareto1", (int) 1e9);
        put("chi1", (int) 1e7);
    }};
    static HashMap<String, Integer> BUCKETS = new HashMap<>() {{
//        put("bitcoin-s", 100);
//        put("gas-s", 100);
//        put("power-s", 100);
        put("bitcoin-s", 2000);
        put("gas-s", 2000);
        put("power-s", 2000);
        put("norm1", 2000);
        put("pareto1", 2000);
        put("chi1", 2000);
    }};

    static HashMap<String, Double> KS = new HashMap<>() {{
        put("norm1", (double) 4);
        put("pareto1", (double) 100.0);
        put("chi1", (double) 10.0);
        put("bitcoin-s", (double) 20000.0);
        put("gas-s", (double) 3.5);
        put("power-s", (double) 15.0);
    }};
    //    static double[] epsilons = new double[]{1e-5,5e-5,1e-4,5e-4,1e-3,5e-3,1e-2,5e-2,1e-1};
    static double[] epsilons = new double[]{1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1};
    static HashMap<String, LongArrayList> all_time_record = new HashMap<>();

    public static void set_testCase(int times) {
        TEST_CASE = times;
    }

    public static void add_record(String name, long time) {
        LongArrayList times = all_time_record.getOrDefault(name, new LongArrayList());
        times.add(time);
        times.sort(Long::compare);
        all_time_record.put(name, times);
    }

    public static void show_records() {
        ArrayList<String> record_k = new ArrayList<>(all_time_record.keySet());
        record_k.sort(CharSequence::compare);
        for (String key : record_k) {
            System.out.print("\t\t" + key + ":\t\t");
            LongArrayList time_list = all_time_record.get(key);
            for (long t : time_list)
                System.out.print("\t" + t);
            long mid_t = (time_list.getLong((TEST_CASE - 1) / 2) + time_list.getLong(TEST_CASE / 2)) / 2;
            System.out.print("\t\t\tmid_t:\t" + mid_t);
            System.out.println();
        }
    }

    public static void test_together() throws IOException {
        long ALL_ST = new Date().getTime();

        for (String dataset_name : new String[]{
//            "bitcoin-s",
//            "gas-s",
//            "power-s",
//            "pareto1",
                "chi1",
//                "norm1"
        }) {
//            for (double ke = 5; ke < 15; ke += 1)
            {
                int size = SIZES.get(dataset_name);
                double k = KS.get(dataset_name);
                int SPACE_LIMIT = BUCKETS.get(dataset_name);
//                double k = ke;
                System.out.println("\nepsilon:\t" + Arrays.toString(epsilons));
                int[] iter_list_core = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                long[] time_list_exact, time_list_core, time_list_core_single, time_list_dd, time_list_tp;
                double[] space_list_exact, space_list_core, space_list_core_single, space_list_dd, space_list_tp;
                double[] err_list_dd, err_list_tp;
                double[] mad_dd, mad_tp, mad_exact, median;
                double[] f1_list_dd, f1_list_tp, pre_list_dd, re_list_dd, pre_list_tp, re_list_tp;
                time_list_exact = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                time_list_core = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                time_list_core_single = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                time_list_dd = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                time_list_tp = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                space_list_exact = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                space_list_core = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                space_list_core_single = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                space_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                space_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                mad_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                mad_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                mad_exact = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                median = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                err_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                err_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                f1_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                f1_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                pre_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                re_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                pre_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                re_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                int index = 0;
                for (double epsilon : epsilons) {
                    String size_name = String.valueOf(epsilon);
                    double minimum = 100000;
                    double maximum = -100000;
                    long times_exact, times_core, times_dd, times_tp, times_core_single;
                    int iter_core;

                    double relErr_core, space_core, space_exact, space_dd, space_tp, space_core_single;
                    double relErr_dd, relErr_tp;

                    times_exact = 0;
                    space_exact = 0;
                    times_core = 0;
                    space_core = 0;
                    relErr_core = 0;
                    space_dd = 0;
                    space_tp = 0;
                    times_dd = 0;
                    relErr_dd = 0;
                    relErr_tp = 0;
                    times_tp = 0;
                    times_core_single = 0;
                    space_core_single = 0;
                    iter_core = 0;

                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, 2 * size);
                    if (dataset_name.contains("s")) {
                        XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom(233);
                        for (int i = 1; i < data.length; i++) {
                            int j = random.nextInt(i);
                            double t = data[i];
                            data[i] = data[j];
                            data[j] = t;
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    try {
//                    Arrays.sort(data);
//                    System.out.println("\tn:\t"+data.length+"\tmid:\t"+data[data.length/2]);
//                    continue;
                        System.gc();
//                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double exact_mad_v = EXACT_MAD.exact_mad(data, size)[1];
                    try {
                        for (int i = 0; i < DROP_COUNT; i++) {
                            DD_MAD.dd_mad(data, epsilon, SPACE_LIMIT);
                            TP_MAD.tp_mad(data, epsilon, SPACE_LIMIT);
                        }
                    } catch (Exception e) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERR\t\t Epsilon(Alpha):\t" + epsilon + "\t\tdataset:\t" + dataset_name);
                    }

//                    long time_exact = new Date().getTime();
//                    double[] exact_result = EXACT_MAD.exact_mad(data, size);
//                    space_exact += exact_result[0] * TEST_CASE;
//                    times_exact += (new Date().getTime() - time_exact) * TEST_CASE;
//                    add_record("exact_" + dataset_name + "_" + size_name, new Date().getTime() - time_exact);
//                    exact_mad_v = exact_result[1];
//                    mad_exact[index] = exact_mad_v;
//                    median[index] = exact_result[2];

//                    if (index==0 && size < 2e7) {
//                        System.out.println("\t\t!!EXACT_MEDIAN:\t" + EXACT_MAD.exact_median(data, size));
//                        CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, false);
//                        CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, false);
//                        long time_core_single = new Date().getTime();
//                        double[] single_result = CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, false);
//                        times_core_single += (new Date().getTime() - time_core_single);
////                        System.out.println("\t\t!!times_core_single:\t" + times_core_single);
//                        CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
//                        CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
//                        long time_core = new Date().getTime();
//                        double[] core_result = CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
//                        times_core += (new Date().getTime() - time_core);
////                        System.out.println("\t\t!!times_core:\t" + times_core);
//                    }
                    for (int T = 0; T < TEST_CASE; T++) {
                        double old_space_exact = space_exact, old_space_dd = space_dd, old_space_tp = space_tp;
                        double old_relErr_dd = relErr_dd, old_relErr_tp = relErr_tp;
                        long old_times_dd = times_dd, old_times_tp = times_tp;
                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_dd = new Date().getTime();
                            double[] dd_result = DD_MAD.dd_mad_givenAlpha(data, epsilon, SPACE_LIMIT);
                            times_dd += (new Date().getTime() - time_dd);
                            space_dd += dd_result[0];
                            add_record("dd_" + dataset_name + "_" + size_name, new Date().getTime() - time_dd);
                            relErr_dd += Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v;
                            mad_dd[index] = dd_result[1];

//                            System.gc();
//                            Thread.sleep(GC_SLEEP_BASE);
//                            long time_tp = new Date().getTime();
//                            double[] tp_result = TP_MAD.tp_mad_givenAlpha(data, epsilon, SPACE_LIMIT);
//                            times_tp += (new Date().getTime() - time_tp);
//                            space_tp += tp_result[0];
//                            add_record("tp_" + dataset_name + "_" + size_name, new Date().getTime() - time_tp);
//                            relErr_tp += Math.abs(tp_result[1] - exact_mad_v) / exact_mad_v;
//                            mad_tp[index] = tp_result[1];
                        } catch (Exception e) {
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERR\t\t Epsilon(Alpha):\t" + epsilon + "\t\tdataset:\t" + dataset_name);
                            e.printStackTrace();
                            System.out.println("\t" + e.toString());
//                        T--;
                            space_exact = old_space_exact;
                            space_dd = old_space_dd;
                            space_tp = old_space_tp;
                            times_dd = old_times_dd;
                            relErr_dd = old_relErr_dd;
                            times_tp = old_times_tp;
                            relErr_tp = old_relErr_tp;
                        }
                    }
                    time_list_exact[index] = times_exact / TEST_CASE;
                    time_list_dd[index] = times_dd / TEST_CASE;
                    time_list_tp[index] = times_tp / TEST_CASE;
                    time_list_core[index] = times_core;
                    time_list_core_single[index] = times_core_single;
                    space_list_exact[index] = space_exact / TEST_CASE;
                    space_list_dd[index] = space_dd / TEST_CASE;
                    space_list_tp[index] = space_tp / TEST_CASE;
                    err_list_dd[index] = relErr_dd / TEST_CASE;
                    err_list_tp[index] = relErr_tp / TEST_CASE;

                    double upper_exact = median[index] + k * mad_exact[index];
                    double lower_exact = median[index] - k * mad_exact[index];
                    double upper_dd = median[index] + k * mad_dd[index];
                    double lower_dd = median[index] - k * mad_dd[index];
                    double upper_tp = median[index] + k * mad_tp[index];
                    double lower_tp = median[index] - k * mad_tp[index];
                    double anomaly_dd = 0;
                    double anomaly_tp = 0;
                    double anomaly_exact = 0;
                    double pre_dd, pre_tp, re_dd, re_tp;
                    for (double datum : data) {
                        if (datum > upper_exact || datum < lower_exact)
                            anomaly_exact += 1;
                        if (datum > upper_dd || datum < lower_dd)
                            anomaly_dd += 1;
                        if (datum > upper_tp || datum < lower_tp)
                            anomaly_tp += 1;
                    }
//                    if (epsilon == 0.01) {
//                        System.out.println(anomaly_exact);
//                    }
//                    if (anomaly_exact == 0) {
//                        System.out.println(dataset_name + epsilon + " anomaly_exact=0");
//                        continue;
//                    }
                    pre_list_dd[index] = Math.min(1, anomaly_exact / anomaly_dd);
                    re_list_dd[index] = Math.min(1, anomaly_dd / anomaly_exact);
                    pre_list_tp[index] = Math.min(1, anomaly_exact / anomaly_tp);
                    re_list_tp[index] = Math.min(1, anomaly_tp / anomaly_exact);
                    if (anomaly_dd == 0) {
                        pre_list_dd[index] = 0;
                    }
                    if (anomaly_tp == 0) {
                        pre_list_tp[index] = 0;
                    }
                    f1_list_dd[index] = (2 * pre_list_dd[index] * re_list_dd[index]) / (pre_list_dd[index] + re_list_dd[index]);
                    f1_list_tp[index] = (2 * pre_list_tp[index] * re_list_tp[index]) / (pre_list_tp[index] + re_list_tp[index]);
                    if (pre_list_dd[index] + re_list_dd[index] == 0) {
                        f1_list_dd[index] = 0;
                    }
                    if (pre_list_tp[index] + re_list_tp[index] == 0) {
                        f1_list_tp[index] = 0;
                    }

                    index += 1;
//                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core_single/ TEST_CASE) + "\t" + (times_core/ TEST_CASE) + "\t" + (times_dd/ TEST_CASE) + "\t" + (times_tp/ TEST_CASE) + "\t"+times_exact/TEST_CASE
//                        +"\t\trel Err:\t" + relErr_tp / TEST_CASE + "\t" + relErr_dd / TEST_CASE
//                        +"\tSpace:\t"+space_core_single/TEST_CASE+"\t"+space_core/TEST_CASE+"\t"+space_dd/TEST_CASE+"\t"+space_tp/TEST_CASE+"\t"+space_exact/TEST_CASE);
                }
            System.out.println(dataset_name + "(time ms)\texact\tdd\ttp\tcore\tcore_single");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + time_list_exact[j] + "\t" + time_list_dd[j] + "\t" + time_list_tp[j]+ "\t" + time_list_core[0] + "\t" + time_list_core_single[0] );
            }
            System.out.println(dataset_name + "(space)\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + space_list_exact[j] + "\t" + space_list_dd[j] + "\t" + space_list_tp[j]);
            }
                System.out.println(dataset_name + "\texact dd tp");
                for (int j = 0; j < index; j++) {
                    System.out.println((j + 1) + "\t" + 0 + "\t" + err_list_dd[j] + "\t" + err_list_tp[j]);
                }
            System.out.println(dataset_name + "(mad)\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + mad_exact[j] + "\t" + mad_dd[j] + "\t" + mad_tp[j]);
            }
            System.out.println(dataset_name + "(pre)\texact core dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 1 + "\t" + pre_list_dd[j] + "\t" + pre_list_tp[j]);
            }
            System.out.println(dataset_name + "(re)\texact core dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 1 + "\t" + re_list_dd[j] + "\t" + re_list_tp[j]);
            }
            System.out.println(dataset_name + "(f1)\texact core dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 1 + "\t" + f1_list_dd[j] + "\t" + f1_list_tp[j]);
            }
            }
//            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
//        show_records();
    }

    public static void main(String[] args) throws IOException {
        long MAIN_ALL_TIME = new Date().getTime();
        test_together();
//        test_separate_EXACT(9,9);
//        test_separate_CORE(3,3);
//        test_separate_DD(4,4);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t" + (new Date().getTime() - MAIN_ALL_TIME));
    }
}
