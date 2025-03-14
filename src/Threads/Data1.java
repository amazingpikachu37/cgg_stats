package Threads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data1 {
    private UpdateValue[] counts;
    private boolean loadedOldest;

    public boolean isLoadedOldest() {
        return this.loadedOldest;
    }

    public void setLoadedOldest(boolean loadedOldest) {
        this.loadedOldest = loadedOldest;
    }

    public void setCounts(UpdateValue[] data) {
        this.counts = data;
    }
    public UpdateValue[] getCounts() {
        return this.counts;
    }
}
