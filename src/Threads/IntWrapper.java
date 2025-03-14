package Threads;

public class IntWrapper {
    private int value;
    public IntWrapper(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public void plus(int plus) {
        value += plus;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
