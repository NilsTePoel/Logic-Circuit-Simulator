package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

public class NotGate extends SingleInputGate {
    public NotGate(LogicCircuit circuit) {
        super(circuit);
    }

    protected NotGate(NotGate oldGate, Point newPos) {
        super(oldGate, newPos);
    }

    @Override
    public Gate addInput(Point pos) {
        return new NotGate(this, pos);
    }

    public GateType getType() {
        return GateType.NOT;
    }

    public boolean getOutput() {
        if (getInputs().isEmpty()) return true;
        return !circuit.getGateAt(getInputs().get(0)).getOutput();
    }

    public String getBooleanFunction() {
        if (getInputs().isEmpty()) return "";
        Gate input = circuit.getGateAt(getInputs().get(0));
        return "!" + (input.getType() != GateType.INPUT ? "(" + input.getBooleanFunction() + ")" : input.getBooleanFunction());
    }
}
