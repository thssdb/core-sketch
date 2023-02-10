package dataset;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.io.*;
import java.util.Date;

public class synthetic {

    public static void main(String[] args){
        Long ST = new Date().getTime();
//        generateNormData(1000000000, 1);
//        generateParetoData(1000000000, 1);
        generateChiSquareData(1000000000, 1);
        System.out.println("\t\t\t\tALL_TIME:"+(new Date().getTime()-ST));
    }

    public static void generateChiSquareData(int n, int q) {
//        FileWriter fw;
        ChiSquaredDistribution chi = new ChiSquaredDistribution(1);
        try {
//            FileWriter fw = new FileWriter("D:\\Study\\Lab\\iotdb\\MAD-space\\exp-code\\core_sketch\\dataset\\chi" + q + ".csv");
            BufferedWriter fw = new BufferedWriter(new FileWriter("E:\\MAD-data\\chi" + q + ".csv"),5*1024*1024);

            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(chi.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateNormData(int n, int q) {
        NormalDistribution norm = new NormalDistribution(4, 2);
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter("E:\\MAD-data\\norm" + q + ".csv"),5*1024*1024);
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(norm.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateParetoData(int n, int q) {
        ParetoDistribution pareto = new ParetoDistribution(1, 1);
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter("E:\\MAD-data\\pareto" + q + ".csv"),5*1024*1024);
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(pareto.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
