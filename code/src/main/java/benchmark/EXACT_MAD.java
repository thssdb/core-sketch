package benchmark;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

public class EXACT_MAD {
    public static double memory = 0;
    public static double median = 0;
    public static XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();


    public static double[] exact_mad(double[] data, int des) {
        memory = 0;
//        System.out.println("exact initial\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);
        DoubleArrayList queue = new DoubleArrayList(des);
//        List<Double> queue = new ArrayList<>(des);

        for(int i = 0; i < des; i++){
            queue.add(data[i]);
        }
        median = getKth(queue,0,des,(des-1)/2);
        queue.replaceAll(aDouble -> Math.abs(aDouble - median));

        double mad = getKth(queue,0,des,(des-1)/2);
        memory = (double)data.length * 8 / 1024;
        queue=null;
//        System.out.println(mad);
//        return memory;
//        return new double[]{memory,mad};
        return new double[]{memory,mad,median};
    }


    public static double exact_median(double[] data, int des) {
        memory = 0;
        DoubleArrayList queue = new DoubleArrayList(des);
        for(int i = 0; i < des; i++){
            queue.add(data[i]);
        }
        median = getKth(queue,0,des,(des-1)/2);
        return median;
    }

    public static double query(DoubleArrayList data){
        data.unstableSort(Double::compare);
        int rank = (int) Math.floor(0.5 * (data.size() - 1));
        return data.get(rank);
    }
    public static double getKth(DoubleArrayList data, int L, int R,int K){
        int pos = L+random.nextInt(R - L);
        double pivot_v = data.getDouble(pos), swap_v;

        int leP = L, eqR = R;
        data.set(pos, data.set(--eqR, pivot_v)); //   [L,leP): < pivot_v ;    [eqR,R): == pivot_v ;

        for(int i = L; i < eqR; i++)
            if((swap_v = data.getDouble(i)) < pivot_v)
                data.set(i, data.set(leP++, swap_v));
            else if(swap_v == pivot_v){
                data.set(i--, data.set(--eqR, swap_v));
            }

//        if(R-eqR>1)System.out.println("\t\t\t\tk_select. same pivot v.  count:"+(R-eqR));
        if(K < leP - L)return getKth(data, L, leP, K);
        if(K >= (leP - L) + (R - eqR))return getKth(data, leP, eqR, K - (leP - L) - (R - eqR));
        return pivot_v;
    }
}
