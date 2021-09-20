package logicsimulator.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record TableOfValues(List<List<Boolean>> values, List<String> names) {
    public String getTable() {
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

    private String getMinterm(List<Boolean> valueRow) {
        return IntStream.range(0, valueRow.size() - 1)
                .mapToObj(i -> valueRow.get(i) ? names.get(i) : "!" + names.get(i))
                .collect(Collectors.joining(" ^ "));
    }

    public String getDisjunctiveNormalForm() {
        // Ist der Funktionswert immer "falsch", soll "0" zurückgegeben werden, damit die Ausgabe gültig bleibt
        if (values.stream().noneMatch(valueRow -> valueRow.get(valueRow.size() - 1))) return "0";

        return values.stream().filter(valueRow -> valueRow.get(valueRow.size() - 1))
                .map(this::getMinterm)
                .map(minterm -> "(" + minterm + ")")
                .collect(Collectors.joining(" v "));
    }

    @Override
    public String toString() {
        return "%s\nDNF: %s".formatted(getTable(), getDisjunctiveNormalForm());
    }
}
