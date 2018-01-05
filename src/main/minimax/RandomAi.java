package main.minimax;

import main.GamePanel;
import main.MainFrame;

import java.util.ArrayList;
import java.util.Collections;

public class RandomAi extends AI {

    public RandomAi(MainFrame mainFrame, GamePanel gamePanel) {
        super(mainFrame, gamePanel);
    }

    @Override
    public void run() {
        try { sleep(500); }
        catch (InterruptedException e) { e.printStackTrace(); }

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
                if (gamePanel.makeMove(column, row)) {
                    return;
                }
            }
        }

    }
}
