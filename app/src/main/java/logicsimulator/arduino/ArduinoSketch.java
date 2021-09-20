package logicsimulator.arduino;

import logicsimulator.core.TableOfValues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ArduinoSketch(TableOfValues table, Path path) {
    private String getMintermForSketch(List<Boolean> valueRow) {
        return IntStream.range(0, valueRow.size() - 1)
                .mapToObj(i -> valueRow.get(i) ? "inputStates[%d]".formatted(i) : "!inputStates[%d]".formatted(i))
                .collect(Collectors.joining(" && "));
    }

    private String getDisjunctiveNormalFormForSketch() {
        // Ist der Funktionswert immer "falsch", soll "false" zurückgegeben werden, damit der Code gültig bleibt
        if (table.values().stream().noneMatch(valueRow -> valueRow.get(valueRow.size() - 1))) return "false";

        return table.values().stream().filter(valueRow -> valueRow.get(valueRow.size() - 1))
                .map(this::getMintermForSketch)
                .map(minterm -> "(" + minterm + ")")
                .collect(Collectors.joining(" || "));
    }

    public void saveToDisk() throws IOException {
        String template = Files.readString(Path.of("src/main/resources/arduino_sketch_template.txt"));
        String sourceCode = template.replace("DISJUNCTIVE_NORMAL_FORM", getDisjunctiveNormalFormForSketch());
        Files.writeString(path, sourceCode);
    }
}
