package ca.sfu.cmpt276.sudokulang.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import ca.sfu.cmpt276.sudokulang.databinding.FragmentGameBinding;
import ca.sfu.cmpt276.sudokulang.ui.game.board.SudokuCell;

// See: https://developer.android.com/topic/libraries/architecture/viewmodel
public class GameFragment extends Fragment {
    private FragmentGameBinding mBinding;
    private GameViewModel mGameViewModel;
    private boolean mIsCompletedGame = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = FragmentGameBinding.inflate(inflater, container, false);
        mGameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // Check if recreating a previously destroyed instance.
        if (shouldCreateNewGame(savedInstanceState)) {
            mGameViewModel.generateNewBoard(9, 3, 3);
        }

        // Set OnClickListener for parent view of game board.
        ((View) mBinding.gameBoard.getParent()).setOnClickListener(v -> mGameViewModel.setNoSelectedCell());
        setupBoard();
        setupWordButtons();
        mBinding.eraseButton.setOnClickListener(v -> mGameViewModel.clearSelectedCell());

        return mBinding.getRoot();
    }

    private boolean shouldCreateNewGame(@Nullable Bundle savedInstanceState) {
        return savedInstanceState == null || savedInstanceState.getBoolean("should_create_new_game");
    }

    private void endGame() {
        mIsCompletedGame = true;
        for (var button : getAllWordButtons()) {
            button.setEnabled(false);
        }
        mBinding.eraseButton.setEnabled(false);
        mBinding.notesButton.setEnabled(false);
        Snackbar.make(mBinding.getRoot(), "Game completed. Well done!", Snackbar.LENGTH_INDEFINITE).show();
    }

    private void setupBoard() {
        mBinding.gameBoard.createBoard(mGameViewModel.getBoardUiState().getValue());
        mGameViewModel.getBoardUiState().observe(getViewLifecycleOwner(), boardUiState -> {
            mBinding.gameBoard.updateState(boardUiState);
            final var selectedCell = boardUiState.getSelectedCell();
            mBinding.quickCellView.setText(selectedCell == null ? "" : selectedCell.getText());
            if (boardUiState.isSolvedBoard() && !mIsCompletedGame) {
                endGame();
            }
        });
        mBinding.gameBoard.setOnclickListenersForAllCells(view -> {
            final var cell = (SudokuCell) view;
            mGameViewModel.setSelectedCell(cell.getRowIndex(), cell.getColIndex());
        });
    }

    private void setupWordButtons() {
        final var wordButtons = getAllWordButtons();
        final var dataValuePairs = mGameViewModel.getDataValuePairs();
        assert (wordButtons.length == dataValuePairs.length);
        for (int i = 0; i < wordButtons.length; i++) {
            wordButtons[i].setText(dataValuePairs[i].first);
            wordButtons[i].setOnClickListener(view -> {
                final var button = (Button) view;
                final String choice = (String) button.getText();
                mBinding.quickCellView.setText(choice);
                mGameViewModel.setSelectedCellText(choice);
            });
        }
    }

    private Button[] getAllWordButtons() {
        var buttons = new ArrayList<Button>();
        buttons.add(mBinding.wordButton1);
        buttons.add(mBinding.wordButton2);
        buttons.add(mBinding.wordButton3);
        buttons.add(mBinding.wordButton4);
        buttons.add(mBinding.wordButton5);
        buttons.add(mBinding.wordButton6);
        buttons.add(mBinding.wordButton7);
        buttons.add(mBinding.wordButton8);
        buttons.add(mBinding.wordButton9);
        return buttons.toArray(new Button[0]);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save game state to the instance state bundle.
        outState.putBoolean("should_create_new_game", false);
    }
}
