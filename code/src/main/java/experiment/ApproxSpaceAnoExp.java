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

public class ApproxSpaceAnoExp {
    static int DROP_COUNT=0;
    static int sizeMin=(int)1e7,TEST_CASE=1,GC_SLEEP_BASE=0;
    static HashMap<String,Integer> SIZES = new HashMap<>(){{
        put("bitcoin-s", (int)1e7);
        put("gas-s", (int)1e7);
        put("power-s", (int)1e7);
        put("norm1", (int)1e9);
        put("pareto1", (int)1e9);
        put("chi1", (int)1e9);
    }};
    static HashMap<String,Double> ALPHAS = new HashMap<>(){{
        put("bitcoin-s", 1e-5);
        put("gas-s", 1e-5);
        put("power-s", 1e-5);
        put("norm1", 1e-4);
        put("pareto1", 1e-4);
        put("chi1", 1e-4);
//        put("bitcoin-s", 4e-4);// calcAlpha when space=37500
//        put("gas-s", 2e-4);
//        put("power-s", 9e-5);
//        put("bitcoin-s", 6e-4);// calcAlpha when space=25000
//        put("gas-s", 3e-4);
//        put("power-s", 2e-4);
    }};
    static HashMap<String,Double> KS = new HashMap<>(){{
        put("norm1", (double)4);
        put("pareto1", (double)100.0);
        put("chi1", (double)10.0);
        put("bitcoin-s", (double)20000.0);
        put("gas-s", (double)3.5);
        put("power-s", (double)15.0);
    }};
    //    static int[] bucketList = new int[]{11000,12000,13000,14000,15000,16000,17000,18000,19000,20000};
//static int[] bucketList = new int[]{2000,2000,4000,6000,8000,10000,15000,20000};
//static int[] bucketList = new int[]{1000, 2000, 3000, 4000, 5000, 6000, 8000, 10000, 12000};
    static int[] bucketList = new int[]{1000, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000,10000};
//    static int[] bucketList = new int[]{30000,31000,32000,33000,34000,35000,36000,37000,38000,39000,40000};
    static HashMap<String,LongArrayList> all_time_record = new HashMap<>();

    public static void set_testCase(int times) {
        TEST_CASE = times;
    }

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

        for (String dataset_name : new String[]{
            "bitcoin-s",
            "gas-s",
            "power-s",
            "pareto1",
            "chi1",
            "norm1"
        }) {
            int size = SIZES.get(dataset_name);
            double DDAlpha  = ALPHAS.get(dataset_name);
            double k = KS.get(dataset_name);
            System.out.println();
            int[] iter_list_core = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            long[] time_list_exact, time_list_core, time_list_core_single, time_list_dd, time_list_tp;
            double[] space_list_exact, space_list_core, space_list_core_single, space_list_dd, space_list_tp;
            double[] err_list_dd, err_list_tp;
            double[] mad_dd, mad_tp, mad_exact, median;
            double[] f1_list_dd, f1_list_tp;
            time_list_exact=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core_single=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_dd=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_tp=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_exact=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core_single=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_exact=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            median = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            f1_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            f1_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            int index = 0;
            for (int bucket : bucketList) {
                String size_name = String.valueOf(bucket);
                double minimum = 100000;
                double maximum = -100000;
                long times_exact,times_core,times_dd,times_tp,times_core_single;
                int iter_core;

                double relErr_core,space_core,space_exact,space_dd,space_tp,space_core_single;
                double relErr_dd,relErr_tp;

                for (int space = bucket; space <= bucket; space += bucket) {
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, 2*size);

                    times_exact=0;space_exact=0;
                    times_core = 0;space_core = 0;relErr_core = 0;space_dd = 0;space_tp = 0;
                    times_dd = 0;relErr_dd = 0;relErr_tp = 0;times_tp = 0;
                    times_core_single = 0;space_core_single = 0;iter_core = 0;

                    if(dataset_name.contains("s")){
                        XoRoShiRo128PlusRandom random= new XoRoShiRo128PlusRandom(233);
                        for(int i=1;i<data.length;i++){
                            int j=random.nextInt(i);
                            double t=data[i];data[i]=data[j];data[j]=t;
                        }
                    }
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
//                    System.out.println(minimum);
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    try {
//                        Arrays.sort(data);
//                        System.out.println("\tn:\t"+data.length+"\tmid:\t"+data[data.length/2]);
//                        continue;
                        System.gc();
//                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    double exact_mad_v = EXACT_MAD.exact_mad(data, size)[1];
                    for (int i = 0; i < DROP_COUNT; i++) {
                        CORE_MAD.core_mad(data, maximum - minimum + 1,1, space, false, true);
                        CORE_MAD.core_mad(data, maximum - minimum + 1,1, space, false, false);
//                        DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, space);
                        DD_MAD.dd_mad_givenAlpha(data, DDAlpha, space);
//                        TP_MAD.tp_mad_calcAlpha(data, maximum - minimum + 1, space);
                        TP_MAD.tp_mad_givenAlpha(data, DDAlpha, space);
                    }

                    for (int T = 0; T < TEST_CASE; T++) {
                        double old_space_exact=space_exact,old_space_dd=space_dd,old_space_tp=space_tp,old_space_core=space_core,old_space_core_single=space_core_single;
                        double old_relErr_dd=relErr_dd,old_relErr_tp=relErr_tp;
                        long old_times_exact=times_exact,old_times_core=times_core,old_times_core_single=times_core_single,old_times_dd=times_dd,old_times_tp=times_tp;
                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_exact = new Date().getTime();
                            double[] exact_result = EXACT_MAD.exact_mad(data, size);
                            space_exact += exact_result[0];
                            times_exact += (new Date().getTime() - time_exact);
                            add_record("exact_"+dataset_name+"_"+size_name,new Date().getTime() - time_exact);
                            exact_mad_v = exact_result[1];
                            mad_exact[index] = exact_mad_v;
                            median[index] = exact_result[2];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_dd = new Date().getTime();
                            double[] dd_result = DD_MAD.dd_mad_givenAlpha(data, DDAlpha, space);//DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, space);
//                            DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, 37500);
                            times_dd += (new Date().getTime() - time_dd);
                            space_dd += dd_result[0];
                            add_record("dd_"+dataset_name+"_"+size_name,new Date().getTime() - time_dd);
                            relErr_dd += Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v;
                            mad_dd[index] = dd_result[1];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_tp = new Date().getTime();
                            double[] tp_result = TP_MAD.tp_mad_givenAlpha(data, DDAlpha, space);//TP_MAD.tp_mad_calcAlpha(data, maximum - minimum + 1, space);
                            times_tp += (new Date().getTime() - time_tp);
                            space_tp += tp_result[0];
                            add_record("tp_"+dataset_name+"_"+size_name,new Date().getTime() - time_tp);
                            relErr_tp += Math.abs(tp_result[1] - exact_mad_v) / exact_mad_v;
                            mad_tp[index] = tp_result[1];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_core = new Date().getTime();
                            double[] core_result = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                1, space, false, true);
                            double core_mad = core_result[1];
                            space_core += core_result[0];
                            times_core += (new Date().getTime() - time_core);
                            add_record("core_"+dataset_name+"_"+size_name,new Date().getTime() - time_core);
                            relErr_core += Math.abs(core_mad - exact_mad_v) / exact_mad_v;

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_core_single = new Date().getTime();
                            double[] core_result_single = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                1, space, false, false);
                            space_core_single += core_result_single[0];
                            times_core_single += (new Date().getTime() - time_core_single);
                            iter_core += core_result_single[2];
                            add_record("core_single_"+dataset_name+"_"+size_name,new Date().getTime() - time_core_single);
                        } catch (Exception e){
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            e.printStackTrace();
//                            T--;
                            space_exact=old_space_exact;
                            space_core=old_space_core;
                            space_core_single=old_space_core_single;
                            space_dd=old_space_dd;
                            space_tp=old_space_tp;
                            times_exact=old_times_exact;
                            times_core=old_times_core;
                            times_dd=old_times_dd;
                            times_core_single=old_times_core_single;
                            times_tp=old_times_tp;
                            relErr_tp=old_relErr_tp;
                            relErr_dd=old_relErr_dd;
                        }
                    }
                    time_list_exact[index] = times_exact/TEST_CASE;
                    time_list_core[index] = times_core/TEST_CASE;
                    time_list_core_single[index] = times_core_single/TEST_CASE;
                    time_list_dd[index] = times_dd/TEST_CASE;
                    time_list_tp[index] = times_tp/TEST_CASE;
                    space_list_exact[index] = space_exact/TEST_CASE;
                    space_list_core[index] = space_core/TEST_CASE;
                    space_list_core_single[index] = space_core_single/TEST_CASE;
                    space_list_dd[index] = space_dd/TEST_CASE;
                    space_list_tp[index] = space_tp/TEST_CASE;
                    err_list_dd[index] = relErr_dd/TEST_CASE;
                    err_list_tp[index] = relErr_tp/TEST_CASE;
                    iter_list_core[index] = iter_core/TEST_CASE;

                    if(index==0)System.out.println("\t\t!!EXACT_MEDIAN:\t"+median[index]);
                    double upper_exact = median[index] + k * mad_exact[index];
                    double lower_exact = median[index] - k * mad_exact[index];
                    double upper_dd = median[index] + k * mad_dd[index];
                    double lower_dd = median[index] - k * mad_dd[index];
                    double upper_tp = median[index] + k * mad_tp[index];
                    double lower_tp = median[index] - k * mad_tp[index];
                    double anomaly_dd = 0; double anomaly_tp = 0; double anomaly_exact = 0;
                    double pre_dd, pre_tp, re_dd, re_tp;
                    for (double datum : data) {
                        if (datum > upper_exact || datum < lower_exact)
                            anomaly_exact += 1;
                        if (datum > upper_dd || datum < lower_dd)
                            anomaly_dd += 1;
                        if (datum > upper_tp || datum < lower_tp)
                            anomaly_tp += 1;
                    }
                    if (space == 1000) {
                        System.out.println(anomaly_exact);
                    }
                    if (anomaly_exact == 0) {
                        System.out.println(dataset_name + space + " anomaly_exact=0");
                        continue;
                    }
                    pre_dd = Math.min(1, anomaly_exact / anomaly_dd);
                    re_dd = Math.min(1, anomaly_dd / anomaly_exact);
                    pre_tp = Math.min(1, anomaly_exact / anomaly_tp);
                    re_tp = Math.min(1, anomaly_tp / anomaly_exact);
                    if (anomaly_dd == 0) {
                        pre_dd = 0;
                    }
                    if (anomaly_tp == 0) {
                        pre_tp = 0;
                    }
                    f1_list_dd[index] = (2 * pre_dd * re_dd) / (pre_dd + re_dd);
                    f1_list_tp[index] = (2 * pre_tp * re_tp) / (pre_tp + re_tp);
                    if (pre_dd + re_dd == 0) {
                        f1_list_dd[index] = 0;
                    }
                    if (pre_tp + re_tp == 0) {
                        f1_list_tp[index] = 0;
                    }

                    index += 1;
//                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core_single/ TEST_CASE) + "\t" + (times_core/ TEST_CASE) + "\t" + (times_dd/ TEST_CASE) + "\t" + (times_tp/ TEST_CASE) + "\t"+times_exact/TEST_CASE
//                        +"\t\trel Err:\t" + relErr_tp / TEST_CASE + "\t" + relErr_dd / TEST_CASE
//                        +"\tSpace:\t"+space_core_single/TEST_CASE+"\t"+space_core/TEST_CASE+"\t"+space_dd/TEST_CASE+"\t"+space_tp/TEST_CASE+"\t"+space_exact/TEST_CASE);
                }
            }
            System.out.println("\tbucketList:\t"+ Arrays.toString(bucketList));
            System.out.println(dataset_name + "(time)\texact core core_single dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + time_list_exact[j] + "\t" + time_list_core[j] + "\t" + time_list_core_single[j] + "\t" + time_list_dd[j] + "\t" + time_list_tp[j]);
            }
            System.out.println(dataset_name + "(space)\texact core core_single dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + space_list_exact[j] + "\t" + space_list_core[j] + "\t" + space_list_core_single[j] + "\t" + space_list_dd[j] + "\t" + space_list_tp[j]);
            }
            System.out.println(dataset_name + "(err)\texact core core_single dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + err_list_dd[j] + "\t" + err_list_tp[j]);
            }
            System.out.println(dataset_name + "(iter)\texact core_single");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + iter_list_core[j]);
            }
            System.out.println(dataset_name + "(mad)\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + mad_exact[j] + "\t" + mad_dd[j] + "\t" + mad_tp[j]);
            }

            System.out.println(dataset_name + "(f1)\texact core dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 1 + "\t" + f1_list_dd[j] + "\t" + f1_list_tp[j]);
            }
//            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + (new Date().getTime() - ALL_ST));
//        show_records();
    }

    public static void main(String[] args) throws IOException{
        long MAIN_ALL_TIME = new Date().getTime();
        test_together();
//        test_separate_EXACT(9,9);
//        test_separate_CORE(3,3);
//        test_separate_DD(4,4);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(new Date().getTime()-MAIN_ALL_TIME));
    }
}
