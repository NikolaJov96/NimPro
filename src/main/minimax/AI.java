package main.minimax;

import main.GamePanel;
import main.MainFrame;

public class AI extends Thread {
    protected MainFrame mainFrame;
    protected GamePanel gamePanel;
    protected boolean callMakeMove;

    public int selectedColumn;
    public int selectedRow;

    private static final long minWaitTime = 500;
    private long startTime;

    public AI(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove) {
        this.mainFrame = mainFrame;
        this.gamePanel = gamePanel;
        this.callMakeMove = callMakeMove;
    }

    protected void moveStart() {
        startTime = System.currentTimeMillis();
    }

    protected void moveEnd() {
        long endTime = System.currentTimeMillis();
        if (endTime - startTime < minWaitTime) try {
            sleep(minWaitTime - (endTime - startTime));
        } catch (InterruptedException e) { return; }
    }
}
