package logicsimulator.ui;

import logicsimulator.arduino.ArduinoSketch;
import logicsimulator.core.LogicCircuit;
import logicsimulator.core.Point;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static logicsimulator.ui.GateSelectionWindow.WINDOW_WIDTH;

public class MainWindow {
    private final PGraphics g;
    private final LogicCircuit circuit;
    private final DrawableGate drawableGate;
    private final int colorBlack, colorGrey, colorDarkGrey, colorWhite, colorRed;
    public static final int TILE_SIZE = 60;

    private int xOffset, yOffset;
    private String message;
    private long messageDuration;
    private boolean showContextMenu;

    public MainWindow(PGraphics g, LogicCircuit circuit, DrawableGate drawableGate) {
        this.g = g;
        this.circuit = circuit;
        this.drawableGate = drawableGate;
        colorBlack = g.color(0, 0, 0);
        colorGrey = g.color(128, 128, 128);
        colorDarkGrey = g.color(85, 85, 85);
        colorWhite = g.color(255, 255, 255);
        colorRed = g.color(255, 0, 0);
    }

    public void draw(int width, int height, int mouseX, int mouseY) {
        drawCoordinateSystem(width, height);
        drawGates();
        drawErrorMessage(width);
        drawContextMenu(width, mouseX, mouseY);
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
        if (System.currentTimeMillis() < messageDuration) {
            g.textSize(18);
            g.fill(colorRed);
            g.text(message, width / 2F, 20);
        }
    }

    private void drawContextMenu(int width, int mouseX, int mouseY) {
        if (showContextMenu) {
            g.fill(colorGrey);
            g.noStroke();
            g.rect(width - 185, 5, 180, 60);
            g.fill(colorDarkGrey);
            if (mouseX >= width - 185) {
                if (mouseY <= 30) {
                    g.rect(width - 185, 5, 180, 30);
                } else if (mouseY <= 60) {
                    g.rect(width - 185, 35, 180, 30);
                }
            }
            g.fill(colorWhite);
            g.textSize(18);
            g.text("Wertetabelle", width - 125, 25);
            g.text("Arduino-Sketch", width - 113, 55);
        }
    }

    public void onMousePressed(int width, int height, int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= width - 185) {
            if (mouseY <= 30) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(circuit.getTableOfValues().orElseThrow().toString()), null);
                message = "Wertetabelle in die Zwischenablage kopiert.";
                messageDuration = System.currentTimeMillis() + 2000;
            } else if (mouseY <= 60) {
                PApplet.selectOutput("Arduino-Sketch speichern", "saveArduinoSketch", null, this, null);
            }
        } else if (mouseX >= WINDOW_WIDTH && mouseY <= height - TILE_SIZE) {
            Point pos = new Point((mouseX - WINDOW_WIDTH) / TILE_SIZE - xOffset, mouseY / TILE_SIZE - yOffset);
            if (mouseButton == PConstants.LEFT && !circuit.addGate(pos)) {
                circuit.toggleSelection(pos);
                showContextMenu = circuit.getTableOfValues().isPresent();
            } else if (mouseButton == PConstants.RIGHT && !circuit.interactWith(pos)) {
                message = "Verbindung nicht m\u00F6glich.";
                messageDuration = System.currentTimeMillis() + 2000;
            }
        }
    }

    public void onKeyPressed(char key, int keyCode) {
        if (key == 'r') {
            circuit.removeSelectedGate();
            showContextMenu = false;
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

    @SuppressWarnings("unused")
    public void saveArduinoSketch(File selection) {
        if (selection != null) {
            ArduinoSketch arduinoSketch = new ArduinoSketch(circuit.getTableOfValues().orElseThrow(), selection.toPath());
            try {
                arduinoSketch.saveToDisk();
                message = "Der Arduino-Sketch wurde erfolgreich gespeichert.";
            } catch (IOException e) {
                message = "Der Arduino-Sketch konnte nicht gespeichert werden!";
            }
            messageDuration = System.currentTimeMillis() + 2000;
        }
    }
}
