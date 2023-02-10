package benchmark;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import mad.CORESketch;

import java.util.Map;
import java.util.concurrent.*;


public class CORE_MAD {
    public static double memory = 0;
    public static double mad;
    public static double median;
    public static int MMP=2;
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
        while (true) {
            if (Math.ceil(Math.pow(2, card) * Math.log(max_value) / Math.log(2)) -
                    Math.floor(Math.pow(2, card) * Math.log(min_value) / Math.log(2))
                    > space_limit) {
                return card - 1;
            }
            card += 1;
        }
    }

    private static int calculate_card(double[] real_range, int space_limit) {
        int card = 0;
        while (true) {
            int space_expected = 0;
            for (int i = 0; i < real_range[6]; ++i) {
                space_expected += Math.ceil(Math.pow(2, card) * Math.log(real_range[2 * i + 1]) / Math.log(2)) -
                Math.floor(Math.pow(2, card) * Math.log(real_range[2 * i]) / Math.log(2));
            }
            if (space_expected > space_limit) {
                return card - 1;
            }
            card += 1;
        }
    }

    public static double[] core_mad(double[] data, double max_value, double min_value,
                                  int space_limit, boolean is_integer, boolean parallel) {
        mad = 0;
        median = 0;
        memory = 0;

        if (parallel) {
            core_mad_optimal(data, max_value, min_value, space_limit, is_integer);
        }
        else {
            core_mad_original(data, max_value, min_value, space_limit, is_integer);
        }
        return new double[]{memory,mad};
    }

    public static double[] core_mad(double[] data, double max_value, double min_value,
                                  int space_limit, boolean is_integer) {
        mad = 0;
        median = 0;
        memory = 0;
        return core_mad(data, max_value, min_value, space_limit, is_integer, false);
    }


    public static double[] core_mad_p(double[] data, double max_value, double min_value,
                                  int space_limit, boolean is_integer, boolean parallel) {
        mad = 0;
        median = 0;
        memory = 0;
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

    public static double core_mad_optimal(double[] data, double max_value, double min_value,
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
        return calculate_mad(data, sketch, is_integer);
//        return calculate_mad(data, sketch, is_integer);
    }

    public static double[] core_mad_original(double[] data, double max_value, double min_value, int space_limit, boolean is_integer) {
        memory = 0;
        double[] range = new double[] {min_value, max_value,
                min_value, max_value, min_value, max_value, 1};
        CORESketch sketch = finest_sketch(data, max_value, min_value, space_limit,
                is_integer, false, 0, data.length, 1, range).sketch;
        calculate_mad(data, sketch, is_integer);
        return new double[]{memory,mad};
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

    public static double getKth(DoubleArrayList data, int L, int R, int K){
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
            int m = sketch.mid_half_count_bucket();
            int[] lr = sketch.edge_half_count_bucket(m);
            if (is_integer && sketch.bucket_finest(m, lr[0], lr[1])) {
                return new SpacePair(thread_memory, sketch);
            }
            double[] next_range = sketch.generate_useful_range(m, lr[0], lr[1]);
            memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, memory);
            thread_memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, thread_memory);
            sketch = new CORESketch(0, space_limit, next_range);
            double[] real_range = sketch.real_range();
            card = calculate_card(real_range, space_limit);
            if (card == pre_card) {
                System.out.println("Larger bucket limit needed");
            }
            sketch.set_card(card);
        }
    }
}
