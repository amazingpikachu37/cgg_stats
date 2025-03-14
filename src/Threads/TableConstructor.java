package Threads;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TableConstructor {
    public static String constructRedditTable(Map<String, ? extends Comparable> hoc, String[] headers, boolean reversed, String focused_user, int range) {
        if(range < 1) range = 1;
        else if(range > 10) range = 10;
        TreeMap<String, Comparable> ordered_hoc;
        if(reversed) {
            ordered_hoc = new TreeMap<>(new ValueComparator(hoc).reversed());
        } else {
            ordered_hoc = new TreeMap<>(new ValueComparator(hoc));
        }
        ordered_hoc.putAll(hoc);
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        String[] keyList = ordered_hoc.keySet().toArray(new String[0]);
        if(keyList.length == 0) return null;
        int user_loc = -1;
        int deleted = 0;
        for(int i=0;i<keyList.length;i++) {
            if(keyList[i].equals("[deleted]")) {
                deleted = i;
            }
            if(keyList[i].equals(focused_user)) {
                user_loc = i;
                break;
            }
        }
        int sub_for_deleted = 0;
        for(int i=0;i<Math.min(Math.max(user_loc+range+1,3),keyList.length);i++) {
            if (i == 3 && user_loc != -1 && user_loc - range > 3) {
                i = user_loc - range;
                base.append("...|".repeat(headers.length + 1));
                base.append("...\n");
                if(deleted <= i && deleted!=0) {
                    sub_for_deleted = 1;
                }
            }
            if (keyList[i].equals("[deleted]")) {
                base.append(String.format(" |%s|%s\n", keyList[i], ordered_hoc.get(keyList[i])));
                sub_for_deleted = 1;
            } else if (i == user_loc) {
                String temp = String.format("***%s|%s|%s***\n", i + 1 - sub_for_deleted, keyList[i], ordered_hoc.get(keyList[i]));
                base.append(temp.replace("|", "***|***"));
            } else {
                base.append(String.format("%s|%s|%s\n", i + 1 - sub_for_deleted, keyList[i], ordered_hoc.get(keyList[i])));
            }
        }
        return base.toString();
    }

    public static String constructRedditTable(Map<String, ? extends Comparable> hoc, String[] headers, boolean reversed, boolean slash_u_slash) {
        TreeMap<String, Comparable> ordered_hoc;
        if(reversed) {
            ordered_hoc = new TreeMap<>(new ValueComparator(hoc).reversed());
        } else {
            ordered_hoc = new TreeMap<>(new ValueComparator(hoc));
        }
        ordered_hoc.putAll(hoc);
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        int counter = 0;
        for(String key:ordered_hoc.keySet()) {
            if(key.equals("[deleted]")) {
                base.append(String.format(" |%s|%s\n", key, ordered_hoc.get(key)));
            } else {
                counter++;
                if(slash_u_slash) {
                    base.append(String.format("%s|@%s|%s\n", counter, key, ordered_hoc.get(key)));
                } else {
                    base.append(String.format("%s|%s|%s\n", counter, key, ordered_hoc.get(key)));
                }
            }
        }
        return base.toString();
    }
    public static String constructRedditTable(List<? extends NonUnique> hoc, String[] headers, boolean reversed, String focused_user, int range) {
        if(range < 1) range = 1;
        else if(range > 10) range = 10;
        if(reversed) {
            hoc.sort(Comparator.reverseOrder());
        } else {
            hoc.sort(null);
        }
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        if(hoc.size() == 0) return null;
        int user_loc = -1;
        int deleted = 0;
        for(int i=0;i<hoc.size();i++) {
            if(hoc.get(i).user().equals("[deleted]")) {
                deleted = i;
            }
            if(hoc.get(i).user().equals(focused_user)) {
                user_loc = i;
                break;
            }
        }
        int sub_for_deleted = 0;
        for(int i=0;i<Math.min(Math.max(user_loc+range+1,3),hoc.size());i++) {
            if (i == 3 && user_loc != -1 && user_loc - range > 3) {
                i = user_loc - range;
                base.append("...|".repeat(headers.length + 1));
                base.append("...\n");
                if(deleted <= i && deleted!=0) {
                    sub_for_deleted = 1;
                }
            }
            if (hoc.get(i).user().equals("[deleted]")) {
                base.append(String.format(" |%s\n", hoc.get(i)));
                sub_for_deleted = 1;
            } else if (i == user_loc) {
                String temp = String.format("***%s|%s***\n", i + 1 - sub_for_deleted, hoc.get(i));
                base.append(temp.replace("|", "***|***"));
            } else {
                base.append(String.format("%s|%s\n", i + 1 - sub_for_deleted, hoc.get(i)));
            }
        }
        return base.toString();
    }

    public static String constructRedditTable(List<? extends NonUnique> hoc, String[] headers, boolean reversed, int position, int range) {
        if(range < 1) range = 1;
        else if(range > 10) range = 10;
        if(position < 1 || position >= hoc.size()) position = 1;
        position--;
        if(reversed) {
            hoc.sort(Comparator.reverseOrder());
        } else {
            hoc.sort(null);
        }
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        if(hoc.size() == 0) return null;
        for(int i=0;i<Math.min(Math.max(position+range+1,3),hoc.size());i++) {
            if (i == 3 && position - range > 3) {
                i = position - range;
                base.append("...|".repeat(headers.length + 1));
                base.append("...\n");
            }
            if (i == position) {
                String temp = String.format("***%s|%s***\n", i + 1, hoc.get(i));
                base.append(temp.replace("|", "***|***"));
            } else {
                base.append(String.format("%s|%s\n", i + 1, hoc.get(i)));
            }
        }
        return base.toString();
    }
    public static String constructRedditTable(List<? extends NonUnique> hoc, String[] headers, boolean reversed, boolean slash_u_slash) {
        if(reversed) {
            hoc.sort(Comparator.reverseOrder());
        } else {
            hoc.sort(null);
        }
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        int counter = 0;
        for(NonUnique key:hoc) {
            if(key.user().equals("[deleted]")) {
                base.append(String.format(" |%s\n", key));
            } else {
                counter++;
                if(slash_u_slash) {
                    base.append(String.format("%s|@%s\n", counter, key));
                } else {
                    base.append(String.format("%s|%s\n", counter, key));
                }
            }
        }
        return base.toString();
    }
    public static String constructRedditTable(GenericStatOutput data, String[] headers, boolean reversed, boolean slash_u_slash) {
        if(reversed) data.sortDataReversed();
        else data.sortData();
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : data.table_headers) {
            base.append("|").append(header);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        int counter = 0;
        for(StatOutput key:data.dataList) {
            counter++;
            base.append(String.format("%s|%s\n", counter, data.formatData(key)));
        }
        return base.toString();
    }
    public static String constructSplitsTable(StatLoader.SplitsOutput data) {
        List<TimeOutput> times = data.splits();
        Long total = 0L;
        StringBuilder base = new StringBuilder("\\#|Time\n:--|:--\n");
        for(int i=0;i<times.size();i++) {
            if(times.get(i)==null) {
                total = null;
            }
            if(total!=null) {
                total += times.get(i).time;
            }
            base.append(i+1).append("|").append(times.get(i)).append('\n');
        }
        if(total!=null) {
            base.append("\n").append("Total time: ").append(new TimeOutput(total));
        }
        return base.toString();
    }
    public static String constructRedditTableFromList(List<?> data, String[] headers) {
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append(":-:|");
        }
        base.append("\n--:|").append(row2).append("\n");
        int counter = 0;
        for(Object key:data) {
            counter++;
            base.append(String.format("%s|%s\n", counter, key));
        }
        return base.toString();
    }
    public static String constructRedditTableWithURLsFromList(List<?> data, String[] headers, List<String> urls) {
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (String header : headers) {
            base.append("|").append(header);
            row2.append(":-:|");
        }
        base.append("\n--:|").append(row2).append("\n");
        int counter = 0;
        for(int i=0;i<data.size();i++) {
            counter++;
            base.append(String.format("[%s](%s)|%s\n", counter, urls.get(i), data.get(i)));
        }
        return base.toString();
    }
}
