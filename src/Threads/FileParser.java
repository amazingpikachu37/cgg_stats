package Threads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FileParser {
    private static final HashMap<String, FileData> file_data = new HashMap<>();
    static {
        File f = new File("SidethreadData//FileData");
        try {
            Scanner in = new Scanner(f);
            while (in.hasNextLine()) {
                String[] line = in.nextLine().split(" ");
                if (line[0].charAt(0) != '/') {
                    String[] line2 = in.nextLine().strip().split(" ");
                    String[] line3 = in.nextLine().strip().split(" ");
                    String[] line4 = in.nextLine().strip().split(" ");
                    String[] line5 = in.nextLine().strip().split(" ");
                    ArrayList<Integer> updates_per_file = new ArrayList<>();
                    ArrayList<Integer> counts_per_file = new ArrayList<>();
                    ArrayList<Integer> count_values = new ArrayList<>(Arrays.asList(Integer.parseInt(line[1]), Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4])));
                    ArrayList<String[]> first_and_last_counts = new ArrayList<>();
                    ArrayList<String[]> first_and_last_updates = new ArrayList<>();
                    for (String s: line2) {
                        if(s.equals("")) break;
                        updates_per_file.add(Integer.parseInt(s));
                    }
                    for (String s: line3) {
                        if(s.equals("")) break;
                        counts_per_file.add(Integer.parseInt(s));
                    }
                    for(int i=0;i<2*updates_per_file.size();i+=2) {
                        if(line4[i].equals("")) break;
                        first_and_last_updates.add(new String[]{line4[i], line4[i+1]});
                    }
                    for(int i=0;i<2*counts_per_file.size();i+=2) {
                        if(line5[i].equals("")) break;
                        first_and_last_counts.add(new String[]{line5[i], line5[i+1]});
                    }
                    FileData data = new FileData(updates_per_file, counts_per_file, count_values, first_and_last_updates, first_and_last_counts);
                    file_data.put(line[0], data);
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("SidethreadData//FileData is missing");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public static FileData getFileData(String live_thread) {
        return file_data.get(live_thread);
    }

    public static void updateFileData(String live_thread, FileData update) {
        String comment = """
                //This file is parsed line by line to gather information about the count/update files without having to check the files.
                //Each thread has four lines.
                //The format for this is as follows:
                //    live_thread total_updates total_counts_time_sort last_count_time_id_for_num_sort last_count (can't be empty. 0 represents nonexistence)
                //    [msgs_per_update_file] (can be empty)
                //    [msgs_per_count_time_file] (can be empty)
                //    [first and last uuid of each update file] (must be exactly two per file. Zero length files are not permitted)
                //    [first and last uuid of each count_time file] (must be exactly two per file. Zero length files are not permitted)""";
        file_data.put(live_thread, update);
        File f = new File("SidethreadData//FileData");
        try {
            PrintWriter fout = new PrintWriter(f);
            fout.println(comment);
            for(String key: file_data.keySet()) {
                fout.print(formatFileData(key));
            }
            fout.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public static String formatFileData(String id) {
        FileData data = file_data.get(id);
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(" ");
        for(int x:data.count_values())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.updates_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(int x:data.counts_per_file())
            sb.append(x).append(" ");
        sb.append("\n");
        for(String[] x:data.first_and_last_updates()) {
            sb.append(x[0]).append(" ");
            sb.append(x[1]).append(" ");
        }
        sb.append("\n");
        for(String[] x:data.first_and_last_counts()) {
            sb.append(x[0]).append(" ");
            sb.append(x[1]).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
