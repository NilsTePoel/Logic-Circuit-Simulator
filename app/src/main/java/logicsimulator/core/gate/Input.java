package logicsimulator.core.gate;

import logicsimulator.core.Point;

import java.util.List;
import java.util.Optional;

public class Input implements Gate {
    private final boolean output;
    private final String inputName;

    private Input(String inputName, boolean output) {
        this.inputName = inputName;
        this.output = output;
    }

    public Input(String inputName) {
        this(inputName, false);
    }

    public GateType getType() {
        return GateType.INPUT;
    }

    public boolean getOutput() {
        return output;
    }

    public Gate toggleOutput() {
        return new Input(inputName, !output);
    }

    public List<Point> getInputs() {
        return List.of();
    }

    public Optional<String> getName() {
        return Optional.of(inputName);
    }

    public String getBooleanFunction() {
        return inputName;
    }

    @Override
    public String toString() {
        return String.format("%s [Output: %b]", getBooleanFunction(), getOutput());
    }
}
