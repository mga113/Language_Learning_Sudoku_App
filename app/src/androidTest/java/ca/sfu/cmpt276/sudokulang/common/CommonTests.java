package ca.sfu.cmpt276.sudokulang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ca.sfu.cmpt276.sudokulang.common.Util.APP_PACKAGE_NAME;
import static ca.sfu.cmpt276.sudokulang.common.Util.CLICK_TIMEOUT;
import static ca.sfu.cmpt276.sudokulang.common.Util.RANDOM;
import static ca.sfu.cmpt276.sudokulang.common.Util.SELECTOR_TIMEOUT;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.DEVICE;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.bringGameBoardIntoView;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.clickRandomWordButton;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getAllWordButtons;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getId2NoScroll;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getNonemptyVisibleCellCount;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getSpinnerText;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getUpdatedMenus;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getVisibleCellCount;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.getVisibleCells;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.open;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.pause;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.rotateDevice;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.searchForId;
import static ca.sfu.cmpt276.sudokulang.common.Util.TestHelper.selectMenuItem;

import android.os.RemoteException;

import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import ca.sfu.cmpt276.sudokulang.GameActivityTest;
import ca.sfu.cmpt276.sudokulang.HomePage2Test;
import ca.sfu.cmpt276.sudokulang.MainActivityTest;

public class CommonTests {
    private static void clickOutside() {
        DEVICE.click(0, 0);
        DEVICE.waitForWindowUpdate(APP_PACKAGE_NAME, SELECTOR_TIMEOUT);
    }

    /**
     * @implNote Sharable between {@link MainActivityTest} and {@link HomePage2Test} only.
     */
    public static void testSpinner(String spinnerResId) throws UiObjectNotFoundException {
        // Find and open spinner.
        final var spinner = searchForId(spinnerResId);
        open(spinner);
        final var dropdown = DEVICE.findObject(
                new UiSelector().className(android.widget.ListView.class)
        );
        dropdown.waitForExists(SELECTOR_TIMEOUT);
        assertTrue(dropdown.isFocusable());
        assertTrue(dropdown.isEnabled());

        // Get list of dropdown menu items.
        var menus = getUpdatedMenus();
        assertTrue(menus.size() > 0);

        // Assert no choice selected.
        assertTrue(menus.get(0).isChecked());
        for (var menu : menus) {
            if (menus.indexOf(menu) != 0) {
                assertFalse(menu.isChecked());
            }
        }

        // Select one choice.
        final int selectedIndex =
                menus.size() > 1 // If there's a header.
                        ? RANDOM.nextInt(menus.size() - 1) + 1
                        : 0;
        final String selectedText = menus.get(selectedIndex).getText();
        selectMenuItem(menus.get(selectedIndex));

        // Assert spinner changed text.
        assertEquals(selectedText, getSpinnerText(spinner));

        // Assert item was selected in dropdown.
        open(spinner);
        menus = getUpdatedMenus();
        assertTrue(menus.get(selectedIndex).isChecked());

        // Close spinner.
        clickOutside();
        assertEquals(selectedText, getSpinnerText(spinner));
    }

    /**
     * @implNote For {@link GameActivityTest} only.
     */
    public static void testKeypadAndCellView(int numButtons) throws UiObjectNotFoundException {
        // Assert correct number of word buttons.
        assertEquals(numButtons, getAllWordButtons().size());
        for (int i = 0; i < numButtons; i++) {
            final var button = getAllWordButtons().get(i);

            // Assert no empty buttons.
            assertNotEquals("", button.getText().trim());

            // Assert clicking button shows text in quick cell view.
            final var buttonText = button.getText();
            button.click();
            pause(CLICK_TIMEOUT);
            assertEquals(buttonText, searchForId("quick_cell_view").getText());
        }
    }

    /**
     * @implNote For {@link GameActivityTest} only.
     */
    public static void testGameBoardAndCellView(int boardSize) throws UiObjectNotFoundException {
        // Assert correct number of cells.
        final var board = bringGameBoardIntoView();
        assertNotNull(board);
        assertEquals(boardSize * boardSize, getVisibleCellCount(board));

        // Assert clicking cell shows text in quick cell view.
        for (var cell : getVisibleCells(board)) {
            final var cellText = cell.getText();
            cell.click();
            pause(CLICK_TIMEOUT);
            if (cellText == null) {
                assertFalse(searchForId("quick_cell_view").exists());
            } else {
                assertEquals(cellText, searchForId("quick_cell_view").getText());
            }
        }
    }

    /**
     * @implNote For {@link GameActivityTest} only.
     */
    public static void testKeypadBoardEraseViewCombined(int boardSize) throws UiObjectNotFoundException {
        for (int i = 0; i < boardSize * boardSize; i++) {

            // Select each cell.
            final var cell = getVisibleCells(bringGameBoardIntoView()).get(i);
            final var initialCellText = cell.getText();
            final var initialNonemptyCellCount = getNonemptyVisibleCellCount(bringGameBoardIntoView());
            cell.click();

            final var buttonText = clickRandomWordButton(boardSize);
            assertEquals(buttonText, searchForId("quick_cell_view").getText());

            // Assert clicking that button sets text in the selected cell if it's initially not empty.
            var board = bringGameBoardIntoView();
            final var cellText = getVisibleCells(board).get(i).getText();
            if (initialCellText == null) {
                assertEquals(buttonText, cellText);
                assertEquals(initialNonemptyCellCount + 1, getNonemptyVisibleCellCount(board));
            } else {
                assertEquals(initialCellText, cellText);
                assertEquals(initialNonemptyCellCount, getNonemptyVisibleCellCount(board));
            }

            // Assert clicking erase button clears quick cell view and the selected cell.
            searchForId("erase_button").click();
            pause(CLICK_TIMEOUT);
            assertEquals(initialCellText != null, searchForId("quick_cell_view").exists());
            board = bringGameBoardIntoView();
            assertEquals(initialCellText, getVisibleCells(board).get(i).getText());
            assertEquals(initialNonemptyCellCount, getNonemptyVisibleCellCount(board));
        }
    }

    /**
     * @implNote For {@link GameActivityTest} only.
     */
    public static void testRetainStateOnRotation(int boardSize) throws UiObjectNotFoundException, RemoteException {
        var board = bringGameBoardIntoView();
        var cellIndex = RANDOM.nextInt(boardSize * boardSize);
        var cell = getVisibleCells(board).get(cellIndex);
        var initialCellText = cell.getText();

        // Search for a fillable cell and click it.
        while (initialCellText != null) {
            cellIndex = RANDOM.nextInt(boardSize * boardSize);
            cell = getVisibleCells(board).get(cellIndex);
            initialCellText = cell.getText();
        }
        cell.click();
        final var initialNonemptyCellCount = getNonemptyVisibleCellCount(board);

        // Click a random word button.
        final var button = getAllWordButtons().get(RANDOM.nextInt(boardSize));
        final var buttonText = button.getText();
        button.click();

        // Act.
        rotateDevice();

        // Assert cell still has the same text.
        board = bringGameBoardIntoView();
        cell = getVisibleCells(board).get(cellIndex);
        assertEquals(buttonText, cell.getText());
        assertEquals(buttonText, getId2NoScroll("quick_cell_view").getText());
        assertEquals(initialNonemptyCellCount + 1, getNonemptyVisibleCellCount(board));
    }
}