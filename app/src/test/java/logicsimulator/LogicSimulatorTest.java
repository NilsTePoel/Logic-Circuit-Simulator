package logicsimulator;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.LogicCircuitSimulation;
import logicsimulator.core.Point;
import logicsimulator.core.TableOfValues;
import logicsimulator.core.gate.GateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class LogicSimulatorTest {
    private LogicCircuit c;

    @BeforeEach
    public void setupCircuit() {
        c = new LogicCircuitSimulation();
    }

    // Testen, ob die Schaltung anfangs leer ist
    @Test public void testEmptyCircuit() {
        assertTrue(c.getGatePositions().isEmpty(), "List of gate positions should be empty");
    }

    // Hinzufügen von Gattern zur Schaltung testen
    @Test public void testGateAddition() {
        c.addGate(new Point(1, 1));
        assertEquals(Set.of(new Point(1, 1)), c.getGatePositions(), "Gate was not added");
        assertEquals(GateType.AND, c.getGateAt(new Point(1, 1)).getType(), "Gate has a wrong type");
        c.addGate(new Point(1, 2));
        assertEquals(Set.of(new Point(1, 1), new Point(1, 2)), c.getGatePositions(), "Gate was not added");
        assertEquals(GateType.AND, c.getGateAt(new Point(1, 2)).getType(), "Gate has a wrong type");
    }

    // Hinzufügen von mehreren Gattern gleichzeitig testen
    @Test public void testAddingMultipleGates() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        assertEquals(Set.of(new Point(1, 1), new Point(1, 2)), c.getGatePositions(), "Gates were not added");
    }

    // An einer Position dürfen nicht mehrere Gatter hinzugefügt werden
    // (ist das Hinzufügen nicht möglich, soll die addGate()-Methode "false" zurückgeben)
    @Test public void testInvalidGateAddition() {
        assertTrue(c.addGate(new Point(0, 0)), "Gate was not added");
        assertFalse(c.addGate(new Point(0, 0)), "Gate was added two times");
    }

    // Wird addGate() mit einer ungültigen Position aufgerufen, soll eine Exception ausgelöst werden
    // (eine Position ist hier ungültig, wenn sie "null" ist)
    @Test public void testGateAdditionWithInvalidPosition() {
        assertThrows(IllegalArgumentException.class, () -> c.addGate(null), "Invalid position did not cause an exception");
    }

    // Entfernen von Gattern testen
    @Test public void testGateRemoval() {
        c.addGate(new Point(1, 1));
        assertEquals(Set.of(new Point(1, 1)), c.getGatePositions(), "Gate was not added");
        c.toggleSelection(new Point(1, 1)).removeSelectedGate();
        assertTrue(c.getGatePositions().isEmpty(), "Gate was not removed");
    }

    // Auswahl von Gattern testen (toggleSelection() schaltet die Auswahl um)
    @Test public void testGateSelection() {
        c.addGate(new Point(0, 0));
        c.toggleSelection(new Point(0, 0));
        assertTrue(c.isSelected(new Point(0, 0)), "Gate is not selected");
        c.toggleSelection(new Point(0, 0));
        assertFalse(c.isSelected(new Point(0, 0)), "Gate is selected");
    }

    // Wird toggleSelection() mit einer ungültigen Position aufgerufen, soll eine Exception ausgelöst werden
    // (eine Position ist hier ungültig, wenn sie "null" ist oder sich an dieser Position kein Gatter befindet)
    @Test public void testGateSelectionWithInvalidPosition() {
        assertThrows(IllegalArgumentException.class, () -> c.toggleSelection(new Point(0, 0)), "Invalid position did not cause an exception");
    }

    // Verbinden von Gattern testen (hier mit Und-Gattern)
    @Test public void testGateConnection1() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals(List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs(), "Connection was not added");
    }

    // Verbinden von Gattern testen (hier mit einem Nicht-Gatter)
    // (da dieses nur einen einzigen Eingang haben darf, wird der Eingang bei erneuten Verbindungen überschrieben)
    @Test public void testGateConnection2() {
        c.addGate(new Point(1, 1));
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals(List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs(), "Connection was not added");
        c.addGate(new Point(1, 3));
        c.toggleSelection(new Point(1, 3)).interactWith(new Point(1, 2));
        assertEquals(List.of(new Point(1, 3)), c.getGateAt(new Point(1, 2)).getInputs(), "Connection was not overwritten");
    }

    // Wird ein Gatter entfernt, sollen auch alle Verbindungen zu diesem Gatter entfernt werden (hier mit Und-Gattern)
    @Test public void testGateConnectionRemoval1() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals(List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs(), "Connection was not added");
        c.removeSelectedGate();
        assertTrue(c.getGateAt(new Point(1, 2)).getInputs().isEmpty(), "Connection was not removed");
    }

    // Wird ein Gatter entfernt, sollen auch alle Verbindungen zu diesem Gatter entfernt werden (hier mit Nicht-Gattern)
    @Test public void testGateConnectionRemoval2() {
        c.setSelectedType(GateType.NOT).addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals(List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs(), "Connection was not added");
        c.removeSelectedGate();
        assertTrue(c.getGateAt(new Point(1, 2)).getInputs().isEmpty(), "Connection was not removed");
    }

    // Ein Gatter darf nicht mit sich selbst verbunden werden
    // (sonst funktioniert die Darstellung als boolesche Funktion nicht; interactWith() gibt false zurück)
    @Test public void testInvalidGateConnection1() {
        c.addGate(new Point(0, 0));
        assertFalse(c.toggleSelection(new Point(0, 0)).interactWith(new Point(0, 1)), "Invalid connection was added");
    }

    // Ein Gatter darf nicht mit sich selbst verbunden werden (auch nicht über andere Gatter)
    @Test public void testInvalidGateConnection2() {
        c.addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0));
        assertTrue(c.interactWith(new Point(0, 1)), "Connection could not be added");
        c.toggleSelection(new Point(0, 1));
        assertFalse(c.interactWith(new Point(0, 0)), "Invalid connection was added");
    }

    // Standardmäßig sollte die Gatter-Art "Und" ausgewählt sein
    @Test public void testDefaultGateType() {
        assertEquals(GateType.AND, c.getSelectedType(), "Default gate type should be AND");
    }

    // Ändern der Gatter-Art testen
    @Test public void testGateTypeSelection() {
        c.setSelectedType(GateType.INPUT);
        assertEquals(GateType.INPUT, c.getSelectedType(), "Wrong gate type");
    }

    // Eingangs-Gatter testen (Ausgangswert lässt sich mit interactWith() ändern)
    @Test public void testInputGate() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        assertFalse(c.getGateAt(new Point(0, 0)).getOutput(), "Default output is false");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(0, 0)).getOutput(), "Output was not changed");
    }

    // Namen der Eingangs-Gatter testen
    // (a, b, ..., z, a1, b1, ...)
    @Test public void testInputNameGeneration() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        assertEquals("a", c.getGateAt(new Point(0, 0)).getName().orElseThrow(), "Invalid name");
        c.addGate(new Point(1, 0));
        assertEquals("b", c.getGateAt(new Point(1, 0)).getName().orElseThrow(), "Invalid name");
        IntStream.rangeClosed(2, 26).forEach(n -> c.addGate(new Point(n, 0)));
        assertEquals("a1", c.getGateAt(new Point(26, 0)).getName().orElseThrow(), "Invalid name");
    }

    // Namen der Ausgangs-Gatter testen
    // (f1, f2, f3, ...)
    @Test public void testOutputNameGeneration() {
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(0, 0));
        assertEquals("f1", c.getGateAt(new Point(0, 0)).getName().orElseThrow(), "Invalid name");
        IntStream.rangeClosed(1, 9).forEach(n -> c.addGate(new Point(n, 0)));
        assertEquals("f10", c.getGateAt(new Point(9, 0)).getName().orElseThrow(), "Invalid name");
    }

    // Alle anderen Gatter sollten keine Namen haben
    @Test public void testNamesOfOtherGates() {
        c.addGate(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(0, 0)).getName().isEmpty(), "And gate should not have a name");
        c.setSelectedType(GateType.OR).addGate(new Point(1, 0));
        assertTrue(c.getGateAt(new Point(1, 0)).getName().isEmpty(), "Or gate should not have a name");
        c.setSelectedType(GateType.NOT).addGate(new Point(2, 0));
        assertTrue(c.getGateAt(new Point(2, 0)).getName().isEmpty(), "Not gate should not have a name");
    }

    // Ausgangs-Gatter testen (besitzt den gleichen Ausgangswert wie sein Eingang)
    @Test public void testOutputGate() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals("f1 = a [Output: true] [Input: [[0, 0]]]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("f1 = a", c.getBooleanFunctions().orElseThrow(), "Invalid boolean function");
    }

    // Ein Ausgangs-Gatter ohne Eingang sollte den Wert "false" haben
    // (Nicht festgelegte Eingänge werden bei dieser Simulation als "false" interpretiert)
    @Test public void testOutputGateWithoutInput() {
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(1, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals("f1 =  [Output: false] [Input: []]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("f1 = ", c.getBooleanFunctions().orElseThrow(), "Invalid boolean function");
    }

    // Und-Gatter testen (Ausgangswert ist nur "true", wenn die Werte aller Eingänge "true" sind)
    @Test public void testAndGate() {
        c.addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertFalse(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 1));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        assertEquals("a ^ b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString(), "Invalid boolean function, input or output");
        assertEquals("a ^ b", c.getGateAt(new Point(1, 1)).getBooleanFunction(), "Invalid boolean function");
    }

    // Ein Und-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testAndGateWithoutInputs() {
        c.addGate(new Point(1, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals(" [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("", c.getGateAt(new Point(1, 0)).getBooleanFunction(), "Invalid boolean function");
    }

    // Oder-Gatter testen (Ausgangswert ist nur "true", wenn mindestens ein Eingang den Wert "true" hat)
    @Test public void testOrGate() {
        c.setSelectedType(GateType.OR).addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 1));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        assertEquals("a v b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString(), "Invalid boolean function, input or output");
        assertEquals("a v b", c.getGateAt(new Point(1, 1)).getBooleanFunction(), "Invalid boolean function");
    }

    // Ein Oder-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testOrGateWithoutInputs() {
        c.setSelectedType(GateType.OR).addGate(new Point(1, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals(" [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("", c.getGateAt(new Point(1, 0)).getBooleanFunction(), "Invalid boolean function");
    }

    // Exklusiv-Oder-Gatter testen (Ausgangswert ist nur "true", wenn genau ein Eingang den Wert "true" hat)
    @Test public void testExclusiveOrGate() {
        c.setSelectedType(GateType.EXCLUSIVE_OR).addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 1));
        assertFalse(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(1, 1)).getOutput(), "Wrong output");
        assertEquals("a XOR b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString(), "Invalid boolean function, input or output");
        assertEquals("a XOR b", c.getGateAt(new Point(1, 1)).getBooleanFunction(), "Invalid boolean function");
    }

    // Ein Exklusiv-Oder-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testExclusiveOrGateWithoutInputs() {
        c.setSelectedType(GateType.EXCLUSIVE_OR).addGate(new Point(1, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals(" [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("", c.getGateAt(new Point(1, 0)).getBooleanFunction(), "Invalid boolean function");
    }

    // Nicht-Gatter testen (Dreht den Wert seines Eingangs um: true → false, false → true)
    @Test public void testNotGate() {
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 0));
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        assertTrue(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertFalse(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals("!a [Output: false] [Input: [[0, 0]]]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("!a", c.getGateAt(new Point(1, 0)).getBooleanFunction(), "Invalid boolean function");
    }

    // Ein Nicht-Gatter ohne Eingang sollte den Ausgangswert "true" haben
    @Test public void testNotGateWithoutInputs() {
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 0));
        assertTrue(c.getGateAt(new Point(1, 0)).getOutput(), "Wrong output");
        assertEquals(" [Output: true] [Input: []]", c.getGateAt(new Point(1, 0)).toString(), "Invalid boolean function, input or output");
        assertEquals("", c.getGateAt(new Point(1, 0)).getBooleanFunction(), "Invalid boolean function");
    }

    // Aufbau eines Exklusiv-Oder-Gatters aus Grundgattern testen (Ausgangswert ist nur "true", wenn genau ein Eingang den Wert "true" hat)
    @Test public void testExclusiveOr() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.setSelectedType(GateType.NOT).addGates(new Point(1, 0), new Point(1, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        c.setSelectedType(GateType.AND).addGates(new Point(2, 0), new Point(2, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(2, 1));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 1));
        c.setSelectedType(GateType.OR).addGate(new Point(3, 0));
        c.toggleSelection(new Point(2, 0)).interactWith(new Point(3, 0));
        c.toggleSelection(new Point(2, 1)).interactWith(new Point(3, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(4, 0));
        c.toggleSelection(new Point(3, 0)).interactWith(new Point(4, 0));
        assertFalse(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 1));
        assertFalse(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        assertEquals("f1 = (a ^ (!b)) v (b ^ (!a))", c.getBooleanFunctions().orElseThrow(), "Invalid boolean function");
    }

    // Aufbau eines Halbaddierers aus Grundgattern testen
    // (Addiert zwei einstellige Binärzahlen; f1: Summe; f2: Übertrag)
    @Test public void testHalfAdder() {
        c.setSelectedType(GateType.INPUT);
        c.addGates(new Point(0, 0), new Point(0, 1));

        // Summe
        c.setSelectedType(GateType.NOT).addGates(new Point(1, 0), new Point(1, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        c.setSelectedType(GateType.AND).addGates(new Point(2, 0), new Point(2, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(2, 1));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 1));
        c.setSelectedType(GateType.OR).addGate(new Point(3, 0));
        c.toggleSelection(new Point(2, 0)).interactWith(new Point(3, 0));
        c.toggleSelection(new Point(2, 1)).interactWith(new Point(3, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(4, 0));
        c.toggleSelection(new Point(3, 0)).interactWith(new Point(4, 0));

        // Übertrag
        c.setSelectedType(GateType.AND).addGate(new Point(1, 2));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 2));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 2));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(2, 2));
        c.toggleSelection(new Point(1, 2)).interactWith(new Point(2, 2));

        assertFalse(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        assertFalse(c.getGateAt(new Point(2, 2)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        assertFalse(c.getGateAt(new Point(2, 2)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 1));
        assertFalse(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        assertTrue(c.getGateAt(new Point(2, 2)).getOutput(), "Wrong output");
        c.interactWith(new Point(0, 0));
        assertTrue(c.getGateAt(new Point(4, 0)).getOutput(), "Wrong output");
        assertFalse(c.getGateAt(new Point(2, 2)).getOutput(), "Wrong output");

        assertEquals("f1 = (a ^ (!b)) v (b ^ (!a)); f2 = a ^ b", c.getBooleanFunctions().orElseThrow(), "Invalid boolean functions");
    }

    // Wurde noch kein Ausgangs-Gatter hinzugefügt, wird auch noch keine boolesche Funktion zurückgegeben
    @Test public void testEmptyBooleanFunction() {
        assertTrue(c.getBooleanFunctions().isEmpty());
    }

    // Darstellung als boolesche Funktion testen (mehrere Szenarien)
    @Test public void testBooleanFunction1() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1), new Point(0, 2));
        c.setSelectedType(GateType.AND).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 0));
        c.setSelectedType(GateType.OR).addGate(new Point(1, 1));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 2)).interactWith(new Point(1, 1));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(2, 0));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(2, 0));

        assertEquals("f1 = (a ^ b) v c", c.getBooleanFunctions().orElseThrow(), "Wrong boolean function");
    }

    // Darstellung als boolesche Funktion testen (mehrere Szenarien)
    @Test public void testBooleanFunction2() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.setSelectedType(GateType.AND).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 0));
        c.setSelectedType(GateType.NOT).addGate(new Point(2, 0));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(3, 0));
        c.toggleSelection(new Point(2, 0)).interactWith(new Point(3, 0));

        assertEquals("f1 = !(a ^ b)", c.getBooleanFunctions().orElseThrow(), "Wrong boolean function");
    }

    // Darstellung als boolesche Funktion testen (mehrere Szenarien)
    @Test public void testBooleanFunction3() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3));
        c.setSelectedType(GateType.OR).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 0));
        c.setSelectedType(GateType.NOT).addGate(new Point(2, 0));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 0));
        c.setSelectedType(GateType.AND).addGate(new Point(3, 0));
        c.toggleSelection(new Point(2, 0)).interactWith(new Point(3, 0));
        c.toggleSelection(new Point(0, 2)).interactWith(new Point(3, 0));
        c.toggleSelection(new Point(0, 3)).interactWith(new Point(3, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(4, 0));
        c.toggleSelection(new Point(3, 0)).interactWith(new Point(4, 0));

        assertEquals("f1 = (!(a v b)) ^ c ^ d", c.getBooleanFunctions().orElseThrow(), "Wrong boolean function");
    }

    // Gibt es mehrere Ausgangs-Gatter, werden auch mehrere boolesche Funktionen zurückgegeben
    @Test public void testMultipleBooleanFunctions() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1), new Point(0, 2));
        c.setSelectedType(GateType.AND).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 0));
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 1));
        c.toggleSelection(new Point(0, 2)).interactWith(new Point(1, 1));
        c.setSelectedType(GateType.OUTPUT).addGates(new Point(2, 0), new Point(2, 1));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(2, 1));

        assertEquals("f1 = a ^ b; f2 = !c", c.getBooleanFunctions().orElseThrow(), "Wrong boolean functions");
    }

    // Erstellen einer Wertetabelle testen
    @Test public void testTableOfValues() {
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1), new Point(0, 2));
        c.setSelectedType(GateType.AND).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 0));
        c.setSelectedType(GateType.EXCLUSIVE_OR).addGate(new Point(2, 0));
        c.toggleSelection(new Point(1, 0)).interactWith(new Point(2, 0));
        c.toggleSelection(new Point(0, 2)).interactWith(new Point(2, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(3, 0));
        c.toggleSelection(new Point(2, 0)).interactWith(new Point(3, 0));

        List<List<Boolean>> expectedValues = List.of(List.of(false, false, false, false),
                                                     List.of(false, false, true, true),
                                                     List.of(false, true, false, false),
                                                     List.of(false, true, true, true),
                                                     List.of(true, false, false, false),
                                                     List.of(true, false, true, true),
                                                     List.of(true, true, false, true),
                                                     List.of(true, true, true, false));
        TableOfValues expectedTable = new TableOfValues(expectedValues, List.of("a", "b", "c", "(a ^ b) XOR c"));
        TableOfValues actualTable = c.getTableOfValues().orElseThrow();
        assertEquals(expectedTable, actualTable, "Wrong table of values");

        String expectedDisjunctiveNormalForm = "(!a ^ !b ^ c) v (!a ^ b ^ c) v (a ^ !b ^ c) v (a ^ b ^ !c)";
        assertEquals(expectedDisjunctiveNormalForm, actualTable.getDisjunctiveNormalForm(), "Wrong disjunctive normal form");
    }
}
