package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

public class AndGate extends MultipleInputGate {
    public AndGate(LogicCircuit circuit) {
        super(circuit, "^");
    }

    protected AndGate(AndGate oldGate, Point newPos) {
        super(oldGate, newPos);
    }

    public Gate addInput(Point pos) {
        return new AndGate(this, pos);
    }

    public GateType getType() {
        return GateType.AND;
    }

    public boolean getOutput() {
        if (getInputs().isEmpty()) return false;
        return getInputs().stream().map(circuit::getGateAt).allMatch(Gate::getOutput);
    }
}
