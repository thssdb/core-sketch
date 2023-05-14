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

public class ApproxBoundExp {
    static int DROP_COUNT=2;
    static int TEST_CASE=1,SPACE_LIMIT=2000,GC_SLEEP_BASE=0;
    static HashMap<String,Integer> SIZES = new HashMap<>(){{
        put("bitcoin-s", (int)1e7);
        put("gas-s", (int)1e7);
        put("power-s", (int)1e7);
        put("norm1", (int)1e9);
        put("pareto1", (int)1e9);
        put("chi1", (int)1e9);
    }};

//    static double[] epsilons = new double[]{1e-5,5e-5,1e-4,5e-4,1e-3,5e-3,1e-2,5e-2,1e-1};
    static double[] epsilons = new double[]{1e-6,1e-5,1e-4,1e-3,1e-2,1e-1};
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
            "bitcoin-s", "gas-s", "power-s"
            ,
            "pareto1","chi1","norm1"
        }) {
            int size = SIZES.get(dataset_name);
            int[] iter_list_core = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            long[] time_list_exact, time_list_core, time_list_core_single, time_list_dd, time_list_tp;
            double[] space_list_exact, space_list_core, space_list_core_single, space_list_dd, space_list_tp;
            double[] err_list_dd, err_list_tp;
            double[] mad_dd, mad_tp, mad_exact;
            time_list_exact=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core_single=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_dd=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_tp=new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_exact=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core_single=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_exact=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            int index = 0;
            for (double epsilon : epsilons) {
                String size_name = String.valueOf(epsilon);
                double minimum = 100000;
                double maximum = -100000;
                long times_exact,times_core,times_dd,times_tp,times_core_single;
                int iter_core;

                double relErr_core,space_core,space_exact,space_dd,space_tp,space_core_single;
                double relErr_dd,relErr_tp;

                times_exact=0;space_exact=0;
                times_core = 0;space_core = 0;relErr_core = 0;space_dd = 0;space_tp = 0;
                times_dd = 0;relErr_dd = 0;relErr_tp = 0;times_tp = 0;
                times_core_single = 0;space_core_single = 0;iter_core = 0;

                double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, 2*size);
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
                }catch (Exception e){
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERR\t\t Epsilon(Alpha):\t"+epsilon+"\t\tdataset:\t"+dataset_name);
                }

                long time_exact = new Date().getTime();
                double[] exact_result = EXACT_MAD.exact_mad(data, size);
                space_exact += exact_result[0] * TEST_CASE;
                times_exact += (new Date().getTime() - time_exact) * TEST_CASE;
                add_record("exact_"+dataset_name+"_"+size_name,new Date().getTime() - time_exact);
                exact_mad_v = exact_result[1];
                mad_exact[index] = exact_mad_v;

                for (int T = 0; T < TEST_CASE; T++) {
                    double old_space_exact=space_exact,old_space_dd=space_dd,old_space_tp=space_tp;
                    double old_relErr_dd=relErr_dd,old_relErr_tp=relErr_tp;
                    long old_times_dd=times_dd,old_times_tp=times_tp;
                    try {
                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_dd = new Date().getTime();
                        double[] dd_result = DD_MAD.dd_mad(data, epsilon, SPACE_LIMIT);
                        times_dd += (new Date().getTime() - time_dd);
                        space_dd += dd_result[0];
                        add_record("dd_"+dataset_name+"_"+size_name,new Date().getTime() - time_dd);
                        relErr_dd += Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v;
                        mad_dd[index] = dd_result[1];

                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_tp = new Date().getTime();
                        double[] tp_result = TP_MAD.tp_mad(data, epsilon, SPACE_LIMIT);
                        times_tp += (new Date().getTime() - time_tp);
                        space_tp += tp_result[0];
                        add_record("tp_"+dataset_name+"_"+size_name,new Date().getTime() - time_tp);
                        relErr_tp += Math.abs(tp_result[1] - exact_mad_v) / exact_mad_v;
                        mad_tp[index] = tp_result[1];
                    } catch (Exception e){
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ERR\t\t Epsilon(Alpha):\t"+epsilon+"\t\tdataset:\t"+dataset_name);
                        e.printStackTrace();
                        System.out.println("\t"+e.toString());
//                        T--;
                        space_exact=old_space_exact;
                        space_dd=old_space_dd;
                        space_tp=old_space_tp;
                        times_dd=old_times_dd;
                        relErr_dd=old_relErr_dd;
                        times_tp=old_times_tp;
                        relErr_tp=old_relErr_tp;
                    }
                }
                time_list_exact[index] = times_exact/TEST_CASE;
                time_list_dd[index] = times_dd/TEST_CASE;
                time_list_tp[index] = times_tp/TEST_CASE;
                space_list_exact[index] = space_exact/TEST_CASE;
                space_list_dd[index] = space_dd/TEST_CASE;
                space_list_tp[index] = space_tp/TEST_CASE;
                err_list_dd[index] = relErr_dd/TEST_CASE;
                err_list_tp[index] = relErr_tp/TEST_CASE;
                index += 1;
//                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core_single/ TEST_CASE) + "\t" + (times_core/ TEST_CASE) + "\t" + (times_dd/ TEST_CASE) + "\t" + (times_tp/ TEST_CASE) + "\t"+times_exact/TEST_CASE
//                        +"\t\trel Err:\t" + relErr_tp / TEST_CASE + "\t" + relErr_dd / TEST_CASE
//                        +"\tSpace:\t"+space_core_single/TEST_CASE+"\t"+space_core/TEST_CASE+"\t"+space_dd/TEST_CASE+"\t"+space_tp/TEST_CASE+"\t"+space_exact/TEST_CASE);
            }
            System.out.println("\tbucketList:\t"+ Arrays.toString(epsilons));
            System.out.println("\tN:\t"+ size+"\tSpace:\t"+SPACE_LIMIT);
            System.out.println(dataset_name + "\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + time_list_exact[j] + "\t" + time_list_dd[j] + "\t" + time_list_tp[j]);
            }
            System.out.println(dataset_name + "\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + space_list_exact[j] + "\t" + space_list_dd[j] + "\t" + space_list_tp[j]);
            }
            System.out.println(dataset_name + "\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 0 + "\t" + err_list_dd[j] + "\t" + err_list_tp[j]);
            }
            System.out.println(dataset_name + "\texact dd tp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + mad_exact[j] + "\t" + mad_dd[j] + "\t" + mad_tp[j]);
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
