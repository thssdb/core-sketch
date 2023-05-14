package benchmark;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import utils.Mad;
import mad.TPSketch;

import static utils.FileHelper.GET_SIZE;

public class TP_MAD {
    public static long NORM_N,NORM_TIME;
    public static int NORM_TYPE,NORM_PARTITION=4,NORM_PAGE=1000;
    public static double median;
    public static double memory;

    public static double[] tp_mad(double[] nums, double epsilon, int maxNumBins) {
        memory = 0;

        TPSketch sketch = new TPSketch(epsilon, maxNumBins);

        for(double num: nums){
            sketch.insert(num);
        }

        Mad mad = sketch.getMad();
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        median = sketch.getMedian();
//        System.out.println(mad);
        if(mad.error_bound > epsilon && sketch.needTwoPass()){
            double[] bounds = sketch.getValid_range();

            sketch = new TPSketch(sketch.getBeta() * epsilon, maxNumBins);

            for(double num: nums){
                sketch.insert(num, bounds);
            }

            mad = sketch.getMad();
            memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
            median = sketch.getMedian();
        }
        return new double[]{memory, mad.result};
    }

    public static double[] tp_mad_calcAlpha(double[] nums, double maxV, int maxNumBins) {
        memory = 0;

        double epsilon = Math.pow(10,Math.log10(maxV)/(maxNumBins*0.75))-1;

        TPSketch sketch = new TPSketch(epsilon, maxNumBins);

        for(double num: nums){
            sketch.insert(num);
        }

        Mad mad = sketch.getMad();
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        median = sketch.getMedian();
//        System.out.println(mad);
        if(mad.error_bound > epsilon && sketch.needTwoPass()){
            double[] bounds = sketch.getValid_range();

            sketch = new TPSketch(sketch.getBeta() * epsilon, maxNumBins);

            for(double num: nums){
                sketch.insert(num, bounds);
            }

            mad = sketch.getMad();
            memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
            median = sketch.getMedian();
        }
        return new double[]{memory, mad.result};
    }

    public static double[] tp_mad_givenAlpha(double[] nums, double epsilon, int maxNumBins) {
        memory = 0;

        TPSketch sketch = new TPSketch(epsilon, maxNumBins);

        for(double num: nums){
            sketch.insert(num);
        }

        Mad mad = sketch.getMad();
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        median = sketch.getMedian();
//        System.out.println(mad);
        if(mad.error_bound > epsilon && sketch.needTwoPass()){
            double[] bounds = sketch.getValid_range();

            sketch = new TPSketch(sketch.getBeta() * epsilon, maxNumBins);

            for(double num: nums){
                sketch.insert(num, bounds);
            }

            mad = sketch.getMad();
            memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
            median = sketch.getMedian();
        }
        return new double[]{memory, mad.result};
    }

    public static double[] tp_mad_givenAlpha_norm(int syn_type,long n, double MinV,double epsilon, int maxNumBins) {
        NORM_TYPE=syn_type;
        NORM_N = n;
        NORM_TIME=0;
        memory = 0;

        TPSketch sketch = new TPSketch(epsilon, maxNumBins);


        double[] synPage=new double[NORM_PAGE];
        for(int b=0;b<NORM_PARTITION;b++) {
            RandomGenerator rng = new Well44497b(b);
            AbstractRealDistribution dis;
            if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
            for(long Page=0;Page<NORM_N/NORM_PARTITION/NORM_PAGE;Page++){
                NORM_TIME-=System.nanoTime();
                if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=dis.sample() -MinV+1;
                else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.pow(dis.sample(),2) -MinV+1;
                NORM_TIME+=System.nanoTime();
                for(int i=0;i<NORM_PAGE;i++)
                    sketch.insert(synPage[i]);
            }
        }

//        for(int b=0;b<NORM_PARTITION;b++) {
//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
//            for (long i = 0; i < NORM_N/NORM_PARTITION; i++) {
//                sketch.insert(rnd.nextGaussian() * 2 + 4 -MinV +1);
//            }
//        }

        Mad mad = sketch.getMad();
        memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
        median = sketch.getMedian();
//        System.out.println(mad);
        if(mad.error_bound > epsilon && sketch.needTwoPass()){
            double[] bounds = sketch.getValid_range();

            sketch = new TPSketch(sketch.getBeta() * epsilon, maxNumBins);



            synPage=new double[NORM_PAGE];
            for(int b=0;b<NORM_PARTITION;b++) {
                RandomGenerator rng = new Well44497b(b);
                AbstractRealDistribution dis;
                if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
//                XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
                for(long Page=0;Page<NORM_N/NORM_PARTITION/NORM_PAGE;Page++){
                    NORM_TIME-=System.nanoTime();
                    if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=dis.sample() -MinV+1;
                    else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.pow(dis.sample(),2) -MinV+1;
                    NORM_TIME+=System.nanoTime();
                    for(int i=0;i<NORM_PAGE;i++)
                        sketch.insert(synPage[i],bounds);
//                        sketch.insert(synPage[i]);
                }
            }

            mad = sketch.getMad();
            memory = Math.max(memory, sketch.sketch_size() * 48 / 1024.0);
            median = sketch.getMedian();
        }
//        System.out.println("\t\ttp median:\t"+median+"\t\t"+(median+MinV-1));
        return new double[]{memory, mad.result};
    }
}
