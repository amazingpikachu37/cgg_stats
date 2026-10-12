package Stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileParser {
    public record FileData(List<Integer> updates_per_file, List<Integer> update_body_per_file, List<Integer> counts_per_file, List<Integer> count_body_per_file,
                           List<Integer> count_values, List<Long[]> first_and_last_updates, List<Long[]> first_and_last_counts) {
    }
    private static final List<FileData> file_data = new ArrayList<>();
    static {
        File f = new File("SidethreadData/FileData");
        try {
            Scanner in = new Scanner(f);
            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(" ");
                if (line[0].charAt(0) != '/') {
                    int thread_id = Integer.parseInt(line[0]);
                    String[] line2 = in.nextLine().strip().split(" ");
                    String[] line3 = in.nextLine().strip().split(" ");
                    String[] line4 = in.nextLine().strip().split(" ");
                    String[] line5 = in.nextLine().strip().split(" ");
                    String[] line6 = in.nextLine().strip().split(" ");
                    String[] line7 = in.nextLine().strip().split(" ");
                    List<Integer> updates_per_file = new ArrayList<>();
                    List<Integer> update_body_per_file = new ArrayList<>();
                    List<Integer> counts_per_file = new ArrayList<>();
                    List<Integer> count_body_per_file = new ArrayList<>();
                    List<Integer> count_values = new ArrayList<>(Arrays.asList(Integer.parseInt(line[1]), Integer.parseInt(line[2])));
                    List<Long[]> first_and_last_counts = new ArrayList<>();
                    List<Long[]> first_and_last_updates = new ArrayList<>();
                    for (String s: line2) {
                        if(s.isEmpty()) break;
                        updates_per_file.add(Integer.parseInt(s));
                    }
                    for (String s: line3) {
                        if(s.isEmpty()) break;
                        update_body_per_file.add(Integer.parseInt(s));
                    }
                    for (String s: line4) {
                        if(s.isEmpty()) break;
                        counts_per_file.add(Integer.parseInt(s));
                    }
                    for (String s: line5) {
                        if(s.isEmpty()) break;
                        count_body_per_file.add(Integer.parseInt(s));
                    }
                    for(int i=0;i<2*updates_per_file.size();i+=2) {
                        if(line6[i].isEmpty()) break;
                        first_and_last_updates.add(new Long[]{Long.parseLong(line6[i]), Long.parseLong(line6[i+1])});
                    }
                    for(int i=0;i<2*counts_per_file.size();i+=2) {
                        if(line7[i].isEmpty()) break;
                        first_and_last_counts.add(new Long[]{Long.parseLong(line7[i]), Long.parseLong(line7[i+1])});
                    }
                    FileData data = new FileData(updates_per_file, update_body_per_file, counts_per_file, count_body_per_file, count_values, first_and_last_updates, first_and_last_counts);
                    while(file_data.size() < thread_id+2) file_data.add(null);
                    file_data.set(thread_id+1, data);
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("CGGStats/SidethreadData/FileData is missing");
            throw new RuntimeException();
        }
    }
    public static FileData getFileData(int thread_id) {
        if(thread_id+1 >= file_data.size()) return null;
        return file_data.get(thread_id+1);
    }

    public static void updateFileData(int thread_id, FileData update) {
        String comment = """
                //This file is parsed line by line to gather information about the count/update files without having to check the files.
                //Each thread has seven lines.
                //The format for this is as follows:
                //    thread_id total_updates total_counts (can't be empty. 0 represents nonexistence)
                //    [msgs_per_update_file] (can be empty)
                //    [msgs_pere_update_body_file] (can be empty)
                //    [msgs_per_count_time_file] (can be empty)
                //    [msgs_per_count_body_file] (can be empty)
                //    [first and last timestamp of each update file] (must be exactly two per file. Zero length files are not permitted)
                //    [first and last timestamp of each count_time file] (must be exactly two per file. Zero length files are not permitted)""";
        while(file_data.size() < thread_id+2) file_data.add(null);
        file_data.set(thread_id+1, update);
        File f = new File("SidethreadData/FileData");
        try {
            PrintWriter fout = new PrintWriter(f);
            fout.println(comment);
            for(int i=0;i<file_data.size();i++) {
                if(file_data.get(i)==null) continue;
                fout.print(formatFileData(i));
            }
            fout.close();
        } catch(FileNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private static String formatFileData(int thread_id) {
        FileData data = file_data.get(thread_id);
        StringBuilder sb = new StringBuilder();
        sb.append(thread_id-1).append(" ");
        for(int x:data.count_values())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.updates_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.update_body_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.counts_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.count_body_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(Long[] x:data.first_and_last_updates()) {
            sb.append(x[0]).append(" ");
            sb.append(x[1]).append(" ");
        }
        sb.append("\n");
        for(Long[] x:data.first_and_last_counts()) {
            sb.append(x[0]).append(" ");
            sb.append(x[1]).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
