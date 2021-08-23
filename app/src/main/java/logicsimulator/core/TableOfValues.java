package logicsimulator.core;

import java.util.List;
import java.util.stream.Collectors;

public record TableOfValues(List<List<Boolean>> values, List<String> names) {
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        String header = String.join(" | ", names);
        s.append(header).append("\n");

        int rowLength = names.stream().mapToInt(String::length).sum() + 3 * (names.size() - 1) ;
        s.append("-".repeat(rowLength)).append("\n");

        for (List<Boolean> valueRow : values) {
            String row = valueRow.stream().map(value -> value ? "1" : "0").collect(Collectors.joining(" | "));
            s.append(row).append("\n");
        }

        return s.toString();
    }
}
