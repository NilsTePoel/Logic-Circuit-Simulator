package logicsimulator.ui;

import logicsimulator.core.gate.Gate;
import logicsimulator.core.gate.GateType;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.Map;

public class DrawableGate {
    private final PGraphics g;
    private final PImage enabledOutput;
    private final int colorWhite, colorBlack, colorBlue, colorRed;

    private final Map<GateType, String> symbols = Map.of(GateType.AND, "&", GateType.OR, ">=1",
            GateType.EXCLUSIVE_OR, "=1", GateType.NOT, "1");

    public DrawableGate(PGraphics g, PImage enabledOutput) {
        this.g = g;
        this.enabledOutput = enabledOutput;
        colorWhite = g.color(255, 255, 255);
        colorBlack = g.color(0, 0, 0);
        colorBlue = g.color(30, 30, 200);
        colorRed = g.color(255, 0, 0);
    }

    public void draw(int x, int y, GateType type, int gateColor, boolean isEnabled) {
        g.stroke(gateColor);
        g.strokeWeight(3);
        g.fill(colorWhite);
        if (type == GateType.INPUT) {
            g.fill(gateColor);
            g.circle(x + 30, y + 30, 30);
            g.line(x + 45, y + 30, x + 50, y + 30);
        } else if (type == GateType.OUTPUT) {
            if (isEnabled) g.image(enabledOutput, x, y);
            g.circle(x + 30, y + 30, 30);
            g.line(x + 10, y + 30, x + 15, y + 30);
            g.line(x + 20, y + 20, x + 40, y + 40);
            g.line(x + 20, y + 40, x + 40, y + 20);
        } else {
            if (type == GateType.NOT) {
                g.rect(x + 10, y + 10, 35, 40);
                g.circle(x + 50, y + 30, 5);
            } else {
                g.rect(x + 10, y + 10, 40, 40);
            }

            g.fill(gateColor);
            g.textSize(18);
            g.text(symbols.get(type), x + 30, y + 35);
        }
    }

    public void draw(int x, int y, Gate gate, boolean isSelected) {
        int gateColor = getGateColor(gate, isSelected);
        draw(x, y, gate.getType(), gateColor, gate.getOutput());

        g.fill(gateColor);
        g.textSize(12);
        g.text(gate.getName().orElse(""), x + 30, y + 10);
    }

    public void drawConnection(int startX, int startY, int endX, int endY, Gate inputGate, boolean isInputSelected) {
        g.stroke(getGateColor(inputGate, isInputSelected));
        g.line(startX + 10, startY + 30, endX + 50, endY + 30);
    }

    private int getGateColor(Gate gate, boolean isSelected) {
        return isSelected ? colorBlue : (gate.getOutput() ? colorRed : colorBlack);
    }
}
