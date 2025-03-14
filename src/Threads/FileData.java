package Threads;

import java.util.ArrayList;
import java.util.List;

public record FileData(ArrayList<Integer> updates_per_file, ArrayList<Integer> counts_per_file,
                       List<Integer> count_values, List<String[]> first_and_last_updates, List<String[]> first_and_last_counts) {
}
