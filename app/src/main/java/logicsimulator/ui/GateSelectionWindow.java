package logicsimulator.ui;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.gate.GateType;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.Map;

public class GateSelectionWindow {
    private final PGraphics g;
    private final LogicCircuit circuit;
    private final DrawableGate drawableGate;
    private final int colorBlack, colorGrey, colorDarkGrey, colorWhite, colorBlue;
    public static final int WINDOW_WIDTH = 140;
    private static final int SELECTION_SIZE = MainWindow.TILE_SIZE + 15;

    private final Map<GateType, String> names = Map.of(GateType.AND, "Und", GateType.OR, "Oder",
            GateType.EXCLUSIVE_OR, "Exklusiv-Oder", GateType.NOT, "Nicht", GateType.INPUT, "Eingang",
            GateType.OUTPUT, "Ausgang");

    public GateSelectionWindow(PGraphics g, LogicCircuit circuit, DrawableGate drawableGate) {
        this.g = g;
        this.circuit = circuit;
        this.drawableGate = drawableGate;
        colorBlack = g.color(0, 0, 0);
        colorGrey = g.color(128, 128, 128);
        colorDarkGrey = g.color(85, 85, 85);
        colorWhite = g.color(255, 255, 255);
        colorBlue = g.color(30, 30, 200);
    }

    public void draw(int width, int height, int mouseX, int mouseY) {
        drawBackground(height);
        drawGates(mouseX, mouseY);
        drawBooleanFunctions(width, height);
    }

    private void drawBackground(int height) {
        g.noStroke();
        g.fill(colorGrey);
        g.rect(0, 0, WINDOW_WIDTH, height);
        g.fill(colorWhite);
        g.textSize(18);
        g.textAlign(PConstants.CENTER);
        g.text("Gatter-Auswahl:", WINDOW_WIDTH / 2F, 20);
    }

    private void drawGates(int mouseX, int mouseY) {
        for (GateType type : GateType.values()) {
            int gateX = type.ordinal() * SELECTION_SIZE + 30;
            g.noStroke();
            if (circuit.getSelectedType() == type) {
                g.fill(colorBlue);
                g.rect(0, gateX, WINDOW_WIDTH, SELECTION_SIZE);
            } else if (mouseX <= WINDOW_WIDTH && mouseY >= gateX && mouseY <= gateX + SELECTION_SIZE) {
                g.fill(colorDarkGrey);
                g.rect(0, gateX, WINDOW_WIDTH, SELECTION_SIZE);
            }
            drawableGate.draw(WINDOW_WIDTH / 2 - 30, type.ordinal() * SELECTION_SIZE + 30, type, colorBlack, false);

            g.fill(colorWhite);
            g.textSize(16);
            g.text(names.get(type), WINDOW_WIDTH / 2F, type.ordinal() * SELECTION_SIZE + 100);
        }
    }

    private void drawBooleanFunctions(int width, int height) {
        g.noStroke();
        g.fill(colorGrey);
        g.rect(0, height - MainWindow.TILE_SIZE, width, height);
        g.fill(colorWhite);
        g.text(circuit.getBooleanFunctions().orElse("Es wurden noch keine Ausg\u00E4nge hinzugef\u00FCgt."), 0, height - MainWindow.TILE_SIZE, width, height);
    }

    public void onMousePressed(int mouseX, int mouseY) {
        if (mouseX <= WINDOW_WIDTH) {
            int selectedGate = (mouseY - 30) / SELECTION_SIZE;
            if (selectedGate < GateType.values().length) circuit.setSelectedType(GateType.values()[selectedGate]);
        }
    }

    public void onKeyPressed(char key) {
        if (Character.isDigit(key)) {
            int index = Integer.parseInt(String.valueOf(key)) - 1;
            if (index >= 0 && index < GateType.values().length) {
                circuit.setSelectedType(GateType.values()[index]);
            }
        }
    }
}
