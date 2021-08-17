package logicsimulator;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.LogicCircuitSimulation;
import logicsimulator.core.Point;
import logicsimulator.core.gate.GateType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class LogicSimulatorTest {
    private LogicCircuit c;

    @Before
    public void setupCircuit() {
        c = new LogicCircuitSimulation();
    }

    // Testen, ob die Schaltung anfangs leer ist
    @Test public void testEmptyCircuit() {
        assertTrue("List of gate positions should be empty", c.getGatePositions().isEmpty());
    }

    // Hinzufügen von Gattern zur Schaltung testen
    @Test public void testGateAddition() {
        c.addGate(new Point(1, 1));
        assertEquals("Gate was not added", Set.of(new Point(1, 1)), c.getGatePositions());
        assertEquals("Gate has a wrong type", GateType.AND, c.getGateAt(new Point(1, 1)).getType());
        c.addGate(new Point(1, 2));
        assertEquals("Gate was not added", Set.of(new Point(1, 1), new Point(1, 2)), c.getGatePositions());
        assertEquals("Gate has a wrong type", GateType.AND, c.getGateAt(new Point(1, 2)).getType());
    }

    // Hinzufügen von mehreren Gattern gleichzeitig testen
    @Test public void testAddingMultipleGates() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        assertEquals("Gates were not added", Set.of(new Point(1, 1), new Point(1, 2)), c.getGatePositions());
    }

    // An einer Position dürfen nicht mehrere Gatter hinzugefügt werden
    // (ist das Hinzufügen nicht möglich, soll die addGate()-Methode "false" zurückgeben)
    @Test public void testInvalidGateAddition() {
        assertTrue("Gate was not added", c.addGate(new Point(0, 0)));
        assertFalse("Gate was added two times", c.addGate(new Point(0, 0)));
    }

    // Wird addGate() mit einer ungültigen Position aufgerufen, soll eine Exception ausgelöst werden
    // (eine Position ist hier ungültig, wenn sie "null" ist)
    @Test(expected = IllegalArgumentException.class) public void testGateAdditionWithInvalidPosition() {
        c.addGate(null);
    }

    // Entfernen von Gattern testen
    @Test public void testGateRemoval() {
        c.addGate(new Point(1, 1));
        assertEquals("Gate was not added", Set.of(new Point(1, 1)), c.getGatePositions());
        c.toggleSelection(new Point(1, 1)).removeSelectedGate();
        assertTrue("Gate was not removed", c.getGatePositions().isEmpty());
    }

    // Auswahl von Gattern testen (toggleSelection() schaltet die Auswahl um)
    @Test public void testGateSelection() {
        c.addGate(new Point(0, 0));
        c.toggleSelection(new Point(0, 0));
        assertTrue("Gate is not selected", c.isSelected(new Point(0, 0)));
        c.toggleSelection(new Point(0, 0));
        assertFalse("Gate is selected", c.isSelected(new Point(0, 0)));
    }

    // Wird toggleSelection() mit einer ungültigen Position aufgerufen, soll eine Exception ausgelöst werden
    // (eine Position ist hier ungültig, wenn sie "null" ist oder sich an dieser Position kein Gatter befindet)
    @Test(expected = IllegalArgumentException.class) public void testGateSelectionWithInvalidPosition() {
        c.toggleSelection(new Point(0, 0));
    }

    // Verbinden von Gattern testen (hier mit Und-Gattern)
    @Test public void testGateConnection1() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals("Connection was not added", List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs());
    }

    // Verbinden von Gattern testen (hier mit einem Nicht-Gatter)
    // (da dieses nur einen einzigen Eingang haben darf, wird der Eingang bei erneuten Verbindungen überschrieben)
    @Test public void testGateConnection2() {
        c.addGate(new Point(1, 1));
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals("Connection was not added", List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs());
        c.addGate(new Point(1, 3));
        c.toggleSelection(new Point(1, 3)).interactWith(new Point(1, 2));
        assertEquals("Connection was not overwritten", List.of(new Point(1, 3)), c.getGateAt(new Point(1, 2)).getInputs());
    }

    // Wird ein Gatter entfernt, sollen auch alle Verbindungen zu diesem Gatter entfernt werden (hier mit Und-Gattern)
    @Test public void testGateConnectionRemoval1() {
        c.addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals("Connection was not added", List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs());
        c.removeSelectedGate();
        assertTrue("Connection was not removed", c.getGateAt(new Point(1, 2)).getInputs().isEmpty());
    }

    // Wird ein Gatter entfernt, sollen auch alle Verbindungen zu diesem Gatter entfernt werden (hier mit Nicht-Gattern)
    @Test public void testGateConnectionRemoval2() {
        c.setSelectedType(GateType.NOT).addGates(new Point(1, 1), new Point(1, 2));
        c.toggleSelection(new Point(1, 1)).interactWith(new Point(1, 2));
        assertEquals("Connection was not added", List.of(new Point(1, 1)), c.getGateAt(new Point(1, 2)).getInputs());
        c.removeSelectedGate();
        assertTrue("Connection was not removed", c.getGateAt(new Point(1, 2)).getInputs().isEmpty());
    }

    // Ein Gatter darf nicht mit sich selbst verbunden werden
    // (sonst funktioniert die Darstellung als boolesche Funktion nicht; interactWith() gibt false zurück)
    @Test public void testInvalidGateConnection1() {
        c.addGate(new Point(0, 0));
        assertFalse("Invalid connection was added", c.toggleSelection(new Point(0, 0)).interactWith(new Point(0, 1)));
    }

    // Ein Gatter darf nicht mit sich selbst verbunden werden (auch nicht über andere Gatter)
    @Test public void testInvalidGateConnection2() {
        c.addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0));
        assertTrue("Connection could not be added", c.interactWith(new Point(0, 1)));
        c.toggleSelection(new Point(0, 1));
        assertFalse("Invalid connection was added", c.interactWith(new Point(0, 0)));
    }

    // Standardmäßig sollte die Gatter-Art "Und" ausgewählt sein
    @Test public void testDefaultGateType() {
        assertEquals("Default gate type should be AND", GateType.AND, c.getSelectedType());
    }

    // Ändern der Gatter-Art testen
    @Test public void testGateTypeSelection() {
        c.setSelectedType(GateType.INPUT);
        assertEquals("Wrong gate type", GateType.INPUT, c.getSelectedType());
    }

    // Eingangs-Gatter testen (Ausgangswert lässt sich mit interactWith() ändern)
    @Test public void testInputGate() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        assertFalse("Default output is false", c.getGateAt(new Point(0, 0)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Output was not changed", c.getGateAt(new Point(0, 0)).getOutput());
    }

    // Namen der Eingangs-Gatter testen
    // (a, b, ..., z, a1, b1, ...)
    @Test public void testInputNameGeneration() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        assertEquals("Invalid name", "a", c.getGateAt(new Point(0, 0)).getName().orElseThrow());
        c.addGate(new Point(1, 0));
        assertEquals("Invalid name", "b", c.getGateAt(new Point(1, 0)).getName().orElseThrow());
        IntStream.rangeClosed(2, 26).forEach(n -> c.addGate(new Point(n, 0)));
        assertEquals("Invalid name", "a1", c.getGateAt(new Point(26, 0)).getName().orElseThrow());
    }

    // Namen der Ausgangs-Gatter testen
    // (f1, f2, f3, ...)
    @Test public void testOutputNameGeneration() {
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(0, 0));
        assertEquals("Invalid name", "f1", c.getGateAt(new Point(0, 0)).getName().orElseThrow());
        IntStream.rangeClosed(1, 9).forEach(n -> c.addGate(new Point(n, 0)));
        assertEquals("Invalid name", "f10", c.getGateAt(new Point(9, 0)).getName().orElseThrow());
    }

    // Alle anderen Gatter sollten keine Namen haben
    @Test public void testNamesOfOtherGates() {
        c.addGate(new Point(0, 0));
        assertTrue("And gate should not have a name", c.getGateAt(new Point(0, 0)).getName().isEmpty());
        c.setSelectedType(GateType.OR).addGate(new Point(1, 0));
        assertTrue("Or gate should not have a name", c.getGateAt(new Point(1, 0)).getName().isEmpty());
        c.setSelectedType(GateType.NOT).addGate(new Point(2, 0));
        assertTrue("Not gate should not have a name", c.getGateAt(new Point(2, 0)).getName().isEmpty());
    }

    // Ausgangs-Gatter testen (besitzt den gleichen Ausgangswert wie sein Eingang)
    @Test public void testOutputGate() {
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(1, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", "f1 = a [Output: true] [Input: [[0, 0]]]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "f1 = a", c.getBooleanFunctions().orElseThrow());
    }

    // Ein Ausgangs-Gatter ohne Eingang sollte den Wert "false" haben
    // (Nicht festgelegte Eingänge werden bei dieser Simulation als "false" interpretiert)
    @Test public void testOutputGateWithoutInput() {
        c.setSelectedType(GateType.OUTPUT).addGate(new Point(1, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", "f1 =  [Output: false] [Input: []]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "f1 = ", c.getBooleanFunctions().orElseThrow());
    }

    // Und-Gatter testen (Ausgangswert ist nur "true", wenn die Werte aller Eingänge "true" sind)
    @Test public void testAndGate() {
        c.addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 1));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        assertEquals("Invalid boolean function, input or output", "a ^ b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString());
        assertEquals("Invalid boolean function", "a ^ b", c.getGateAt(new Point(1, 1)).getBooleanFunction());
    }

    // Ein Und-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testAndGateWithoutInputs() {
        c.addGate(new Point(1, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", " [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "", c.getGateAt(new Point(1, 0)).getBooleanFunction());
    }

    // Oder-Gatter testen (Ausgangswert ist nur "true", wenn mindestens ein Eingang den Wert "true" hat)
    @Test public void testOrGate() {
        c.setSelectedType(GateType.OR).addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 1));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        assertEquals("Invalid boolean function, input or output", "a v b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString());
        assertEquals("Invalid boolean function", "a v b", c.getGateAt(new Point(1, 1)).getBooleanFunction());
    }

    // Ein Oder-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testOrGateWithoutInputs() {
        c.setSelectedType(GateType.OR).addGate(new Point(1, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", " [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "", c.getGateAt(new Point(1, 0)).getBooleanFunction());
    }

    // Exklusiv-Oder-Gatter testen (Ausgangswert ist nur "true", wenn genau ein Eingang den Wert "true" hat)
    @Test public void testExclusiveOrGate() {
        c.setSelectedType(GateType.EXCLUSIVE_OR).addGate(new Point(1, 1));
        c.setSelectedType(GateType.INPUT).addGates(new Point(0, 0), new Point(0, 1));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 1));
        c.toggleSelection(new Point(0, 1)).interactWith(new Point(1, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 1)).getOutput());
        assertEquals("Invalid boolean function, input or output", "a XOR b [Output: true] [Inputs: [[0, 0], [0, 1]]]", c.getGateAt(new Point(1, 1)).toString());
        assertEquals("Invalid boolean function", "a XOR b", c.getGateAt(new Point(1, 1)).getBooleanFunction());
    }

    // Ein Exklusiv-Oder-Gatter ohne Eingänge sollte den Ausgangswert "false" haben
    @Test public void testExclusiveOrGateWithoutInputs() {
        c.setSelectedType(GateType.EXCLUSIVE_OR).addGate(new Point(1, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", " [Output: false] [Inputs: []]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "", c.getGateAt(new Point(1, 0)).getBooleanFunction());
    }

    // Nicht-Gatter testen (Dreht den Wert seines Eingangs um: true -> false, false -> true)
    @Test public void testNotGate() {
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 0));
        c.setSelectedType(GateType.INPUT).addGate(new Point(0, 0));
        c.toggleSelection(new Point(0, 0)).interactWith(new Point(1, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        c.interactWith(new Point(0, 0));
        assertFalse("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", "!a [Output: false] [Input: [[0, 0]]]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "!a", c.getGateAt(new Point(1, 0)).getBooleanFunction());
    }

    // Ein Nicht-Gatter ohne Eingang sollte den Ausgangswert "true" haben
    @Test public void testNotGateWithoutInputs() {
        c.setSelectedType(GateType.NOT).addGate(new Point(1, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(1, 0)).getOutput());
        assertEquals("Invalid boolean function, input or output", " [Output: true] [Input: []]", c.getGateAt(new Point(1, 0)).toString());
        assertEquals("Invalid boolean function", "", c.getGateAt(new Point(1, 0)).getBooleanFunction());
    }

    // Aufbau eines Exklusiv-Oder-Gatters testen (Ausgangswert ist nur "true", wenn genau ein Eingang den Wert "true" hat)
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
        assertFalse("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        c.interactWith(new Point(0, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        assertEquals("Invalid boolean function", "f1 = (a ^ (!b)) v (b ^ (!a))", c.getBooleanFunctions().orElseThrow());
    }

    // Aufbau eines Halbaddierers testen
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

        assertFalse("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        assertFalse("Wrong output", c.getGateAt(new Point(2, 2)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        assertFalse("Wrong output", c.getGateAt(new Point(2, 2)).getOutput());
        c.interactWith(new Point(0, 1));
        assertFalse("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        assertTrue("Wrong output", c.getGateAt(new Point(2, 2)).getOutput());
        c.interactWith(new Point(0, 0));
        assertTrue("Wrong output", c.getGateAt(new Point(4, 0)).getOutput());
        assertFalse("Wrong output", c.getGateAt(new Point(2, 2)).getOutput());

        assertEquals("Invalid boolean functions", "f1 = (a ^ (!b)) v (b ^ (!a)); f2 = a ^ b", c.getBooleanFunctions().orElseThrow());
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

        assertEquals("Wrong boolean function", "f1 = (a ^ b) v c", c.getBooleanFunctions().orElseThrow());
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

        assertEquals("Wrong boolean function", "f1 = !(a ^ b)", c.getBooleanFunctions().orElseThrow());
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

        assertEquals("Wrong boolean function", "f1 = (!(a v b)) ^ c ^ d", c.getBooleanFunctions().orElseThrow());
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

        assertEquals("Wrong boolean functions", "f1 = a ^ b; f2 = !c", c.getBooleanFunctions().orElseThrow());
    }
}
