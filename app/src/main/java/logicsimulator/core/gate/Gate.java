package logicsimulator.core.gate;

import logicsimulator.core.Point;

import java.util.List;
import java.util.Optional;

public interface Gate {
    GateType getType();
    default Gate toggleOutput() { throw new UnsupportedOperationException(); }
    boolean getOutput();
    default Gate addInput(Point pos) { throw new UnsupportedOperationException(); }
    List<Point> getInputs();
    default Optional<String> getName() { return Optional.empty(); }
    String getBooleanFunction();
}
