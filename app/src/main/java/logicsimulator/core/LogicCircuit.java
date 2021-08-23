package logicsimulator.core;

import logicsimulator.core.gate.Gate;
import logicsimulator.core.gate.GateType;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public interface LogicCircuit {
    // Der Rückgabewert ist "false", wenn sich an der Position "pos" schon ein Gatter befindet.
    boolean addGate(Point pos);
    default void addGates(Point... positions) { Arrays.stream(positions).forEach(this::addGate); }
    Gate getGateAt(Point pos);
    Set<Point> getGatePositions();

    LogicCircuit setSelectedType(GateType type);
    GateType getSelectedType();

    LogicCircuit toggleSelection(Point pos);
    boolean isSelected(Point pos);
    // Der Rückgabewert ist "false", wenn zwei Gatter nicht verbunden werden konnten.
    boolean interactWith(Point pos);
    void removeSelectedGate();

    Optional<String> getBooleanFunctions();
    Optional<TableOfValues> getTableOfValues();
}
