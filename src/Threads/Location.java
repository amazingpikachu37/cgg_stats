package Threads;

public class Location {
    private final int counts_by_file;
    private final int chat_file;
    private final int loc_in_file;
    public Location(int cbf, int cf, int lif) {
        this.counts_by_file = cbf;
        this.chat_file = cf;
        this.loc_in_file = lif;
    }

    public int getChatFile() {
        return chat_file;
    }
    public int getCountsByFile() {
        return counts_by_file;
    }
    public int getLocInFile() {
        return loc_in_file;
    }
    @Override
    public String toString() {
        return counts_by_file+" "+chat_file+" "+loc_in_file;
    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Location p)) return false;
        return p.counts_by_file == this.counts_by_file && p.chat_file == this.chat_file && p.loc_in_file == this.loc_in_file;
    }
}
