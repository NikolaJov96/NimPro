package main.minimax;

import main.GamePanel;
import main.MainFrame;

public class AI extends Thread {
    protected MainFrame mainFrame;
    protected GamePanel gamePanel;

    public AI(MainFrame mainFrame, GamePanel gamePanel) {
        this.mainFrame = mainFrame;
        this.gamePanel = gamePanel;
    }
}
