package experiment;

import benchmark.CORE_MAD;
import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.TP_MAD;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static utils.FileHelper.READ;

public class BoomExp_Bucket_M {
    static int DROP_COUNT=0;
    static int TEST_CASE=1;
    static HashMap<String,Integer> SIZES = new HashMap<>(){{
        put("bitcoin-s", (int)1e7);
        put("gas-s", (int)1e7);
        put("power-s", (int)1e7);
        put("norm1", (int)1e8);
        put("pareto1", (int)1e8);
        put("chi1", (int)1e8);
    }};

    static double[] epsilons = new double[]{1e-7,5e-7,1e-6,5e-6,1e-5,5e-5,1e-4,5e-4,1e-3,5e-3,1e-2,5e-2,1e-1};
    static int[] bucketList = new int[]{2000,4000,6000,8000,10000,15000,20000,25000,30000,35000,40000,45000,50000};

    public static void test_together() throws IOException {
        long ALL_ST = new Date().getTime();

        for (String dataset_name : new String[]{
            "bitcoin-s", "gas-s", "power-s"
            ,
            "pareto1","chi1","norm1"
        }) {
            DecimalFormat df1 = new DecimalFormat("#.##E0");
            int size = SIZES.get(dataset_name);
            System.out.println("\n\n[\tDATASET:\t"+dataset_name+"\tdatasize:\t"+size+"\t]");

            double minimum = 100000;
            double maximum = -100000;
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

            double[] exact_result = EXACT_MAD.exact_mad(data, size);
            double exact_mad_v = exact_result[1];

            System.out.println("CORE-SKETCH Boom?");
            for(int bucket:bucketList){
                System.out.print("bucket:\t"+bucket+"\t");
                boolean boomed=false;
                try {
                    CORE_MAD.core_mad(data, maximum - minimum + 1, 1, bucket, false, true);
                }catch (Exception e){
                    boomed=true;
                }
                System.out.print("\t"+(boomed?"BOOM":"OK"));
                System.out.println();
            }

            System.out.println("DD-SKETCH Boom?   relative_err");
            System.out.print("epsilons:\t\t");for (double epsilon : epsilons)System.out.print("\t"+epsilon);System.out.println();
            for(int bucket:bucketList){
                System.out.print("bucket:\t"+bucket+"\t");

                for(double epsilon:epsilons){
                    boolean boomed=false;
                    double[] result=new double[2];
                    double rel_err=1;
                    try {
                        result=DD_MAD.dd_mad_givenAlpha(data, epsilon,bucket);
                    }catch (Exception e){
                        boomed=true;
                    }
                    if(!boomed){
                        rel_err=Math.abs(result[1] - exact_mad_v) / exact_mad_v;
                        boomed=rel_err>=0.999;
                    }
                    System.out.print("\t"+(boomed?"BOOM":df1.format(rel_err)));
                }
                System.out.println();
            }

            System.out.println("TP-SKETCH Boom?   relative_err");
            System.out.print("epsilons:\t\t");for (double epsilon : epsilons)System.out.print("\t"+epsilon);System.out.println();
            for(int bucket:bucketList){
                System.out.print("bucket:\t"+bucket+"\t");

                for(double epsilon:epsilons){
                    boolean boomed=false;
                    double[] result=new double[2];
                    double rel_err=1.0;
                    try {
                        result=TP_MAD.tp_mad_givenAlpha(data, epsilon,bucket);
                    }catch (Exception e){
                        boomed=true;
                    }
                    if(!boomed){
                        rel_err=Math.abs(result[1] - exact_mad_v) / exact_mad_v;
                        boomed=rel_err>=0.999;
                    }
                    System.out.print("\t"+(boomed?"BOOM":df1.format(rel_err)));
                }
                System.out.println();
            }
        }
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
