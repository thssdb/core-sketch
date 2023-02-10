package benchmark;

import mad.DDSketch;

public class DD_MAD {
    public static double memory = 0;
    public static double mad;
    public static double median;

    public static double dd_mad(double[] data, double epsilon, int maxNumBins) {
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, epsilon, maxNumBins);
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
        }
        mad = dd_median(data_exp, epsilon, maxNumBins) - 1;
        return mad;
    }
    public static double dd_mad_calcAlpha(double[] data, double maxV, int maxNumBins) {
        double alpha = Math.pow(10,Math.log10(maxV)/(maxNumBins*0.75))-1;
//        System.out.println("\t\t\t\tcalc alpha:\t"+Math.pow(1.0+alpha,(maxNumBins*0.75))+"\t\tmaxV:"+maxV);
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, alpha, maxNumBins);
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median) + 1;
        }
        mad = dd_median(data_exp, alpha, maxNumBins) - 1;
        return mad;
    }

    public static double dd_median(double[] data, double alpha, int maxNumBins) {
        DDSketch sketch = new DDSketch(alpha, maxNumBins);
        for(double datum: data){
            sketch.insert(datum);
        }
        memory = sketch.sketch_size() * 48 / 1024.0;
        double quantile = sketch.getQuantile(0.5);
        return quantile;
    }
}