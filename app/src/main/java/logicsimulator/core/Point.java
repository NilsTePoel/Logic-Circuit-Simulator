package logicsimulator.core;

public record Point(int x, int y) {
    @Override
    public String toString() {
        return "[%d, %d]".formatted(x, y);
    }
}
