package benchmark;

import utils.Mad;
import mad.TPSketch;

import static utils.FileHelper.GET_SIZE;

public class TP_MAD {
    public static int first_memory;
    public static int second_memory;
    public static long first_time;
    public static long second_time;
    public static double first_result;
    public static double first_bucket_num;
    public static double second_bucket_num;
    public static double median;

    public static double mad(double[] nums, double epsilon, int maxNumBins) {
        first_time = 0;
        second_time = 0;
        first_memory = 0;
        second_memory = 0;
        first_result = 0;

        long time = System.nanoTime();
        TPSketch sketch = new TPSketch(epsilon, maxNumBins);
        first_time += (System.nanoTime() - time);

        for(double num: nums){
            time = System.nanoTime();
            sketch.insert(num);
            first_time += (System.nanoTime() - time);
        }

        time = System.nanoTime();
        Mad mad = sketch.getMad();
        first_time += (System.nanoTime() - time);
        first_memory = GET_SIZE(sketch);
        first_result = mad.result;
        first_bucket_num = sketch.sketch_size();
        median = sketch.getMedian();
//        System.out.println(mad);
        if(mad.error_bound > epsilon && sketch.needTwoPass()){
            time = System.nanoTime();
            double[] bounds = sketch.getValid_range();
            second_time += (System.nanoTime() - time);

            time = System.nanoTime();
            sketch = new TPSketch(sketch.getBeta() * epsilon, maxNumBins);
            second_time += (System.nanoTime() - time);

            for(double num: nums){
                time = System.nanoTime();
                sketch.insert(num, bounds);
                second_time += (System.nanoTime() - time);
            }

            time = System.nanoTime();
            mad = sketch.getMad();
            second_time += (System.nanoTime() - time);
            second_memory = GET_SIZE(sketch);
            second_bucket_num = sketch.sketch_size();
            median = sketch.getMedian();
        }
        return mad.result;
    }
}
