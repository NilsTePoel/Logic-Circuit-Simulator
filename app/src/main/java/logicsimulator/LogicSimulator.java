package logicsimulator;

import logicsimulator.core.LogicCircuit;
import logicsimulator.core.LogicCircuitSimulation;
import logicsimulator.ui.DrawableGate;
import logicsimulator.ui.GateSelectionWindow;
import logicsimulator.ui.MainWindow;
import processing.core.PApplet;

public class LogicSimulator extends PApplet {
    private final int colorWhite = color(255, 255, 255);

    private DrawableGate drawableGate;
    private MainWindow mainWindow;
    private GateSelectionWindow gateSelectionWindow;
    private LogicCircuit circuit = new LogicCircuitSimulation();

    public static void main(String[] args) {
        PApplet.runSketch(new String[] {""}, new LogicSimulator());
    }

    public void settings() {
        size(1400, 780);
    }

    public void setup() {
        surface.setResizable(true);
        surface.setTitle("Logic Circuit Simulator");
        drawableGate = new DrawableGate(super.g, loadImage("enabled_output.png"));
        mainWindow = new MainWindow(super.g, circuit, drawableGate);
        gateSelectionWindow = new GateSelectionWindow(super.g, circuit, drawableGate);
    }

    public void draw() {
        background(colorWhite);
        mainWindow.draw(width, height);
        gateSelectionWindow.draw(width, height, mouseX, mouseY);
    }

    public void mousePressed() {
        mainWindow.onMousePressed(height, mouseX, mouseY, mouseButton);
        gateSelectionWindow.onMousePressed(mouseX, mouseY);
    }

    public void keyPressed() {
        char lowerCaseKey = Character.toLowerCase(key);
        mainWindow.onKeyPressed(lowerCaseKey, keyCode);
        gateSelectionWindow.onKeyPressed(lowerCaseKey);

        // Bisher aufgebaute Schaltung löschen
        if (lowerCaseKey == 'c') {
            circuit = new LogicCircuitSimulation();
            mainWindow = new MainWindow(super.g, circuit, drawableGate);
            gateSelectionWindow = new GateSelectionWindow(super.g, circuit, drawableGate);
        }
    }
}
