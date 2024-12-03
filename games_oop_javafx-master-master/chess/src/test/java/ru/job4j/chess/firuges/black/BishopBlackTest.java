package ru.job4j.chess.firuges.black;

import org.junit.jupiter.api.Test;
import ru.job4j.chess.ImpossibleMoveException;
import ru.job4j.chess.firuges.Cell;
import ru.job4j.chess.firuges.Figure;

import static org.junit.jupiter.api.Assertions.*;

class BishopBlackTest {

    @Test
    void testPosition() {
        Cell initialPosition = Cell.A1;
        BishopBlack bishop = new BishopBlack(initialPosition);

        assertEquals(initialPosition, bishop.position());
    }

    @Test
    void testWay() {
        BishopBlack bishop = new BishopBlack(Cell.C1);
        Cell destination = Cell.G5;

        Cell[] expectedWay = {Cell.D2, Cell.E3, Cell.F4, Cell.G5};
        assertArrayEquals(expectedWay, bishop.way(destination));
    }

    @Test
    void testInvalidWay() {
        BishopBlack bishop = new BishopBlack(Cell.C1);
        Cell destination = Cell.E5;

        assertThrows(ImpossibleMoveException.class, () -> bishop.way(destination));
    }

    @Test
    void testIsDiagonal() {
        BishopBlack bishop = new BishopBlack(Cell.A1);
        Cell diagonalCell = Cell.B2;
        Cell nonDiagonalCell = Cell.C1;

        assertTrue(bishop.isDiagonal(Cell.A1, diagonalCell));
        assertFalse(bishop.isDiagonal(Cell.A1, nonDiagonalCell));
    }

    @Test
    void testCopy() {
        BishopBlack bishop = new BishopBlack(Cell.C1);
        Figure copiedBishop = bishop.copy(Cell.D2);

        assertEquals(Cell.D2, copiedBishop.position());
        assertTrue(copiedBishop instanceof BishopBlack);
    }
}