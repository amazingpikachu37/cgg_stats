package Threads;

import java.io.Serial;
import java.io.Serializable;

public class TimeOutput implements Comparable<TimeOutput>, Serializable {
    @Serial
    private static final long serialVersionUID = -327487324L;
    public long time;
    public TimeOutput(long time) {
        this.time = time;
    }
    @Override
    public int compareTo(TimeOutput o) {
        long diff = this.time - o.time;
        if(diff > 0) return 1;
        if(diff < 0) return -1;
        return 0;
    }
    public static TimeOutput parseTimeOutput(String s) {
        String[] vals = s.replace(" ","").split("[d:.]");
        if(vals.length > 5 || vals.length <= 1) throw new NumberFormatException();
        long days = vals.length==5?Long.parseLong(vals[0]):0;
        int hours = vals.length>=4?Integer.parseInt(vals[vals.length-4]):0;
        int minutes = vals.length>=3?Integer.parseInt(vals[vals.length-3]):0;
        int seconds = Integer.parseInt(vals[vals.length-2]);
        int nanoseconds100 = Integer.parseInt(vals[vals.length-1]);
        return new TimeOutput(days*864000000000L+hours*36000000000L+minutes*600000000L+seconds*10000000L+nanoseconds100);
    }

    @Override
    public String toString() {
        long days = time/(86400L * 10000000);
        String hours = Long.toString((time%(86400L * 10000000))/(3600L * 10000000));
        String minutes = Long.toString((time%(3600L * 10000000))/(60L * 10000000));
        String seconds = Long.toString((time%(60L * 10000000))/(10000000));
        String nanoseconds100 = Long.toString((time%(10000000)));
        while(nanoseconds100.length()<7) nanoseconds100 = "0"+nanoseconds100;
        if(minutes.equals("0")&&hours.equals("0")&&days==0) return seconds+"."+nanoseconds100;
        if(seconds.length()==1) seconds = "0"+seconds;
        if(hours.equals("0")&&days==0) return minutes+":"+seconds+"."+nanoseconds100;
        if(minutes.length()==1) minutes = "0"+minutes;
        if(days==0) return hours+":"+minutes+":"+seconds+"."+nanoseconds100;
        if(hours.length()==1) hours = "0"+hours;
        return days+"d "+hours+":"+minutes+":"+seconds+"."+nanoseconds100;
    }
}
