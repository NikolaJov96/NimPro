package etf.nim.jn150081d.minimax;

import etf.nim.jn150081d.MainFrame;
import etf.nim.jn150081d.GamePanel;

/**
 * AI is abstract class representing an AI bot
 */
public class AI extends Thread {
    MainFrame mainFrame;
    GamePanel gamePanel;
    boolean callMakeMove;

    public int selectedColumn;
    public int selectedRow;

    private static final long minWaitTime = 500;
    private long startTime;

    /**
     * Universal AI constructor
     *
     * @param mainFrame assigned mainFrame
     * @param gamePanel assigned gamePanel
     * @param callMakeMove should this AI call makeMove method or not
     */
    public AI(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove) {
        this.mainFrame = mainFrame;
        this.gamePanel = gamePanel;
        this.callMakeMove = callMakeMove;
    }

    /**
     * To be called on beginning of run method, used for measuring time
     */
    void moveStart() {
        startTime = System.currentTimeMillis();
    }

    /**
     * To be called on the end of rum method, if calculation was shorter than minimal length, wait till that duration
     */
    void moveEnd() {
        long endTime = System.currentTimeMillis();
        if (endTime - startTime < minWaitTime) try {
            sleep(minWaitTime - (endTime - startTime));
        } catch (InterruptedException ignored) {}
    }
}
