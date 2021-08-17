package logicsimulator.ui;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.stream.IntStream;

import static logicsimulator.ui.GateSelectionWindow.WINDOW_WIDTH;

public class MainWindow {
    private final PGraphics g;
    private final LogicCircuit circuit;
    private final DrawableGate drawableGate;
    private final int colorBlack, colorRed;
    public static final int TILE_SIZE = 60;

    private int xOffset, yOffset;
    private long errorMessageDuration;

    public MainWindow(PGraphics g, LogicCircuit circuit, DrawableGate drawableGate) {
        this.g = g;
        this.circuit = circuit;
        this.drawableGate = drawableGate;
        colorBlack = g.color(0, 0, 0);
        colorRed = g.color(255, 0, 0);
    }

    public void draw(int width, int height) {
        drawCoordinateSystem(width, height);
        drawGates();
        drawErrorMessage(width);
    }

    private void drawCoordinateSystem(int width, int height) {
        g.strokeWeight(1);
        g.stroke(colorBlack);

        IntStream.range(1, height / TILE_SIZE)
                .forEach(row -> g.line(WINDOW_WIDTH, row * TILE_SIZE, width, row * TILE_SIZE));
        IntStream.range(1, width / TILE_SIZE)
                .forEach(column -> g.line(WINDOW_WIDTH + column * TILE_SIZE, 0, WINDOW_WIDTH + column * TILE_SIZE, height));
    }

    private void drawGates() {
        for (Point pos : circuit.getGatePositions()) {
            Point offsetPos = new Point(pos.x() + xOffset, pos.y() + yOffset);
            drawableGate.draw(offsetPos.x() * TILE_SIZE + WINDOW_WIDTH, offsetPos.y() * TILE_SIZE, circuit.getGateAt(pos), circuit.isSelected(pos));

            for (Point inputPosition : circuit.getGateAt(pos).getInputs()) {
                Point offsetInput = new Point(inputPosition.x() + xOffset, inputPosition.y() + yOffset);
                drawableGate.drawConnection(offsetPos.x() * TILE_SIZE + WINDOW_WIDTH, offsetPos.y() * TILE_SIZE,
                        offsetInput.x() * TILE_SIZE + WINDOW_WIDTH, offsetInput.y() * TILE_SIZE,
                        circuit.getGateAt(inputPosition), circuit.isSelected(inputPosition));
            }
        }
    }

    private void drawErrorMessage(int width) {
        if (System.currentTimeMillis() < errorMessageDuration) {
            g.textSize(18);
            g.fill(colorRed);
            g.text("Verbindung nicht m\u00F6glich.", width / 2F, 20);
        }
    }

    public void onMousePressed(int height, int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= WINDOW_WIDTH && mouseY <= height - TILE_SIZE) {
            Point pos = new Point((mouseX - WINDOW_WIDTH) / TILE_SIZE - xOffset, mouseY / TILE_SIZE - yOffset);
            if (mouseButton == PConstants.LEFT && !circuit.addGate(pos)) {
                circuit.toggleSelection(pos);
            } else if (mouseButton == PConstants.RIGHT && !circuit.interactWith(pos)) {
                errorMessageDuration = System.currentTimeMillis() + 2000;
            }
        }
    }

    public void onKeyPressed(char key, int keyCode) {
        if (key == 'r') {
            circuit.removeSelectedGate();
        } else if (key == 'm') {
            xOffset = yOffset = 0;
        } else if (key == PConstants.CODED) {
            switch (keyCode) {
                case PConstants.UP -> yOffset--;
                case PConstants.DOWN -> yOffset++;
                case PConstants.LEFT -> xOffset--;
                case PConstants.RIGHT -> xOffset++;
            }
        }
    }
}
