package benchmark;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import mad.CORESketch;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;


public class CORE_MAD {
    public static long NORM_N,NORM_TIME;
    public static int NORM_TYPE,NORM_PARTITION=4,NORM_PAGE=1000;
    public static double memory = 0;
    public static double mad;
    public static double median;
    public static int MMP=2;
    public static int iter_round;
    public static XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public static class SpacePair {
        public double thread_memory;
        public CORESketch sketch;
        public SpacePair(double thread_memory, CORESketch sketch) {
            this.thread_memory = thread_memory;
            this.sketch = sketch;
        }
    }

    private static int calculate_card(double max_value, double min_value, int space_limit) {
        int card = 0;
        while (/*true*/card<=30) {// 18 for gas or exp-datasize
            if (Math.ceil(Math.pow(2, card) * Math.log(max_value) / Math.log(2)) -
                    Math.floor(Math.pow(2, card) * Math.log(min_value) / Math.log(2))
                    > space_limit) {
                return card - 1;
            }
            card += 1;
//            if(card==Integer.lowestOneBit(card)){
//                System.out.println("\t\t\ttemp card is power of 2.\t"+card);
//                System.out.println("\t\t\t\t\tcheck:\t"+Math.ceil(Math.pow(2, card) * Math.log(max_value) / Math.log(2))+"\t-\t"+Math.floor(Math.pow(2, card) * Math.log(min_value) / Math.log(2))+"\t?\t"+space_limit+"\t\t\t//minV,maxV:"+min_value+","+max_value);
//            }
        }return card;
    }

    private static int calculate_card(double[] real_range, int space_limit) {
        int card = 0;
        while (/*true*/card<=30) {// 18 for gas or exp-datasize
            int space_expected = 0;
            for (int i = 0; i < real_range[6]; ++i) {
                space_expected += Math.ceil(Math.pow(2, card) * Math.log(real_range[2 * i + 1]) / Math.log(2)) -
                Math.floor(Math.pow(2, card) * Math.log(real_range[2 * i]) / Math.log(2));
            }
            if (space_expected > space_limit) {
                return card - 1;
            }
            card += 1;
//            if(card==Integer.lowestOneBit(card)){
//                System.out.println("\t\t\ttemp card is power of 2.\t"+card);
//                for (int i = 0; i < real_range[6]; ++i) {
//                    System.out.println("\t\t\t\t\trange" + i + "\trange:" + real_range[2 * i] + "," + real_range[2 * i + 1] + "\tspace_expected:" + (Math.ceil(Math.pow(2, card) * Math.log(real_range[2 * i + 1]) / Math.log(2)) - Math.floor(Math.pow(2, card) * Math.log(real_range[2 * i]) / Math.log(2))));
//                    System.out.println("\t\t\t\t\t\t"+Math.pow(2, card) * Math.log(real_range[2 * i + 1]) / Math.log(2));
//                }
//            }
        }return card;
    }

    public static double[] core_mad(double[] data, double max_value, double min_value,
                                    int space_limit, boolean is_integer, boolean parallel) {
        mad = 0;
        median = 0;
        memory = 0;
        iter_round = 1;
        if (parallel) {
            return core_mad_optimal(data, max_value, min_value, space_limit, is_integer);
        }
        else {
            return core_mad_original(data, max_value, min_value, space_limit, is_integer);
        }
    }
    public static double[] core_mad_norm(int syn_type,long n, double max_value, double min_value,
                                    int space_limit, boolean is_integer, boolean parallel) {
        NORM_TYPE=syn_type;
        NORM_N = n;
        NORM_TIME = 0;
        mad = 0;
        median = 0;
        memory = 0;
        iter_round = 1;
        if (parallel) {
            return core_mad_optimal_norm(max_value, min_value, space_limit, is_integer);
        }
        else {
            return core_mad_original_norm(max_value, min_value, space_limit, is_integer);
        }
    }

    public static double[] core_mad(double[] data, double max_value, double min_value,
                                  int space_limit, boolean is_integer) {
        mad = 0;
        median = 0;
        memory = 0;
        iter_round = 1;
        return core_mad(data, max_value, min_value, space_limit, is_integer, false);
    }


    public static double[] core_mad_p(double[] data, double max_value, double min_value,
                                  int space_limit, boolean is_integer, boolean parallel) {
        mad = 0;
        median = 0;
        memory = 0;
        iter_round = 1;
        if (parallel) return core_mad_optimal_p(data, max_value, min_value, space_limit, is_integer);
        else return core_mad_original(data, max_value, min_value, space_limit, is_integer);
    }


    public static double[] core_mad_optimal_p(double[] data, double max_value, double min_value,
                                          int space_limit, boolean is_integer) {
        memory = 0;
        Map<Integer, SpacePair> sketches = new ConcurrentHashMap<>();
        double[] range = new double[] {min_value, max_value,
                min_value, max_value, min_value, max_value, 1};

        if(MMP==2){
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, data.length / 2, 2,range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        data.length / 2, data.length, 2,range)))
            ).join();
        }else if(MMP==3) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, data.length, MMP, range)))
            ).join();
        }else if(MMP==4) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, each_length*3, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(4, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*3, data.length, MMP, range)))
            ).join();
        }else if(MMP==5) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, each_length*3, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(4, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*3, each_length*4, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(5, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*4, data.length, MMP, range)))
            ).join();
        }else if(MMP==6) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, each_length*3, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(4, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*3, each_length*4, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(5, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*4, each_length*5, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(6, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*5, data.length, MMP, range)))
            ).join();
        }else if(MMP==7) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, each_length*3, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(4, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*3, each_length*4, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(5, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*4, each_length*5, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(6, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*5, each_length*6, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(7, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*6, data.length, MMP, range)))
            ).join();
        }else if(MMP==8) {
            int each_length = data.length/MMP;
            int each_space = space_limit/MMP;
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                    sketches.put(1, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        0, each_length, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(2, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length, each_length*2, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(3, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*2, each_length*3, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(4, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*3, each_length*4, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(5, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*4, each_length*5, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(6, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*5, each_length*6, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(7, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*6, each_length*7, MMP, range))),
                CompletableFuture.runAsync(() ->
                    sketches.put(8, finest_sketch(data, max_value, min_value,
                        each_space, is_integer, true,
                        each_length*7, data.length, MMP, range)))
            ).join();
        }

        CORESketch sketch = sketches.get(1).sketch;
        for (int i = 2; i <= sketches.size(); i++)
        {
            sketch.merge(sketches.get(i).sketch);
        }
        sketch.half_count_buckets();
        calculate_mad(data, sketch, is_integer);
        return new double[]{memory,mad};
    }

    public static double[] core_mad_optimal(double[] data, double max_value, double min_value,
                                          int space_limit, boolean is_integer) {
        memory = 0;
        int thread = 4;
        Map<Integer, SpacePair> sketches = new ConcurrentHashMap<>();
//        System.out.println("Start");
        double[] range = new double[] {min_value, max_value,
                min_value, max_value, min_value, max_value, 1};
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                                sketches.put(1, finest_sketch(data, max_value, min_value,
                                        space_limit / thread, is_integer, true,
                                        0, data.length / thread, thread, range))),
                CompletableFuture.runAsync(() ->
                                sketches.put(2, finest_sketch(data, max_value, min_value,
                                        space_limit / thread, is_integer, true,
                                        data.length / thread, data.length * 2 / thread, thread, range))),
                CompletableFuture.runAsync(() ->
                                sketches.put(3, finest_sketch(data, max_value, min_value,
                                        space_limit / thread, is_integer, true,
                                        data.length * 2 / thread, (int)(data.length * 3L / thread), thread, range))),
                CompletableFuture.runAsync(() ->
                                sketches.put(4, finest_sketch(data, max_value, min_value,
                                        space_limit / thread, is_integer, true,
                                    (int)(data.length * 3L / thread), data.length, thread, range)))
        ).join();
        double total_size = sketches.get(1).thread_memory +
                sketches.get(2).thread_memory +
                sketches.get(3).thread_memory +
                sketches.get(4).thread_memory;
        memory = Math.max(memory, total_size);
        CORESketch sketch = sketches.get(1).sketch;
        for (int i = 2; i <= sketches.size(); i++)
        {
            sketch.merge(sketches.get(i).sketch);
        }
        memory = Math.max(memory, sketch.get_bucket_size() * 48.13 / 1024);
        sketch.max_bucket = space_limit;
//        sketch = finest_sketch(data, max_value, min_value, space_limit,
//                is_integer, true, 0, data.length,
//                1, sketch.real_range()).sketch;
        sketch.half_count_buckets();
        calculate_mad(data, sketch, is_integer);
        return new double[]{memory, mad};
//        return calculate_mad(data, sketch, is_integer);
    }

    public static double[] core_mad_optimal_norm(double max_value, double min_value,
                                            int space_limit, boolean is_integer) { // 4 thread
        memory = 0;
        Map<Integer, SpacePair> sketches = new ConcurrentHashMap<>();
//        System.out.println("Start");
//        double[] range = new double[] {min_value, max_value,
//            min_value, max_value, min_value, max_value, 1};
        double[] range = new double[] {1, max_value-min_value,
            1, max_value-min_value, 1, max_value-min_value, 1};
        CompletableFuture.allOf(
            CompletableFuture.runAsync(() ->
                sketches.put(1, finest_sketch_norm(1,NORM_N/NORM_PARTITION,0, max_value, min_value,
                    space_limit / NORM_PARTITION, is_integer, range))),
            CompletableFuture.runAsync(() ->
                sketches.put(2, finest_sketch_norm(1,NORM_N/NORM_PARTITION,1, max_value, min_value,
                    space_limit / NORM_PARTITION, is_integer, range))),
            CompletableFuture.runAsync(() ->
                sketches.put(3, finest_sketch_norm(1,NORM_N/NORM_PARTITION,2, max_value, min_value,
                    space_limit / NORM_PARTITION, is_integer, range))),
            CompletableFuture.runAsync(() ->
                sketches.put(4, finest_sketch_norm(1,NORM_N/NORM_PARTITION,3, max_value, min_value,
                    space_limit / NORM_PARTITION, is_integer, range)))
        ).join();
        double total_size = sketches.get(1).thread_memory +
            sketches.get(2).thread_memory +
            sketches.get(3).thread_memory +
            sketches.get(4).thread_memory;
        memory = Math.max(memory, total_size);
        CORESketch sketch = sketches.get(1).sketch;
        for (int i = 2; i <= sketches.size(); i++)
        {
            sketch.merge(sketches.get(i).sketch);
        }
        memory = Math.max(memory, sketch.get_bucket_size() * 48.13 / 1024);
        sketch.max_bucket = space_limit;
//        sketch = finest_sketch(data, max_value, min_value, space_limit,
//                is_integer, true, 0, data.length,
//                1, sketch.real_range()).sketch;
        sketch.half_count_buckets();
        calculate_mad_norm(sketch, min_value, is_integer);
        return new double[]{memory, mad};
//        return calculate_mad(data, sketch, is_integer);
    }

    public static double[] core_mad_original(double[] data, double max_value, double min_value, int space_limit, boolean is_integer) {
        memory = 0;
        double[] range = new double[] {min_value, max_value,
            min_value, max_value, min_value, max_value, 1};
        CORESketch sketch = finest_sketch(data, max_value, min_value, space_limit,
            is_integer, false, 0, data.length, 1, range).sketch;
        calculate_mad(data, sketch, is_integer);
        return new double[]{memory,mad,iter_round};
    }
    public static double[] core_mad_original_norm(double max_value, double min_value, int space_limit, boolean is_integer) {
        memory = 0;
        double[] range = new double[] {1, max_value-min_value,
            1, max_value-min_value, 1, max_value-min_value, 1};
        CORESketch sketch = finest_sketch_norm(4,NORM_N/NORM_PARTITION,0,max_value, min_value, space_limit,
            is_integer, range).sketch;//System.out.println("\t\\t\t\t----------------???????????___________");
        calculate_mad_norm(sketch, min_value,is_integer);
        return new double[]{memory,mad,iter_round};
    }

    private static double calculate_mad(double[] data, CORESketch sketch, boolean is_integer) {
        if (is_integer && sketch.bucket_finest()) {
//            System.out.println("CORE_SPACE: " + memory);
            return sketch.mad();
//            System.out.println(sketch.mad());
//            return memory;
        }
        double[] useful_range = sketch.get_range();
        sketch = new CORESketch();
//        List<Double> queue = new ArrayList<>();
        DoubleArrayList queue = new DoubleArrayList(data.length);

        sketch.set_range(useful_range);
//        System.out.println("\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);
        long rank = 0;
        for (double datum : data){
            sketch.insert_mid(datum);
            if (sketch.in_range(datum, useful_range)) {
                queue.add(datum);
            }
        }
//        queue.trim(queue.size());
//        System.out.println(queue.size());
        memory = Math.max(memory, ((double)queue.size()) * 8 / 1024);
//        System.out.println("\t\t\t\tafter in range check. |queue|="+queue.size());
        long[] mid_num = sketch.get_gap();
        int n_queue = queue.size(), median_rank = (data.length - 1) / 2 - (int)mid_num[1] - (int)mid_num[0];
        if(median_rank < 0)median_rank += (int)mid_num[1];
        assert median_rank >= 0;
//        System.out.println("\t\t\tmid_rank:"+median_rank);
        median = getKth(queue, 0,n_queue,median_rank);
        queue.replaceAll(aDouble -> Math.abs(aDouble - median));
        double min_mad = Math.max(median - useful_range[1], useful_range[4] - median);
        int mad_rank = (data.length - 1) / 2 - (int)mid_num[1] - (int)mid_num[2];
        if(mad_rank < 0) mad_rank += (int)mid_num[1] + (int)mid_num[2];
        mad = getKth(queue,0,n_queue,mad_rank);
//        System.out.println("CORE\t\tmad:\t" +mad+"\tmedian:\t"+median);
        return /*memory*/mad;
    }

    private static double calculate_mad_norm(CORESketch sketch, double min_value, boolean is_integer) {
        if (is_integer && sketch.bucket_finest()) {
//            System.out.println("CORE_SPACE: " + memory);
            return sketch.mad();
//            System.out.println(sketch.mad());
//            return memory;
        }
        double[] useful_range = sketch.get_range();
        sketch = new CORESketch();
//        List<Double> queue = new ArrayList<>();
        DoubleArrayList queue = new DoubleArrayList();

        sketch.set_range(useful_range);
//        System.out.println("\t\t\tcnt freemem:"+Runtime.getRuntime().freeMemory()/1024/1024+"\t\tmaxmem:"+Runtime.getRuntime().maxMemory()/1024/1024+"\t\t.totmem:"+Runtime.getRuntime().totalMemory()/1024/1024);
        long rank = 0;


        double datum;
        double[] synPage=new double[NORM_PAGE];
        for(int b=0;b<NORM_PARTITION;b++) {
            RandomGenerator rng = new Well44497b(b);
            AbstractRealDistribution dis;
            if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);

//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
            for(long Page=0;Page<NORM_N/NORM_PARTITION/NORM_PAGE;Page++){
                NORM_TIME-=System.nanoTime();
                if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=dis.sample() -min_value+1;
                else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.pow(dis.sample(),2) -min_value+1;
                NORM_TIME+=System.nanoTime();
                for(int i=0;i<NORM_PAGE;i++) {
                    datum=synPage[i];
                    sketch.insert_mid(datum);
                    if (sketch.in_range(datum, useful_range)) {
                        queue.add(datum);
                    }
                }
            }
        }
//        for(int b=0;b<NORM_PARTITION;b++) {
//            XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(b);
//            for (long i = 0; i < NORM_N/NORM_PARTITION; i++) {
//                datum=rnd.nextGaussian() * 2 + 4 - min_value + 1;
//                sketch.insert_mid(datum);
//                if (sketch.in_range(datum, useful_range)) {
//                    queue.add(datum);
//                }
//            }
//        }

//        queue.trim(queue.size());
//        System.out.println("queue-size:\t"+queue.size());
        memory = Math.max(memory, ((double)queue.size()) * 8 / 1024);
//        System.out.println("\t\t\t\tafter in range check. |queue|="+queue.size());
        long[] mid_num = sketch.get_gap();
        long n_queue = queue.size(), median_rank = (NORM_N - 1) / 2 - mid_num[1] - mid_num[0];
        if(median_rank < 0)median_rank += mid_num[1];
        assert median_rank >= 0;
//        System.out.println("\t\t\tmid_rank:"+median_rank);
        median = getKth(queue, 0,(int)n_queue,(int)median_rank);
        queue.replaceAll(aDouble -> Math.abs(aDouble - median));
        double min_mad = Math.max(median - useful_range[1], useful_range[4] - median);
        long mad_rank = (NORM_N - 1) / 2 - mid_num[1] - mid_num[2];
        if(mad_rank < 0) mad_rank += mid_num[1] + mid_num[2];
        mad = getKth(queue,0,(int)n_queue,(int)mad_rank);
//        System.out.println("\tCORE_norm\t\tmad:\t" +mad+"\tmedian:\t"+median);
        return /*memory*/mad;
    }

    private static double calculate_mad_optimal(double[] data, CORESketch sketch, boolean is_integer) {
        if (is_integer && sketch.bucket_finest()) {
//            System.out.println("CORE_SPACE: " + memory);
            return sketch.mad();
//            System.out.println(sketch.mad());
//            return memory;
        }
        double[] useful_range = sketch.get_range();
//        sketch = new CORESketch();
//        List<Double> queue = new ArrayList<>();
        DoubleArrayList queue = new DoubleArrayList(data.length);

        Map<Integer, DoubleArrayList> queues = new ConcurrentHashMap<>();
        Map<Integer, CORESketch> sketches = new ConcurrentHashMap<>();
        for (int i = 1; i < 5; i++) {
            sketches.put(i, new CORESketch());
            queues.put(i, new DoubleArrayList(data.length / 4));
        }
        for (int i = 1; i < 5; i++) {
            sketches.get(i).set_range(useful_range);
        }
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() ->
                {
                    for (int i = 0; i < data.length / 4; i++) {
                        sketches.get(1).insert_mid(data[i]);
                        if (sketches.get(1).in_range(data[i], useful_range)) {
                            queues.get(1).add(data[i]);
                        }
                    }
                }),
                CompletableFuture.runAsync(() ->
                {
                    for (int i = data.length / 4; i < data.length / 2; i++) {
                        sketches.get(2).insert_mid(data[i]);
                        if (sketches.get(2).in_range(data[i], useful_range)) {
                            queues.get(2).add(data[i]);
                        }
                    }
                }),
                CompletableFuture.runAsync(() ->
                {
                    for (int i = data.length / 2; i < data.length * 3 / 4; i++) {
                        sketches.get(3).insert_mid(data[i]);
                        if (sketches.get(3).in_range(data[i], useful_range)) {
                            queues.get(3).add(data[i]);
                        }
                    }
                }),
                CompletableFuture.runAsync(() ->
                {
                    for (int i = data.length * 3 / 4; i < data.length; i++) {
                        sketches.get(4).insert_mid(data[i]);
                        if (sketches.get(4).in_range(data[i], useful_range)) {
                            queues.get(4).add(data[i]);
                        }
                    }
                })
        ).join();

        long[] mid_num = new long[4];
        for (int ll = 1; ll < 5; ll++) {
            for (int jj = 0; jj < 4; jj++) {
                mid_num[jj] = mid_num[jj] + sketches.get(ll).get_gap()[jj];
            }
            queue.addAll(queues.get(ll));
        }

//        queue.trim(queue.size());
        memory = Math.max(memory, ((double)queue.size()) * 8 / 1024);
//        System.out.println("\t\t\t\tafter in range check. |queue|="+queue.size());
//        long[] mid_num = sketch.get_gap();
        int n_queue = queue.size(), median_rank = (data.length - 1) / 2 - (int)mid_num[1] - (int)mid_num[0];
        if(median_rank < 0)median_rank += (int)mid_num[1];
        assert median_rank >= 0;
//        System.out.println("\t\t\tmid_rank:"+median_rank);
        median = getKth(queue, 0,n_queue,median_rank);
        queue.replaceAll(aDouble -> Math.abs(aDouble - median));
        double min_mad = Math.max(median - useful_range[1], useful_range[4] - median);
        int mad_rank = (data.length - 1) / 2 - (int)mid_num[1] - (int)mid_num[2];
        if(mad_rank < 0) mad_rank += (int)mid_num[1] + (int)mid_num[2];
        mad = getKth(queue,0,n_queue,mad_rank);
//        System.out.println("CORE\t\tmad:\t" +mad+"\tmedian:\t"+median);
        return /*memory*/mad;
    }

    public static double getKth(DoubleArrayList data, int L, int R, int K){
        if(L>=R)return data.getDouble(L);
        int pos = L + random.nextInt(R - L);
        double pivot_v = data.getDouble(pos), swap_v;

        int leP = L,eqR = R;
        data.set(pos,data.set(--eqR, pivot_v)); //   [L,leP): < pivot_v ;    [eqR,R): == pivot_v ;

        for(int i = L; i < eqR; i++)
            if((swap_v = data.getDouble(i)) < pivot_v)
                data.set(i,data.set(leP++, swap_v));
            else if(swap_v == pivot_v){
                data.set(i--, data.set(--eqR, swap_v));
            }

//        if(R-eqR>1)System.out.println("\t\t\t\tk_select. same pivot v.  count:"+(R-eqR));
        if(K < leP - L) return getKth(data, L, leP, K);
        if(K >= (leP - L) + (R - eqR)) return getKth(data, leP, eqR, K - (leP - L) - (R - eqR));
        return pivot_v;
    }

    private static SpacePair finest_sketch(double[] data, double max_value, double min_value, int space_limit, boolean is_integer, boolean parallel, int pos, int des, int threads, double[] range) {
        double thread_memory = 0;
        int card = 0;
        if (range[0] == min_value && range[1] == max_value) {
            card = calculate_card(max_value, min_value, space_limit);
        }
        else {
            card = calculate_card(range, space_limit);
        }
        CORESketch sketch = new CORESketch(card, space_limit, range);
        while (true) {
            double pre_card = sketch.card;
            for (int index = pos; index < des; index++) {
                sketch.insert(data[index]);
            }
            memory = Math.max(memory, sketch.get_bucket_size() * 48.13 / 1024);
            thread_memory = Math.max(thread_memory, sketch.get_bucket_size() * 48.13 / 1024);
            if (sketch.data_read(parallel, threads)) {
//                System.out.println(pos);
                return new SpacePair(thread_memory, sketch);
            }
            iter_round += 1;
            int m = sketch.mid_half_count_bucket();
            int[] lr = sketch.edge_half_count_bucket(m);
            if (is_integer && sketch.bucket_finest(m, lr[0], lr[1])) {
                return new SpacePair(thread_memory, sketch);
            }
            double[] next_range = sketch.generate_useful_range(m, lr[0], lr[1]);
            memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, memory);
            thread_memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, thread_memory);
            CORESketch pre_sketch=sketch;
            sketch = new CORESketch(0, space_limit, next_range);
            double[] real_range = sketch.real_range();
            card = calculate_card(real_range, space_limit);
            if (card == pre_card) {
//                System.out.println("Larger bucket limit needed");
//                throw new RuntimeException("Larger bucket limit needed");
                sketch.set_card(card);
                return new SpacePair(thread_memory,pre_sketch);
            }
            sketch.set_card(card);
//            System.out.println("\t\tcntCard:\t"+card);
        }
    }


    private static SpacePair finest_sketch_norm(int BATCH_NUM,long BATCH_N,int BATCH_SEED_START,double max_value, double min_value, int space_limit, boolean is_integer, double[] range) {
        double thread_memory = 0;
        int card = 0;
        if (range[0] == 1 && range[1] == max_value-min_value+1) {
            card = calculate_card(max_value-min_value+1, 1, space_limit);
        }
        else {
            card = calculate_card(range, space_limit);
        }
        CORESketch sketch = new CORESketch(card, space_limit, range);
        while (true) {
            double pre_card = sketch.card;


            double[] synPage=new double[NORM_PAGE];
            for(int b=0;b<BATCH_NUM;b++) {
                RandomGenerator rng = new Well44497b(BATCH_SEED_START+b);
                AbstractRealDistribution dis;
                if(NORM_TYPE==0)dis = new NormalDistribution(rng ,4, 2);else if(NORM_TYPE==1)dis = new ParetoDistribution(rng,1, 1);else dis=new NormalDistribution(rng,0,1);//dis=new ChiSquaredDistribution(rng,1);
//                XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(BATCH_SEED_START+b);
                for(long Page=0;Page<BATCH_N/NORM_PAGE;Page++){
                    if(BATCH_SEED_START==0)NORM_TIME-=System.nanoTime();
                    if(NORM_TYPE!=2)for(int i=0;i<NORM_PAGE;i++)synPage[i]=dis.sample() -min_value+1;
                    else for(int i=0;i<NORM_PAGE;i++)synPage[i]=Math.pow(dis.sample(),2) -min_value+1;
                    if(BATCH_SEED_START==0)NORM_TIME+=System.nanoTime();
                    for(int i=0;i<NORM_PAGE;i++)
                        sketch.insert(synPage[i]);
                }
            }
//            for(int b=0;b<BATCH_NUM;b++) {
//                XoRoShiRo128PlusRandom rnd = new XoRoShiRo128PlusRandom(BATCH_SEED_START+b);
//                for (long i = 0; i < BATCH_N; i++) {
//                    sketch.insert(rnd.nextGaussian() * 2 + 4 -min_value + 1);
//                }
//            }


            memory = Math.max(memory, sketch.get_bucket_size() * 48.13 / 1024);
            thread_memory = Math.max(thread_memory, sketch.get_bucket_size() * 48.13 / 1024);
            if (sketch.data_read_norm()) {
//                System.out.println(pos);
                return new SpacePair(thread_memory, sketch);
            }
            iter_round += 1;
            int m = sketch.mid_half_count_bucket();
            int[] lr = sketch.edge_half_count_bucket(m);
            if (is_integer && sketch.bucket_finest(m, lr[0], lr[1])) {
                return new SpacePair(thread_memory, sketch);
            }
            double[] next_range = sketch.generate_useful_range(m, lr[0], lr[1]);
//            System.out.println("\t\titer:"+iter_round+"\tnext_range:\t"+ Arrays.toString(next_range));
            memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, memory);
            thread_memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, thread_memory);
            sketch = new CORESketch(0, space_limit, next_range);
            double[] real_range = sketch.real_range();
            card = calculate_card(real_range, space_limit);
            if (card == pre_card) {
//                System.out.println("Larger bucket limit needed");
                throw new RuntimeException("Larger bucket limit needed");
            }
            sketch.set_card(card);
//            System.out.println("\t\tcntCard:\t"+card);
        }
    }
}
