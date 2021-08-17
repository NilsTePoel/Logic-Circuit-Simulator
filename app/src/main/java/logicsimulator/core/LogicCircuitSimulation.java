package logicsimulator.core;

import logicsimulator.core.gate.*;

import java.util.*;
import java.util.stream.Collectors;

public class LogicCircuitSimulation implements LogicCircuit {
    private final Map<Point, Gate> gates = new HashMap<>();
    private GateType selectedType = GateType.AND;
    private Point selectedPos;

    private int inputCounter = 0, outputCounter = 1;
    private char inputChar = 'a';

    public boolean addGate(Point pos) {
        if (pos == null) throw new IllegalArgumentException("position must not be null");
        if (gates.containsKey(pos)) return false;

        Gate gate = switch (selectedType) {
            case AND -> new AndGate(this);
            case OR -> new OrGate(this);
            case EXCLUSIVE_OR -> new ExclusiveOrGate(this);
            case NOT -> new NotGate(this);
            case INPUT -> new Input(generateInputName());
            case OUTPUT -> new Output(this, generateOutputName());
        };

        gates.put(pos, gate);
        return true;
    }

    private String generateInputName() {
        String inputName = String.valueOf(inputChar);
        if (inputCounter > 0) inputName += inputCounter;
        inputChar++;
        if (inputChar > 'z') {
            inputChar = 'a';
            inputCounter++;
        }
        return inputName;
    }

    private String generateOutputName() {
        String outputName = "f" + outputCounter;
        outputCounter++;
        return outputName;
    }

    public Gate getGateAt(Point pos) {
        if (pos == null) throw new IllegalArgumentException("position must not be null");
        return gates.get(pos);
    }

    public Set<Point> getGatePositions() {
        return Set.copyOf(gates.keySet());
    }

    public LogicCircuit setSelectedType(GateType type) {
        selectedType = type;
        return this;
    }

    public GateType getSelectedType() {
        return selectedType;
    }

    public LogicCircuit toggleSelection(Point pos) {
        if (pos == null || getGateAt(pos) == null) throw new IllegalArgumentException("invalid position");
        if (selectedPos != null && selectedPos.equals(pos)) {
            selectedPos = null;
        } else {
            selectedPos = pos;
        }
        return this;
    }

    public boolean isSelected(Point pos) {
        if (pos == null) throw new IllegalArgumentException("position must not be null");
        if (selectedPos == null) return false;
        return selectedPos.equals(pos);
    }

    // Eine Verbindung ist ungültig, wenn man ein Ausgangs-Gatter als Eingang verwenden oder ein Gatter mit sich
    // selbst verbinden möchte (da dann die Darstellung als boolesche Funktion nicht funktioniert)
    private boolean isValidConnection(Point pos) {
        if (selectedPos == null || getGateAt(selectedPos).getType() == GateType.OUTPUT) return false;
        return !hasLoop(selectedPos, pos);
    }

    private boolean hasLoop(Point pos, Point connectionPos) {
        Gate gate = getGateAt(pos);
        if (pos.equals(connectionPos)) return true;
        else if (gate.getInputs().isEmpty()) return false;
        else return gate.getInputs().parallelStream().anyMatch(p -> hasLoop(p, connectionPos));
    }

    // Eingangs-Gatter: Ausgangswert ändern
    // Alle anderen Gatter: mit ausgewähltem Gatter verbinden
    public boolean interactWith(Point pos) {
        if (pos == null) throw new IllegalArgumentException("position must not be null");
        Gate gate = getGateAt(pos);
        if (gate != null) {
            if (gate.getType() == GateType.INPUT) {
                gates.replace(pos, gate.toggleOutput());
                return true;
            } else if (isValidConnection(pos)) {
                gates.replace(pos, gate.addInput(selectedPos));
                return true;
            }
        }
        return false;
    }

    public void removeSelectedGate() {
        gates.remove(selectedPos);
        selectedPos = null;
    }
    
    public Optional<String> getBooleanFunctions() {
        if (gates.values().parallelStream().noneMatch(gate -> gate.getType() == GateType.OUTPUT)) return Optional.empty();

        String booleanFunctions = gates.values().parallelStream().filter(gate -> gate.getType() == GateType.OUTPUT)
                .sorted(Comparator.comparing(gate -> gate.getName().orElseThrow()))
                .map(Gate::getBooleanFunction).collect(Collectors.joining("; "));
        return Optional.of(booleanFunctions);
    }

    @Override
    public String toString() {
        return gates.toString();
    }
}
