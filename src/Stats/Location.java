package Stats;

import javax.annotation.Nonnull;

public record Location(int counts_by_file, int chat_file, int loc_in_file) {

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
    @Nonnull
    public String toString() {
        return counts_by_file + " " + chat_file + " " + loc_in_file;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location p)) return false;
        return p.counts_by_file == this.counts_by_file && p.chat_file == this.chat_file && p.loc_in_file == this.loc_in_file;
    }
}
