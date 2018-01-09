package main;

import main.gamemodes.*;
import main.minimax.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

class CPUvCPUThread extends Thread {
    private MainFrame mainFrame;
    private GamePanel gamePanel;

    public CPUvCPUThread(MainFrame mainFrame, GamePanel gamePanel) {
        this.mainFrame = mainFrame;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        while (gamePanel.gameOn) {
            AI ai = gamePanel.getAI(gamePanel.playerOnMove, false);
            ai.start();
            try {
                ai.join();
                gamePanel.makeMove(ai.selectedColumn, ai.selectedRow);
            } catch (InterruptedException e) { ai.interrupt();  }
        }
    }
}

public class GamePanel extends JPanel implements MouseMotionListener, MouseListener {
    private static final double horizontalSelectionMargin = 0.1;
    private static final double verticalSelectionMargin = 0.1;

    private static final String menuText = "Setup initial heap states.";

    private static final int stickWidth = (int) (MainFrame.widthPerHeap * 0.37);
    private static final int ringStartPos = (int) (MainFrame.height * 0.78);
    private static final int ringSpacing = (int) (MainFrame.height * 0.06);
    private static final int ringHeight = (int) (MainFrame.height * 0.12);

    private MainFrame mainFrame;

    private JLabel messageLabel = new JLabel();
    private BufferedImage stickImage;
    private BufferedImage torusFrontImage;
    private BufferedImage torusBackImage;
    private BufferedImage torusFrontTransparentImage;
    private BufferedImage torusBackTransparentImage;

    private volatile int highlightedColumn = -1;
    private volatile int highlightedRow = -1;

    public boolean gameOn = false;
    public GameMode gameMode;
    CPUvCPUThread cpuVScpu;
    public int playerOnMove;
    public int prevMove = MainFrame.maxHeaps;

    public GamePanel(MainFrame mainFrame) throws IOException {
        this.mainFrame = mainFrame;
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.PLAIN, 20));
        add(messageLabel);
        stickImage = ImageIO.read(new File("res/stick.png"));
        torusFrontImage = ImageIO.read(new File("res/torusfront.png"));
        torusBackImage = ImageIO.read(new File("res/torusback.png"));
        torusFrontTransparentImage = ImageIO.read(new File("res/torusfronttransparent.png"));
        torusBackTransparentImage = ImageIO.read(new File("res/torusbacktransparent.png"));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        resetSize();
        addMouseMotionListener(this);
        addMouseListener(this);
        stopGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.ORANGE);
        g.fillRect(0, MainFrame.height * 3/4,
                mainFrame.heapsCo() * MainFrame.widthPerHeap, MainFrame.height * 1/4);

        // paint torus back
        printRings(g, torusBackImage, torusBackTransparentImage);

        // paint sticks
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            g.drawImage(stickImage, (int ) ((MainFrame.widthPerHeap - stickWidth) / 2.0 + i * MainFrame.widthPerHeap),
                    (int) (MainFrame.height * 0.1), stickWidth, (int) (MainFrame.height * 0.8),null);
        }

        // paint tours front
        printRings(g, torusFrontImage, torusFrontTransparentImage);
    }

    private void printRings(Graphics g, BufferedImage image, BufferedImage transparentImage) {
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            int continueIndex = 0;
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                int x1 = (int) (MainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                int y1 = ringStartPos - (j * ringSpacing);
                if (highlightedRow != -1 && highlightedColumn == i &&
                        ((!gameOn && j > highlightedRow) || (gameOn && j >= highlightedRow))) {
                    g.drawImage(transparentImage, x1, y1,
                            (int)(MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                } else {
                    g.drawImage(image, x1, y1,
                            (int) (MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                }
            }
            if (!gameOn && highlightedRow >= mainFrame.heapStates[i] && highlightedColumn == i) {
                for (int j = continueIndex; j <= highlightedRow && j < MainFrame.maxHeaps; j++) {
                    int x1 = (int) (MainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                    int y1 = ringStartPos - (j * ringSpacing);
                    g.drawImage(transparentImage, x1, y1,
                            (int)(MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int xPos = e.getX();
        int yPos = e.getY();

        int columnNo = getColumnNo(xPos);
        if (yPos > MainFrame.height * verticalSelectionMargin && yPos < MainFrame.height * (1 - verticalSelectionMargin)) {
            highlightedColumn = columnNo;
        } else {
            highlightedColumn = -1;
        }

        int rowNo = getRowNo(yPos);
        if (xPos > MainFrame.widthPerHeap * horizontalSelectionMargin &&
                xPos < mainFrame.heapsCo() * MainFrame.widthPerHeap - MainFrame.widthPerHeap * horizontalSelectionMargin) {
            highlightedRow = rowNo;
        } else {
            highlightedRow = -1;
        }
    }

    private int getRowNo(int yPos) {
        if (yPos > ringStartPos + ringHeight || yPos < ringStartPos - (MainFrame.maxHeaps - 1) * ringSpacing) {
            return -1;
        }
        int row = 0;
        while (yPos < ringStartPos - row * ringSpacing) {
            row++;
        }
        return row;
    }

    private int getColumnNo(int xPos) {
        int columnWidth = MainFrame.widthPerHeap;
        int column = xPos / columnWidth;
        xPos %= columnWidth;
        if (xPos < horizontalSelectionMargin * columnWidth || xPos > (1 - horizontalSelectionMargin) * columnWidth) {
            return -1;
        }
        return column;
    }

    public AI getAI(int player, boolean callMakeMove) {
        String level = mainFrame.getCPULevel(player);
        if (level.equals(MainFrame.cpuLabels[0])) {
            return new RandomAi(mainFrame, this, callMakeMove);
        } else if (level.equals(MainFrame.cpuLabels[1])) {
            return new Minimax(mainFrame, this, callMakeMove, prevMove, mainFrame.getCPUDepth(player));
        } else if (level.equals(MainFrame.cpuLabels[2])) {
            return new AlphaBeta(mainFrame, this, callMakeMove, prevMove, mainFrame.getCPUDepth(player));
//        } else if (level.equals(MainFrame.cpuLabels[3])) {
//            return new RandomAi(mainFrame, this);
        } else {
            return null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (highlightedRow == -1 || highlightedColumn == -1) {
            return;
        }

        if (gameOn) {
            if (gameMode.getPlayerType(playerOnMove) == GameMode.PlayerType.HUMAN) {
                if (highlightedRow < mainFrame.heapStates[highlightedColumn]) {
                    // check for invalid move and execute it
                    if (!makeMove(highlightedColumn, highlightedRow)) {
                        return;
                    }

                    if (gameMode.getPlayerType(playerOnMove) == GameMode.PlayerType.CPU) {
                        AI ai = getAI(playerOnMove, true);
                        ai.start();
                    }
                }
            }
        } else {
            // setup heaps
            mainFrame.heapStates[highlightedColumn] =
                    mainFrame.initHeapStates[highlightedColumn] =
                            highlightedRow + 1;
        }
    }

    public boolean isGameFinished(int [] heapStates, int numHeaps) {
        for (int i = 0; i < numHeaps; i++) {
            if (heapStates[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean makeMove(int column, int row) {
        if (!isMoveValid(column, row, mainFrame.heapStates, prevMove)) {
            return false;
        }

        prevMove = mainFrame.heapStates[column] - row;
        mainFrame.heapStates[column] = row;

        if (isGameFinished(mainFrame.heapStates, mainFrame.heapsCo())) {
            JOptionPane.showMessageDialog(null,
                    "No more chips, the winner is " + gameMode.getMessage(playerOnMove) + "!");
            mainFrame.toggleGameToggle();
        } else if (!checkPossibleMove()) {
            JOptionPane.showMessageDialog(null,
                    "No more valid moves, the winner is " + gameMode.getMessage(playerOnMove) + "!");
            mainFrame.toggleGameToggle();
        } else {
            playerOnMove = (playerOnMove + 1) % 2;
            setMessageLabel();
        }

        return true;
    }

    private boolean checkPossibleMove() {
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                if (isMoveValid(i, j, mainFrame.heapStates, prevMove)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMoveValid(int column, int row, int [] states, int prevMove) {
        if (states[column] <= row) {
            return false;
        }
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            if (i == column) {
                continue;
            }
            if (states[i] == row && row != 0) {
                return false;
            }
        }
        if (states[column] - row > 2 * prevMove) {
            return false;
        }
        return true;
    }

    public void mouseExited(MouseEvent e) {
        highlightedRow = highlightedColumn = -1;
    }

    public void resetSize() {
        setPreferredSize(new Dimension(mainFrame.heapsCo() * MainFrame.widthPerHeap, MainFrame.height));
    }

    public void initGame(GameMode gameMode) {
        this.gameMode = gameMode;
        gameOn = true;
        playerOnMove = 0;
        prevMove = MainFrame.maxHeaps;
        setMessageLabel();

        if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU &&
                gameMode.getPlayerType(1) == GameMode.PlayerType.CPU) {
            cpuVScpu = new CPUvCPUThread(mainFrame, this);
            cpuVScpu.start();
        } else if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU) {
            AI ai = getAI(playerOnMove, true);
            ai.start();
        }

    }

    public void setMessageLabel() {
        if (playerOnMove == 0) {
            messageLabel.setForeground(Color.green);
        } else {
            messageLabel.setForeground(Color.blue);
        }
        messageLabel.setText("On the move: " + gameMode.getMessage(playerOnMove));
    }

    public void stopGame() {
        gameOn = false;

        if (cpuVScpu != null) {
            try {
                cpuVScpu.interrupt();
                if (Thread.currentThread() != cpuVScpu) {
                    cpuVScpu.join();
                }
            } catch (InterruptedException e) { e.printStackTrace(); }
            cpuVScpu = null;
        }

        messageLabel.setForeground(Color.black);
        messageLabel.setText(menuText);
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}
}
