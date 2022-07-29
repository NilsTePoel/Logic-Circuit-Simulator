package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultipleInputGate implements Gate {
    private final List<Point> inputPositions = new ArrayList<>();
    protected final LogicCircuit circuit;
    private final String symbol;

    protected MultipleInputGate(LogicCircuit circuit, String symbol) {
        this.circuit = circuit;
        this.symbol = symbol;
    }

    protected MultipleInputGate(MultipleInputGate oldGate, Point newPos) {
        this(oldGate.circuit, oldGate.symbol);
        inputPositions.addAll(oldGate.inputPositions);
        inputPositions.add(newPos);
    }

    public List<Point> getInputs() {
        inputPositions.removeIf(pos -> circuit.getGateAt(pos) == null); // Ungültige Eingänge entfernen
        return List.copyOf(inputPositions);
    }

    public String getBooleanFunction() {
        return inputPositions.stream().map(circuit::getGateAt)
                .map(gate -> gate.getType() != GateType.INPUT ? "(" + gate.getBooleanFunction() + ")" : gate.getBooleanFunction())
                .collect(Collectors.joining(" " + symbol + " "));
    }

    @Override
    public String toString() {
        return "%s [Output: %b] [Inputs: %s]".formatted(getBooleanFunction(), getOutput(), getInputs());
    }
}
