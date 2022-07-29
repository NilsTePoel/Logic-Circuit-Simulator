package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

public class OrGate extends MultipleInputGate {
    public OrGate(LogicCircuit circuit) {
        super(circuit, "v");
    }

    protected OrGate(OrGate oldGate, Point newPos) {
        super(oldGate, newPos);
    }

    @Override
    public Gate addInput(Point pos) {
        return new OrGate(this, pos);
    }

    public GateType getType() {
        return GateType.OR;
    }

    public boolean getOutput() {
        return getInputs().stream().map(circuit::getGateAt).anyMatch(Gate::getOutput);
    }
}
