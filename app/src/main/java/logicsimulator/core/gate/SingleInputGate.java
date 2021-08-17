package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

import java.util.List;

public abstract class SingleInputGate implements Gate {
    private Point input;
    protected LogicCircuit circuit;

    public SingleInputGate(LogicCircuit circuit) {
        this.circuit = circuit;
    }

    public SingleInputGate(SingleInputGate oldGate, Point newPos) {
        this(oldGate.circuit);
        this.input = newPos;
    }

    public List<Point> getInputs() {
        if (input != null && circuit.getGateAt(input) == null) input = null; // Ungültigen Eingang entfernen
        return input == null ? List.of() : List.of(input);
    }

    @Override
    public String toString() {
        return String.format("%s [Output: %b] [Input: %s]", getBooleanFunction(), getOutput(), getInputs());
    }
}
