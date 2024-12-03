package ru.job4j.chess;

import org.junit.jupiter.api.Test;
import ru.job4j.chess.firuges.Cell;
import ru.job4j.chess.firuges.black.BishopBlack;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LogicTest {

    @Test
    public void whenMoveThenFigureNotFoundException()
            throws FigureNotFoundException, OccupiedCellException, ImpossibleMoveException {
        Logic logic = new Logic();
        FigureNotFoundException exception = assertThrows(FigureNotFoundException.class, () -> {
            logic.move(Cell.C1, Cell.H6);
        });
        assertThat(exception.getMessage()).isEqualTo("Figure not found on the board.");
    }

    @Test
    void testMoveImpossibleMoveException() {
        Logic logic = new Logic();
        BishopBlack bishop = new BishopBlack(Cell.C7);
        logic.add(bishop);

        ImpossibleMoveException exception = assertThrows(ImpossibleMoveException.class, () -> {
            logic.move(Cell.C7, Cell.D2);
        });

        assertThat(exception.getMessage()).isEqualTo("Could not way by diagonal from C7 to D2");
    }

    @Test
    void testMoveOccupiedCellException() {
        Logic logic = new Logic();
        BishopBlack bishop1 = new BishopBlack(Cell.C7);
        BishopBlack bishop2 = new BishopBlack(Cell.B6);
        logic.add(bishop1);
        logic.add(bishop2);

        OccupiedCellException exception = assertThrows(OccupiedCellException.class, () -> {
            logic.move(Cell.C7, Cell.B6);
        });

        assertThat(exception.getMessage()).isEqualTo("Cell is occupied by another figure.");
    }

}