package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.TP_MAD;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static utils.FileHelper.READ;

public class NormExp {
    public static int NORM_PARTITION=4;
    static int DROP_COUNT=0;
    static int SIZE_L=10,SIZE_R=20,TEST_CASE=1,SPACE_LIMIT=4000,GC_SLEEP_BASE=20;
    static HashMap<String,LongArrayList> all_time_record = new HashMap<>();
    static HashMap<String,Integer> SIZES = new HashMap<>(){{
        put("norm1", (int)1e8);
        put("pareto1", (int)1e8);
        put("chi1", (int)1e8);
    }};
    static HashMap<String,Integer> SYN_TYPE = new HashMap<>(){{
        put("norm1", 0);
        put("pareto1", 1);
        put("chi1", 2);
    }};
    static HashMap<String,Double> ALPHAS = new HashMap<>(){{
        put("norm1", 1e-4);
        put("pareto1", 1e-4);
        put("chi1", 1e-4);
    }};

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

    public static void test_together() throws IOException, InterruptedException {
        long ALL_ST = (System.nanoTime() / 1000);

        for (String dataset_name : new String[]{
            "pareto1"
            ,
            "chi1"
            ,
            "norm1"
        }) {
            double DDAlpha = ALPHAS.get(dataset_name);
            int syn_type = SYN_TYPE.get(dataset_name);
            System.out.println();
            int[] iter_list_core = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            long[] time_list_exact, time_list_core, time_list_core_single, time_list_dd, time_list_tp;
            double[] space_list_exact, space_list_core, space_list_core_single, space_list_dd, space_list_tp;
            double[] err_list_dd, err_list_tp;
            double[] mad_core_single,mad_core,mad_dd, mad_tp, mad_exact;
            time_list_exact = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_core_single = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_dd = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            time_list_tp = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_exact = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_core_single = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            space_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_core_single = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_core = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            mad_exact = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_dd = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_tp = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            double exact_mad_v=0;

            int index = 0;
            int sizeMin = SIZES.get(dataset_name);
            LongArrayList sizeList = new LongArrayList();

            {
                sizeList.add(sizeMin/100);
                sizeList.add(sizeMin/100);
                sizeList.add(sizeMin/10);
                sizeList.add(1L * sizeMin);
                sizeList.add(10L * sizeMin);
//                sizeList.add(100L * sizeMin);
            }
//            for (int mmpSize = sizeMin * SIZE_L; mmpSize <= sizeMin * SIZE_R; mmpSize += sizeMin)
//                sizeList.add(mmpSize);

            for (long size : sizeList) {
                System.out.println("\t\t\t\trunning size="+size);
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

                double dataV;
                for (int b = 0; b < NORM_PARTITION; b++) {
                    RandomGenerator rng = new Well44497b(b);
                    AbstractRealDistribution dis;
                    if(syn_type==0)dis = new NormalDistribution(rng ,4, 2);else if(syn_type==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
                    for (long i = 0; i < size / NORM_PARTITION; i++) {
                        dataV = dis.sample();
                        if(syn_type==2)dataV*=dataV;
                        minimum = Math.min(minimum, dataV);
                        maximum = Math.max(maximum, dataV);
                    }
                }
//                for (int b = 0; b < NORM_PARTITION; b++) {
//                    XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
//                    for (long i = 0; i < size / NORM_PARTITION; i++) {
//                        dataV = rnd.nextGaussian() * 2 + 4;
//                        minimum = Math.min(minimum, dataV);
//                        maximum = Math.max(maximum, dataV);
//                    }
//                }
//                System.out.println("\t\tminV,maxV:\t"+minimum+"\t"+maximum);
                for (int i = 0; i < DROP_COUNT; i++) {
                    try {
                        CORE_MAD.core_mad_norm(syn_type,size, maximum, minimum, SPACE_LIMIT, false, true);
                        CORE_MAD.core_mad_norm(syn_type,size, maximum, minimum, SPACE_LIMIT, false, false);
                        DD_MAD.dd_mad_givenAlpha_norm(syn_type,size, minimum, DDAlpha, SPACE_LIMIT);
                        TP_MAD.tp_mad_givenAlpha_norm(syn_type,size, minimum, DDAlpha, SPACE_LIMIT);
                    } catch (Exception e) {
                        continue;
                    }
                }
                TEST_CASE=(size<=1e7?TEST_CASE*8:size<=1e8?TEST_CASE*4:TEST_CASE); // DANGER
                for (int T = 0; T < TEST_CASE; T++) {
//                        System.out.println("\t\tMMMP\tT:\t"+T);
                    double old_space_exact = space_exact, old_space_dd = space_dd, old_space_tp = space_tp, old_space_core = space_core, old_space_core_single = space_core_single;
                    double old_relErr_dd = relErr_dd, old_relErr_tp = relErr_tp;
                    long old_times_exact = times_exact, old_times_core = times_core, old_times_core_single = times_core_single, old_times_dd = times_dd, old_times_tp = times_tp;
                    try {
                        if(size<=2e9+1e7) {
                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            double[] data = new double[(int) size];
                            int tmpppp = 0;
                            for (int b = 0; b < NORM_PARTITION; b++) {
                                RandomGenerator rng = new Well44497b(b);
                                AbstractRealDistribution dis;
                                if (syn_type == 0) dis = new NormalDistribution(rng, 4, 2);
                                else if (syn_type == 1) dis = new ParetoDistribution(rng, 1, 1);
                                else dis = new NormalDistribution(rng, 0, 1);//dis=new ChiSquaredDistribution(rng,1);
                                for (long i = 0; i < size / NORM_PARTITION; i++) {
                                    dataV = dis.sample();
                                    if (syn_type == 2) dataV *= dataV;
                                    data[tmpppp++] = dataV;
                                }
                            }
                            long time_exact = (System.nanoTime() / 1000);
                            double[] exact_result = EXACT_MAD.exact_mad(data, (int) size);
                            space_exact += exact_result[0];
                            times_exact += ((System.nanoTime() / 1000) - time_exact);
                            mad_exact[index] = exact_result[1];
//                        System.out.println("\t\tex mad:\t"+exact_result[1]);
                        }

                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_core = (System.nanoTime() / 1000);
                        double[] core_result = CORE_MAD.core_mad_norm(syn_type,size, maximum, minimum, SPACE_LIMIT, false, true);
                        double core_mad = core_result[1];
                        space_core += core_result[0];
                        times_core += ((System.nanoTime() / 1000) - time_core)-
                            CORE_MAD.NORM_TIME/1000;
                        mad_core[index] = core_result[1];
                        exact_mad_v=core_result[1];

                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_core_single = (System.nanoTime() / 1000);
                        double[] core_result_single = CORE_MAD.core_mad_norm(syn_type,size, maximum, minimum, SPACE_LIMIT, false, false);
                        space_core_single += core_result_single[0];
                        times_core_single += ((System.nanoTime() / 1000) - time_core_single)-
                            CORE_MAD.NORM_TIME/1000;
                        iter_core += core_result_single[2];
                        mad_core_single[index] = core_result_single[1];

                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_dd = (System.nanoTime() / 1000);
                        double[] dd_result = DD_MAD.dd_mad_givenAlpha_norm(syn_type,size, minimum, DDAlpha, SPACE_LIMIT);
                        times_dd += ((System.nanoTime() / 1000) - time_dd)-
                            DD_MAD.NORM_TIME/1000;
                        space_dd += dd_result[0];
                        mad_dd[index] = dd_result[1];
                        relErr_dd += Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v;
//                        System.out.println("\t\t\t"+dd_result[1]+"\t"+exact_mad_v+"\t\t"+Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v);

                        System.gc();
                        Thread.sleep(GC_SLEEP_BASE);
                        long time_tp = (System.nanoTime() / 1000);
                        double[] tp_result = TP_MAD.tp_mad_givenAlpha_norm(syn_type,size, minimum, DDAlpha, SPACE_LIMIT);
                        times_tp += ((System.nanoTime() / 1000) - time_tp)-
                            TP_MAD.NORM_TIME/1000;
                        space_tp += tp_result[0];
                        mad_tp[index] = tp_result[1];
                        relErr_tp += Math.abs(tp_result[1] - exact_mad_v) / exact_mad_v;

                    } catch (Exception e) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!error\tdataSize:\t" + size);
                        Thread.sleep(100);
                        e.printStackTrace();
//                            T--;
                        space_exact = old_space_exact;
                        space_core = old_space_core;
                        space_core_single = old_space_core_single;
                        space_dd = old_space_dd;
                        space_tp = old_space_tp;
                        times_exact = old_times_exact;
                        times_core = old_times_core;
                        times_dd = old_times_dd;
                        times_core_single = old_times_core_single;
                        times_tp = old_times_tp;
                        relErr_tp = old_relErr_tp;
                        relErr_dd = old_relErr_dd;
                    }
                }
                time_list_exact[index] = times_exact / TEST_CASE;
                time_list_core[index] = times_core / TEST_CASE;
                time_list_core_single[index] = times_core_single / TEST_CASE;
                time_list_dd[index] = times_dd / TEST_CASE;
                time_list_tp[index] = times_tp / TEST_CASE;
                space_list_exact[index] = space_exact / TEST_CASE;
                space_list_core[index] = space_core / TEST_CASE;
                space_list_core_single[index] = space_core_single / TEST_CASE;
                space_list_dd[index] = space_dd / TEST_CASE;
                space_list_tp[index] = space_tp / TEST_CASE;
                err_list_dd[index] = relErr_dd / TEST_CASE;
                err_list_tp[index] = relErr_tp / TEST_CASE;
                iter_list_core[index] = iter_core / TEST_CASE;
                index += 1;
                TEST_CASE=1; // DANGER

//                System.out.print("[[---temp]]\n\tdataSize:");
//                for (int j = 0; j < index; j++) System.out.print("\t\t" + sizeList.getLong(j));
//                System.out.print("\tSpace:\t"+SPACE_LIMIT+"\t\tEpsilon:\t"+DDAlpha);
//                System.out.println();
//                System.out.println(dataset_name + "(time s)\texact\tcore\tcore_single\tdd\ttp");
//                for (int j = 0; j < index; j++) {
//                    System.out.println((j + 1) + "\t" + time_list_exact[j]/1000000.0 + "\t" + time_list_core[j]/1000000.0 + "\t" + time_list_core_single[j]/1000000.0 + "\t" + time_list_dd[j]/1000000.0 + "\t" + time_list_tp[j]/1000000.0);
//                }
//                System.out.println(dataset_name + "(space)\texact\tcore\tcore_single\tdd\ttp");
//                for (int j = 0; j < index; j++) {
//                    System.out.println((j + 1) + "\t" + space_list_exact[j] + "\t" + space_list_core[j] + "\t" + space_list_core_single[j] + "\t" + space_list_dd[j] + "\t" + space_list_tp[j]);
//                }
//                System.out.println(dataset_name + "(err)\texact\tcore\tcore_single\tdd\ttp");
//                for (int j = 0; j < index; j++) {
//                    System.out.println((j + 1) + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + err_list_dd[j] + "\t" + err_list_tp[j]);
//                }
//                System.out.println(dataset_name + "(iter)\texact\tcore_single");
//                for (int j = 0; j < index; j++) {
//                    System.out.println((j + 1) + "\t" + iter_list_core[j]);
//                }
//                System.out.println(dataset_name + "(mad)\texact\tcore\tcore_single\tdd\ttp");
//                for (int j = 0; j < index; j++) {
//                    System.out.println((j + 1) + "\t" + mad_exact[j] + "\t" + mad_core[j] + "\t" + mad_core_single[j]+ "\t" + mad_dd[j] + "\t" + mad_tp[j]);
//                }
//                System.out.println("\t\t[temp]\n\n"+size);
            }
//                System.out.println("\t\t\tover");
            System.out.print("\tdataSize:");
            for (int j = 0; j < index; j++) System.out.print("\t\t" + sizeList.getLong(j));
            System.out.print("\tSpace:\t"+SPACE_LIMIT+"\t\tEpsilon:\t"+DDAlpha);
            System.out.println();
            System.out.println(dataset_name + "(time s)\texact\tcore\tcore_single\tdd\ttp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + time_list_exact[j]/1000000.0 + "\t" + time_list_core[j]/1000000.0 + "\t" + time_list_core_single[j]/1000000.0 + "\t" + time_list_dd[j]/1000000.0 + "\t" + time_list_tp[j]/1000000.0);
            }
            System.out.println(dataset_name + "(space)\texact\tcore\tcore_single\tdd\ttp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + space_list_exact[j] + "\t" + space_list_core[j] + "\t" + space_list_core_single[j] + "\t" + space_list_dd[j] + "\t" + space_list_tp[j]);
            }
            System.out.println(dataset_name + "(err)\texact\tcore\tcore_single\tdd\ttp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + err_list_dd[j] + "\t" + err_list_tp[j]);
            }
            System.out.println(dataset_name + "(iter)\texact\tcore_single");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + iter_list_core[j]);
            }
            System.out.println(dataset_name + "(mad)\texact\tcore\tcore_single\tdd\ttp");
            for (int j = 0; j < index; j++) {
                System.out.println((j + 1) + "\t" + mad_exact[j] + "\t" + mad_core[j] + "\t" + mad_core_single[j]+ "\t" + mad_dd[j] + "\t" + mad_tp[j]);
            }
        }
//            show_records();

//        System.out.println("\n\nALL_TIME:" + ((System.nanoTime()/1000) - ALL_ST));
//        show_records();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long MAIN_ALL_TIME = (System.nanoTime()/1000);
        test_together();
//        test_separate_EXACT(9,9);
//        test_separate_CORE(3,3);
//        test_separate_DD(4,4);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(((System.nanoTime()/1000)-MAIN_ALL_TIME))/1000.0/1000.0/60.0+"\tmin");
    }
}
