package ru.job4j.chess.firuges.black;

import ru.job4j.chess.ImpossibleMoveException;
import ru.job4j.chess.firuges.Cell;
import ru.job4j.chess.firuges.Figure;

public class BishopBlack implements Figure {
    private final Cell position;

    public BishopBlack(final Cell ps) {
        position = ps;
    }

    @Override
    public Cell position() {
        return position;
    }

    @Override
    public Cell[] way(Cell dest) {
        if (!isDiagonal(position, dest)) {
            throw new ImpossibleMoveException(
                    String.format("Could not way by diagonal from %s to %s", position, dest)
            );
        }

        int deltaX = Integer.compare(dest.getX(), position.getX());
        int deltaY = Integer.compare(dest.getY(), position.getY());
        int numberOfSteps = Math.abs(dest.getX() - position.getX());
        Cell[] steps = new Cell[numberOfSteps];

        int x = position.getX();
        int y = position.getY();

        for (int i = 0; i < numberOfSteps; i++) {
            x += deltaX;
            y += deltaY;
            steps[i] = Cell.findBy(x, y);
        }

        return steps;
    }

    public boolean isDiagonal(Cell source, Cell dest) {
            int deltaX = Math.abs(dest.getX() - source.getX());
            int deltaY = Math.abs(dest.getY() - source.getY());

            return deltaX == deltaY;
    }

    @Override
    public Figure copy(Cell dest) {
        return new BishopBlack(dest);
    }
}
