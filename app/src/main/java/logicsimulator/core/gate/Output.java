package logicsimulator.core.gate;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;

import java.util.Optional;

public class Output extends SingleInputGate {
    private final String outputName;

    public Output(LogicCircuit circuit, String outputName) {
        super(circuit);
        this.outputName = outputName;
    }

    protected Output(Output oldGate, Point newPos) {
        super(oldGate, newPos);
        outputName = oldGate.outputName;
    }

    @Override
    public Gate addInput(Point pos) {
        return new Output(this, pos);
    }

    public GateType getType() {
        return GateType.OUTPUT;
    }

    public boolean getOutput() {
        if (getInputs().isEmpty()) return false;
        return circuit.getGateAt(getInputs().get(0)).getOutput();
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(outputName);
    }

    public String getBooleanFunction() {
        String s = outputName + " = ";
        if (!getInputs().isEmpty()) s += circuit.getGateAt(getInputs().get(0)).getBooleanFunction();
        return s;
    }
}
