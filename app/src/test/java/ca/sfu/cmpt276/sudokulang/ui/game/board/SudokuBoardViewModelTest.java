package ca.sfu.cmpt276.sudokulang.ui.game.board;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ca.sfu.cmpt276.sudokulang.ui.InstantExecutorExtension;

@ExtendWith(InstantExecutorExtension.class)
class SudokuBoardViewModelTest {
    private SudokuBoardViewModel board;

    @BeforeEach
    void setUp() {
        board = new SudokuBoardViewModel();
    }

    @Test
    void createEmptyBoard() {
        board.createEmptyBoard(9, 3, 3);
        assertEquals(9, board.getBoardSize().getValue());
        assertEquals(3, board.getSubgridHeight().getValue());
        assertEquals(3, board.getSubgridWidth().getValue());
    }

    @Test
    void getSelectedCell() {
        board.createEmptyBoard(4, 2, 2);
        board.setSelectedCell(0, 2);
        var selectedCell = board.getSelectedCell().getValue();
        assertEquals(0, selectedCell.getRowIndex().getValue());
        assertEquals(2, selectedCell.getColIndex().getValue());
    }

    @Test
    void setSelectedCell() {
        board.createEmptyBoard(9, 3, 3);
        board.setSelectedCell(0, 3);
        var selectedCell = board.getSelectedCell().getValue();
        assertEquals(0, selectedCell.getRowIndex().getValue());
        assertEquals(3, selectedCell.getColIndex().getValue());

    }

    @Test
    void setNoSelectedCell() {
        board.createEmptyBoard(4, 2, 2);
        board.setSelectedCell(0, 2);
        board.setNoSelectedCell();
        assertNull(board.getSelectedCell().getValue());
    }

    @Test
    void updateNumEmptyCells() {
        board.createEmptyBoard(4, 2, 2);
        assertEquals(16, board.getNumEmptyCells().getValue());
        board.setSelectedCell(1, 3);
        var selectedCell = board.getSelectedCell().getValue();
        selectedCell.setText("Non-empty");
        board.updateNumEmptyCells();
        assertEquals(15, board.getNumEmptyCells().getValue());
    }

    @Test
    void getNumEmptyCells() {
        // NOTE: updateNumEmptyCells() must me called right before getNumEmptyCells() for it to work correctly.
        board.createEmptyBoard(4, 2, 2);
        assertEquals(16, board.getNumEmptyCells().getValue());
        board.setSelectedCell(3, 1);
        var selectedCell = board.getSelectedCell().getValue();
        selectedCell.setText("Null");
        board.updateNumEmptyCells();
        assertEquals(15, board.getNumEmptyCells().getValue());
    }

    @Test
    void getBoardSize() {
        board.createEmptyBoard(4, 2, 2);
        assertEquals(4, board.getBoardSize().getValue());
    }

    @Test
    void getSubgridHeight() {
        board.createEmptyBoard(4, 1, 4);
        assertEquals(1, board.getSubgridHeight().getValue());
    }

    @Test
    void getSubgridWidth() {
        board.createEmptyBoard(4, 1, 4);
        assertEquals(4, board.getSubgridWidth().getValue());
    }

    @Test
    void isValidBoard() {
        board.createEmptyBoard(4, 2, 2);
        assertEquals(16, board.getNumEmptyCells().getValue());
        board.setSelectedCell(3, 1);
        var selectedCell = board.getSelectedCell().getValue();
        assertTrue(board.isValidBoard());
        selectedCell.setAsErrorCell(true);
        assertFalse(board.isValidBoard());
    }

    @Test
    void getCells() {
        final var cells = board.getCells().getValue();
        final var boardSize = board.getBoardSize().getValue();
        assertEquals(boardSize, cells.length);
        assertEquals(boardSize, cells[0].length);
    }

    @Test
    void getDataValuePairs() {
        assertEquals(board.getBoardSize().getValue(), board.getDataValuePairs().length);
    }

    @Test
    void generateBoardData() {
        assertDoesNotThrow(() -> board.generateBoardData());
    }

    @Test
    void isValidValueForCell() {
        board.createEmptyBoard(9, 3, 3);
        board.setSelectedCell(5, 5);
        var selectedCell = board.getSelectedCell().getValue();
        selectedCell.setText("Br");
        assertTrue(board.isValidValueForCell("Ca", 5, 4));
        assertFalse(board.isValidValueForCell("Br", 7, 5));
        assertFalse(board.isValidValueForCell("Br", 5, 0));
        assertFalse(board.isValidValueForCell("Br", 3, 3));

        board.setSelectedCell(4, 5);
        selectedCell = board.getSelectedCell().getValue();
        assertFalse(board.isValidValueForCell("Br", selectedCell));

        board.createEmptyBoard(4, 4, 4);
        board.setSelectedCell(0, 0);
        selectedCell = board.getSelectedCell().getValue();
        selectedCell.setText("Kr");
        assertTrue(board.isValidValueForCell("Ca", 0, 3));
        assertFalse(board.isValidValueForCell("Kr", 3, 0));
    }
}