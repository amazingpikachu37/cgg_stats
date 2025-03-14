package Threads;

import java.util.Comparator;

public class StatComparators {
    public static final Comparator<StatOutput> streak_comparator = Comparator.comparing((StatOutput obj) -> obj.streak).reversed()
            .thenComparing((StatOutput obj) -> obj.start)
            .thenComparing((StatOutput obj) -> obj.user1);
}
