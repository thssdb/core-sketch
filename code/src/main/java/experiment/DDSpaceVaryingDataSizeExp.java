package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static utils.FileHelper.READ;

public class DDSpaceVaryingDataSizeExp {
    static int sizeMin=(int)1e8,TEST_CASE=1,SPACE_LIMIT=20000,GC_SLEEP_BASE=24000;
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

    public static void test_separate_DD(int sizeL,int sizeR) throws IOException {
        all_time_record = new HashMap<>();
        long ALL_ST = new Date().getTime();

        for (int mmpSize = sizeMin * sizeL; mmpSize <= sizeMin * sizeR; mmpSize += sizeMin) {
            int GC_SLEEP = GC_SLEEP_BASE*(mmpSize/sizeMin)/10;
            String size_name = mmpSize == sizeMin * 10 ? String.valueOf(mmpSize) : ('0' + String.valueOf(mmpSize));
            System.out.println();
            for (String dataset_name : new String[]{"norm1", "chi1", "pareto1"})
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
//        show_records();
    }

    public static void main(String[] args) throws IOException{
//        test_together();
        long MAIN_ALL_TIME = new Date().getTime();
        test_separate_DD(10,10);
//        test_separate_CORE(10,10);

//        test_separate_DD(5,8);
//        test_separate_CORE(5,8);

        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(new Date().getTime()-MAIN_ALL_TIME));
    }
}
