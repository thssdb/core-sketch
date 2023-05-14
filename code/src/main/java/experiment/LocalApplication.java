package experiment;

import java.io.IOException;
import java.util.Date;

public class LocalApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        long MAIN_START_TIME = new Date().getTime();
        int test_case = 1;
//        System.out.println("\t\tmaxM:\t"+Runtime.getRuntime().maxMemory()/1024/1024.0+"\t\t"+Runtime.getRuntime().totalMemory());
        ApproxAndDataSizeExp.set_testCase(4);
        ApproxAndSpaceLimitExp.set_testCase(1);
        ApproxBoundExp.set_testCase(1);
        long START1 = new Date().getTime();
//        ApproxAndDataSizeExp.test_together();
        long START2 = new Date().getTime();
//        ApproxAndSpaceLimitExp.test_together();
//        ApproxSpaceAnoExp.test_together();
        long START3 = new Date().getTime();
//        ApproxBoundExp.test_together();
        ApproxBoundAnoExp.test_together();
//        BoomExp_Bucket_M.test_together();
        System.out.println("\n\n\t\tMAIN_ALL_TIME:\t"+(new Date().getTime()-MAIN_START_TIME)/1000.0+" s");
        System.out.println("\t\tTIME_1:\t"+(START2-START1));
        System.out.println("\t\tTIME_2:\t"+(START3-START2));
        System.out.println("\t\tTIME_3:\t"+(new Date().getTime()-START3));
//        NormExp.test_together();
    }
}