package Threads;

import java.util.Comparator;
import java.util.List;

public class GenericStatOutput {
    private final Comparator<StatOutput> comparator;
    public final String[] table_headers;
    private final String[] printOrder;
    public final List<StatOutput> dataList;

    public GenericStatOutput(Comparator<StatOutput> comparator, String[] table_headers, String[] printOrder, List<StatOutput> dataList) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null.");
        }
        this.comparator = comparator;
        this.table_headers = table_headers;
        this.printOrder = printOrder;
        this.dataList = dataList;
    }

    public void addData(StatOutput data) {
        dataList.add(data);
    }

    public void sortData() {
        dataList.sort(comparator);
    }
    public void sortDataReversed() {
        dataList.sort(comparator.reversed());
    }

    public List<StatOutput> getDataList() {
        return dataList;
    }
    public String formatData(StatOutput data) {
        StringBuilder sb = new StringBuilder();
        for (String field : printOrder) {
            Object value = data.getFieldValue(field);
            if (value != null) {
                sb.append(value).append("|");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1); // Remove trailing "|"
        return sb.toString();
    }
}
