package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.TP_MAD;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static utils.FileHelper.READ;

public class ApproxAndDataSizeExp {
    static int DROP_COUNT=4;
    static int SIZE_L=10,SIZE_R=20,TEST_CASE=1,SPACE_LIMIT=4000,GC_SLEEP_BASE=50;
    static HashMap<String,LongArrayList> all_time_record = new HashMap<>();
    static HashMap<String,Integer> SIZES = new HashMap<>(){{
        put("bitcoin-s", (int)1e6);
        put("gas-s", (int)1e6);
        put("power-s", (int)1e6);
        put("norm1", (int)1e4);
        put("pareto1", (int)1e4);
        put("chi1", (int)1e4);
    }};
    static HashMap<String,Double> ALPHAS = new HashMap<>(){{
//        put("norm1", 1e-4);// calc when bucket=25000
//        put("pareto1", 1e-3);
//        put("chi1", 1e-4);
//        put("bitcoin-s", 6e-4);
//        put("gas-s", 3e-4);
//        put("power-s", 2e-4);

//        put("norm1", 1e-4);
//        put("pareto1", 1e-4);
//        put("chi1", 1e-4);
        put("bitcoin-s", 1e-4);
        put("gas-s", 1e-4);
        put("power-s", 1e-4);
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
//        System.out.println("\t\tEpsilons:\t"+ALPHAS);
//        System.out.println("\t\tBucket:\t"+SPACE_LIMIT+"\n");
        long ALL_ST = (System.nanoTime()/1000);

        for (String dataset_name : new String[]{
            "bitcoin-s",
            "gas-s",
            "power-s"
//            ,
//            "pareto1","chi1"//,"norm1"
        }) {
            double DDAlpha  = ALPHAS.get(dataset_name);
            System.out.println();
            int[] iter_list_core = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            long[] time_list_exact, time_list_core, time_list_core_single, time_list_dd, time_list_tp;
            double[] space_list_exact, space_list_core, space_list_core_single, space_list_dd, space_list_tp;
            double[] err_list_dd, err_list_tp;
            double[] mad_dd, mad_tp, mad_exact;
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
            err_list_dd=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            err_list_tp=new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            int index = 0;
            int sizeMin = SIZES.get(dataset_name);
            IntArrayList sizeList = new IntArrayList();

            //real [100000, 300000, 500000, 700000, 1000000, 2000000, 4000000, 6000000, 8000000, 10000000]
            if(dataset_name.contains("-s")) {

                sizeList.add(1 * sizeMin);
                sizeList.add(1 * sizeMin);
                sizeList.add(2 * sizeMin);
                sizeList.add(3 * sizeMin);
                sizeList.add(4 * sizeMin);
                sizeList.add(5 * sizeMin);
                sizeList.add(6 * sizeMin);
                sizeList.add(7 * sizeMin);
                sizeList.add(8 * sizeMin);
                sizeList.add(9 * sizeMin);
                sizeList.add(10 * sizeMin);
            }else{
                sizeList.add(1 * sizeMin);
//                sizeList.add(2 * sizeMin);
//                sizeList.add(4 * sizeMin);
//                sizeList.add(6 * sizeMin);
//                sizeList.add(8 * sizeMin);
//                sizeList.add(10 * sizeMin);
//                sizeList.add(20 * sizeMin);
//                sizeList.add(30 * sizeMin);
//                sizeList.add(50 * sizeMin);
//                sizeList.add(100 * sizeMin);
            }
//            for (int mmpSize = sizeMin * SIZE_L; mmpSize <= sizeMin * SIZE_R; mmpSize += sizeMin)
//                sizeList.add(mmpSize);

            for (int mmpSize:sizeList) {
                String size_name = mmpSize>=sizeMin*10?String.valueOf(mmpSize):('0'+String.valueOf(mmpSize));
                double minimum = 100000;
                double maximum = -100000;
                long times_exact,times_core,times_dd,times_tp,times_core_single;
                int iter_core;

                double relErr_core,space_core,space_exact,space_dd,space_tp,space_core_single;
                double relErr_dd,relErr_tp;

                for (int size = mmpSize; size <= mmpSize; size += mmpSize) {
                    times_exact=0;space_exact=0;
                    times_core = 0;space_core = 0;relErr_core = 0;space_dd = 0;space_tp = 0;
                    times_dd = 0;relErr_dd = 0;relErr_tp = 0;times_tp = 0;
                    times_core_single = 0;space_core_single = 0;iter_core = 0;

//                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size*5, size*5);
                    double[] data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size, size);
                    if(dataset_name.contains("s")){
                        XoRoShiRo128PlusRandom random= new XoRoShiRo128PlusRandom(233);
                        for(int i=1;i<data.length;i++){
                            int j=random.nextInt(i);
                            double t=data[i];data[i]=data[j];data[j]=t;
                        }
                    }
//                    data=Arrays.copyOfRange(data,size*1,size*2);

                    for (int i = 0; i < data.length; i++) {
                        if (data[i] < minimum) minimum = data[i];
                        if (data[i] > maximum) maximum = data[i];
                    }
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i] - minimum + 1;
                    }
                    double exact_mad_v;
                    for (int i = 0; i < DROP_COUNT; i++) {
                        try {
                            CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
                            CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, false);
//                        DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                            DD_MAD.dd_mad_givenAlpha(data, DDAlpha, SPACE_LIMIT);
                            TP_MAD.tp_mad_givenAlpha(data, DDAlpha, SPACE_LIMIT);
                        }catch (Exception e){
                            continue;
                        }
                    }
                    for (int T = 0; T < TEST_CASE; T++) {
                        if(dataset_name.contains("gas")||dataset_name.contains("bitcoin")) {
                            int SIZE_K=4,SIZE_K2=SIZE_K;
                            if(dataset_name.contains("bitcoin")){SIZE_K=4;SIZE_K2=5;}
                            data = READ("E:\\MAD-data\\" + dataset_name + ".csv", size * SIZE_K, size * SIZE_K2);
                            data = Arrays.copyOfRange(data, Math.abs(new Random(233).nextInt()) % (size * SIZE_K - size), Math.abs(new Random(233).nextInt()) % (size * SIZE_K - size) + size);
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
                        }


//                        System.out.println("\t\tMMMP\tT:\t"+T);
                        double old_space_exact=space_exact,old_space_dd=space_dd,old_space_tp=space_tp,old_space_core=space_core,old_space_core_single=space_core_single;
                        double old_relErr_dd=relErr_dd,old_relErr_tp=relErr_tp;
                        long old_times_exact=times_exact,old_times_core=times_core,old_times_core_single=times_core_single,old_times_dd=times_dd,old_times_tp=times_tp;
                        try {
                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_exact = (System.nanoTime()/1000);
                            double[] exact_result = EXACT_MAD.exact_mad(data, size);
                            space_exact += exact_result[0];
                            times_exact += ((System.nanoTime()/1000)  - time_exact);
                            add_record("exact_"+dataset_name+"_"+size_name,(System.nanoTime()/1000) - time_exact);
                            exact_mad_v = exact_result[1];
                            mad_exact[index] = exact_mad_v;

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_dd = (System.nanoTime()/1000);
                            double[] dd_result = DD_MAD.dd_mad_givenAlpha(data, DDAlpha, SPACE_LIMIT);//DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                            times_dd += ((System.nanoTime()/1000) - time_dd);
                            space_dd += dd_result[0];
                            add_record("dd_"+dataset_name+"_"+size_name,(System.nanoTime()/1000) - time_dd);
                            relErr_dd += Math.abs(dd_result[1] - exact_mad_v) / exact_mad_v;
                            mad_dd[index] = dd_result[1];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_tp = (System.nanoTime()/1000);
                            double[] tp_result = TP_MAD.tp_mad_givenAlpha(data, DDAlpha, SPACE_LIMIT);//TP_MAD.tp_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                            times_tp += ((System.nanoTime()/1000) - time_tp);
                            space_tp += tp_result[0];
                            add_record("tp_"+dataset_name+"_"+size_name,(System.nanoTime()/1000) - time_tp);
                            relErr_tp += Math.abs(tp_result[1] - exact_mad_v) / exact_mad_v;
                            mad_tp[index] = tp_result[1];

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_core = (System.nanoTime()/1000);
                            double[] core_result = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                1, SPACE_LIMIT, false, true);
                            double core_mad = core_result[1];
                            space_core += core_result[0];
                            times_core += ((System.nanoTime()/1000) - time_core);
                            add_record("core_"+dataset_name+"_"+size_name,(System.nanoTime()/1000) - time_core);
                            relErr_core += Math.abs(core_mad - exact_mad_v) / exact_mad_v;

                            System.gc();
                            Thread.sleep(GC_SLEEP_BASE);
                            long time_core_single = (System.nanoTime()/1000);
                            double[] core_result_single = CORE_MAD.core_mad(data, maximum - minimum + 1,
                                    1, SPACE_LIMIT, false, false);
                            space_core_single += core_result_single[0];
                            times_core_single += ((System.nanoTime()/1000) - time_core_single);
                            iter_core += core_result_single[2];
                            add_record("core_single_"+dataset_name+"_"+size_name,(System.nanoTime()/1000) - time_core_single);
                        } catch (Exception e){
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!error\tdataSize:\t"+size);
                            Thread.sleep(100);
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
                    index += 1;
//                    System.out.println("\t\t\t\t\tindex:\t"+index);
//                    System.out.println("data:"+dataset_name+"\tn:\t" + size + "\t\t"  + "TIME:\t" + (times_core_single/ TEST_CASE) + "\t" + (times_core/ TEST_CASE) + "\t" + (times_dd/ TEST_CASE) + "\t" + (times_tp/ TEST_CASE) + "\t"+times_exact/TEST_CASE
//                        +"\t\trel Err:\t" + relErr_tp / TEST_CASE + "\t" + relErr_dd / TEST_CASE
//                        +"\tSpace:\t"+space_core_single/TEST_CASE+"\t"+space_core/TEST_CASE+"\t"+space_dd/TEST_CASE+"\t"+space_tp/TEST_CASE+"\t"+space_exact/TEST_CASE);
                }
//                System.out.println("\t\t\tover");
            }
            System.out.print("\tN:");for (int j = 0; j < index; j++)System.out.print("\t\t"+sizeList.getInt(j));System.out.println();
            System.out.println("\tspace:\t"+SPACE_LIMIT+"\t\tEpsilon:"+DDAlpha);
            System.out.println(dataset_name + "(time us)\texact core core_single dd tp");
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
//            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + ((System.nanoTime()/1000) - ALL_ST));
//        show_records();
    }

    public static void test_separate_DD(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = (System.nanoTime()/1000);

        int sizeMin = (int)1e8;
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

                        long tmp_time = (System.nanoTime()/1000);
                        DD_MAD.dd_mad_calcAlpha(data, maximum - minimum + 1, SPACE_LIMIT);
                        time += ((System.nanoTime()/1000) - tmp_time);
                        space+=DD_MAD.memory;
                        add_record("dd_" + dataset_name + "_" + size_name, (System.nanoTime()/1000) - tmp_time);
                    }
                    System.out.println("dd data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + time / TEST_CASE+"\tSPACE:\t"+space/TEST_CASE);
                }
        }
//        System.out.println("\n\nALL_TIME:" + ((System.nanoTime()/1000) - ALL_ST));
        show_records();
    }
    public static void test_separate_EXACT(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = (System.nanoTime()/1000);

        int sizeMin=(int)1e8;
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

                        long time_exact = (System.nanoTime()/1000);
                        EXACT_MAD.exact_mad(data, size);
                        times_exact += ((System.nanoTime()/1000) - time_exact);//System.out.println("\tt\t\t\t\t\t"+(((System.nanoTime()/1000) - time_exact)));
                        add_record("exact_" + dataset_name + "_" + size_name, (System.nanoTime()/1000) - time_exact);
                    }
                    System.out.println("data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + times_exact / TEST_CASE);
                }
            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + ((System.nanoTime()/1000) - ALL_ST));
        show_records();
    }
    public static void test_separate_CORE(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = (System.nanoTime()/1000);

        int sizeMin=(int)1e8;
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

                        long tmp_time = (System.nanoTime()/1000);
                        double[] result = CORE_MAD.core_mad(data, maximum - minimum + 1, 1, SPACE_LIMIT, false, true);
//                        System.out.println("\t\t\tspace:\t"+result[0]);
                        time += ((System.nanoTime()/1000) - tmp_time);
                        // System.out.println("\tt\t\t\t\t\t"+((System.nanoTime()/1000) - tmp_time));
                        add_record("core_" + dataset_name + "_" + size_name, (System.nanoTime()/1000) - tmp_time);
                    }
                    System.out.println("core data:" + dataset_name + "\tn:\t" + size + "\t\t" + "TIME:\t" + "\t" + time / TEST_CASE);
                }
//            show_records();
        }
//        System.out.println("\n\nALL_TIME:" + ((System.nanoTime()/1000) - ALL_ST));
        show_records();
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        long MAIN_ALL_TIME = (System.nanoTime()/1000);
        test_together();
//        test_separate_EXACT(9,9);
//        test_separate_CORE(3,3);
//        test_separate_DD(4,4);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+((System.nanoTime()/1000)-MAIN_ALL_TIME));
    }
}
