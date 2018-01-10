package etf.nim.jn150081d.minimax;

import etf.nim.jn150081d.GamePanel;
import etf.nim.jn150081d.MainFrame;

import java.util.ArrayList;
import java.util.Collections;

/**
 * RandomAi represents most basic bot that plays random moves
 */
public class RandomAi extends AI {

    /**
     * RandomAi constructor
     *
     * @param mainFrame assigned mainFrame
     * @param gamePanel assigned gamePanel
     * @param callMakeMove should this AI call makeMove method or not
     */
    public RandomAi(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove) {
        super(mainFrame, gamePanel, callMakeMove);
    }

    /**
     * Calculates move and calls makeMove is needed
     */
    @Override
    public void run() {
        moveStart();

        ArrayList<Integer> columns = new ArrayList<>(mainFrame.heapsCo());
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            columns.add(i);
        }
        Collections.shuffle(columns);
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            int column = columns.get(i);

            ArrayList<Integer> rows = new ArrayList<>(mainFrame.heapStates[column]);
            for (int j = 0; j < mainFrame.heapStates[column]; j++) {
                rows.add(j);
            }
            Collections.shuffle(rows);
            for (int j = 0; j < mainFrame.heapStates[column]; j++) {
                int row = rows.get(j);
                if (gamePanel.isMoveValid(column, row, mainFrame.heapStates, gamePanel.prevMove)) {
                    if (callMakeMove) {
                        gamePanel.makeMove(column, row);
                    } else {
                        selectedColumn = column;
                        selectedRow = row;
                    }
                    moveEnd();
                    return;
                }
            }
        }
    }
}
