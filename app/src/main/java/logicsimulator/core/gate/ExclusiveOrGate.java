package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

public class ExclusiveOrGate extends MultipleInputGate {
    public ExclusiveOrGate(LogicCircuit circuit) {
        super(circuit, "XOR");
    }

    protected ExclusiveOrGate(ExclusiveOrGate oldGate, Point newPos) {
        super(oldGate, newPos);
    }

    @Override
    public Gate addInput(Point pos) {
        return new ExclusiveOrGate(this, pos);
    }

    public GateType getType() {
        return GateType.EXCLUSIVE_OR;
    }

    public boolean getOutput() {
        return getInputs().stream().map(circuit::getGateAt).filter(Gate::getOutput).count() == 1;
    }
}