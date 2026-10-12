package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Test2 extends CountingThread {
    public Test2(ThreadData t) {
        super(t);
    }
    private final String[] counts_301_to_342 = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
                                                            "AA","AB","AC","AD","AE","AF","AG","AH","AI","AJ","AK","AL","AM","AN","AO","AP"};
    public String getCount(int num) {
        if(num<=300) return String.valueOf(num);
        if(num>=343) return String.valueOf(num-342);
        return counts_301_to_342[num-301];
    }
}
