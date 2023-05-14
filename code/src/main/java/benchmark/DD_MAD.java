package benchmark;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import mad.DDSketch;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.util.Random;

public class DD_MAD {
    public static long NORM_N,NORM_TIME;
    public static int NORM_TYPE,NORM_PARTITION=4,NORM_PAGE=1000;
    public static double NORM_MinV;
    public static double memory = 0;
    public static double mad;
    public static double median;

    public static double[] dd_mad(double[] data, double epsilon, int maxNumBins) {
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, epsilon, maxNumBins);
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
        }
        mad = dd_median(data_exp, epsilon, maxNumBins) - 1;
        return new double[]{memory, mad};
    }
    public static double[] dd_mad_calcAlpha(double[] data, double maxV, int maxNumBins) {
        double alpha = Math.pow(10,Math.log10(maxV)/(maxNumBins*0.75))-1;
        System.out.println("\t\t\t\tcalc alpha:\t"+Math.pow(1.0+alpha,(maxNumBins*0.75))+"\t\tmaxV:"+maxV+"\t\t\tAlpha:\t"+alpha);
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, alpha, maxNumBins);
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
        }
        mad = dd_median(data_exp, alpha, maxNumBins) - 1;
        return new double[]{memory, mad};
    }

    public static double[] dd_mad_givenAlpha(double[] data, double alpha, int maxNumBins) {
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, alpha, maxNumBins);
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
        }
        mad = dd_median(data_exp, alpha, maxNumBins) - 1;
        return new double[]{memory, mad};
    }

    public static double[] dd_mad_givenAlpha_norm(int syn_type,long n, double MinV,double alpha, int maxNumBins) {
        NORM_TYPE=syn_type;
        NORM_N = n;
        NORM_TIME=0;
        NORM_MinV=MinV;
        memory = 0;
        median = dd_median_norm(alpha, maxNumBins);
//        System.out.println("\t\tdd median:\t"+median+"\t\t"+(median+MinV-1));
//        for (int i = 0; i < data_exp.length; i++) {
//            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
//        }
        mad = dd_median_norm_abs(alpha, maxNumBins) - 1;
        return new double[]{memory, mad};
    }

    public static double dd_median(double[] data, double alpha, int maxNumBins) {
        DDSketch sketch = new DDSketch(alpha, maxNumBins);
        for(double datum: data){
            sketch.insert(datum);
        }
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        double quantile = sketch.getQuantile(0.5);
        return quantile;
    }
    public static double dd_median_norm(double alpha, int maxNumBins) {
        DDSketch sketch = new DDSketch(alpha, maxNumBins);

        double[] synPage=new double[NORM_PAGE];
        for(int b=0;b<NORM_PARTITION;b++) {
            RandomGenerator rng = new Well44497b(b);
            AbstractRealDistribution dis;
            if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
            for(long Page=0;Page<NORM_N/NORM_PARTITION/NORM_PAGE;Page++){
                NORM_TIME-=System.nanoTime();
                if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=dis.sample() -NORM_MinV+1;
                else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.pow(dis.sample(),2) -NORM_MinV+1;
//                for(int i=0;i<NORM_PAGE;i++)
//                    synPage[i]=dis.sample() -NORM_MinV+1;
                NORM_TIME+=System.nanoTime();
                for(int i=0;i<NORM_PAGE;i++)
                    sketch.insert(synPage[i]);
            }
        }
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        double quantile = sketch.getQuantile(0.5);
        return quantile;
    }
    public static double dd_median_norm_abs(double alpha, int maxNumBins) {
        DDSketch sketch = new DDSketch(alpha, maxNumBins);

        double[] synPage=new double[NORM_PAGE];
        for(int b=0;b<NORM_PARTITION;b++) {
            RandomGenerator rng = new Well44497b(b);
            AbstractRealDistribution dis;
            if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
            for(long Page=0;Page<NORM_N/NORM_PARTITION/NORM_PAGE;Page++){
                NORM_TIME-=System.nanoTime();
                if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.abs(dis.sample() -NORM_MinV+1-median)+1;
                else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.abs(Math.pow(dis.sample(),2) -NORM_MinV+1-median)+1;
//                for(int i=0;i<NORM_PAGE;i++)
//                    synPage[i]=Math.abs((dis.sample() -NORM_MinV+1)-median)+1;
                NORM_TIME+=System.nanoTime();
                for(int i=0;i<NORM_PAGE;i++)
                    sketch.insert(synPage[i]);
            }
        }

        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        double quantile = sketch.getQuantile(0.5);
        return quantile;
    }
}