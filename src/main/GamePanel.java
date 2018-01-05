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
            AI ai = new RandomAi(mainFrame, gamePanel);
            ai.start();
            try { ai.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }
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
    private GameMode gameMode;
    private int playerOnMove;
    private int prevMove = MainFrame.maxHeaps;

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
        g.fillRect(0, mainFrame.height * 3/4,
                mainFrame.heapsCo() * mainFrame.widthPerHeap, mainFrame.height * 1/4);

        // paint torus back
        printRings(g, torusBackImage, torusBackTransparentImage);

        // paint sticks
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            g.drawImage(stickImage, (int ) ((mainFrame.widthPerHeap - stickWidth) / 2.0 + i * mainFrame.widthPerHeap),
                    (int) (mainFrame.height * 0.1), stickWidth, (int) (mainFrame.height * 0.8),null);
        }

        // paint tours front
        printRings(g, torusFrontImage, torusFrontTransparentImage);
    }

    private void printRings(Graphics g, BufferedImage image, BufferedImage transparentImage) {
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            int continueIndex = 0;
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                int x1 = (int) (mainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                int y1 = ringStartPos - (j * ringSpacing);
                if (highlightedRow != -1 && highlightedColumn == i &&
                        ((!gameOn && j > highlightedRow) || (gameOn && j >= highlightedRow))) {
                    g.drawImage(transparentImage, x1, y1,
                            (int)(mainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                } else {
                    g.drawImage(image, x1, y1,
                            (int) (mainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                }
            }
            if (!gameOn && highlightedRow >= mainFrame.heapStates[i] && highlightedColumn == i) {
                for (int j = continueIndex; j <= highlightedRow && j < mainFrame.maxHeaps; j++) {
                    int x1 = (int) (mainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                    int y1 = ringStartPos - (j * ringSpacing);
                    g.drawImage(transparentImage, x1, y1,
                            (int)(mainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
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
        if (yPos > mainFrame.height * verticalSelectionMargin && yPos < mainFrame.height * (1 - verticalSelectionMargin)) {
            highlightedColumn = columnNo;
        } else {
            highlightedColumn = -1;
        }

        int rowNo = getRowNo(yPos);
        if (xPos > mainFrame.widthPerHeap * horizontalSelectionMargin &&
                xPos < mainFrame.heapsCo() * mainFrame.widthPerHeap - mainFrame.widthPerHeap * horizontalSelectionMargin) {
            highlightedRow = rowNo;
        } else {
            highlightedRow = -1;
        }
    }

    private int getRowNo(int yPos) {
        if (yPos > ringStartPos + ringHeight || yPos < ringStartPos - (mainFrame.maxHeaps - 1) * ringSpacing) {
            return -1;
        }
        int row = 0;
        while (yPos < ringStartPos - row * ringSpacing) {
            row++;
        }
        return row;
    }

    private int getColumnNo(int xPos) {
        int columnWidth = mainFrame.widthPerHeap;
        int column = xPos / columnWidth;
        xPos %= columnWidth;
        if (xPos < horizontalSelectionMargin * columnWidth || xPos > (1 - horizontalSelectionMargin) * columnWidth) {
            return -1;
        }
        return column;
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
                        AI ai = new RandomAi(mainFrame, this);
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

    public boolean makeMove(int column, int row) {
        if (!isMoveValid(column, row, mainFrame.heapStates, prevMove)) {
            return false;
        }

        prevMove = mainFrame.heapStates[column] - row;
        mainFrame.heapStates[column] = row;

        boolean gameFinished = true;
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            if (mainFrame.heapStates[i] != 0) {
                gameFinished = false;
            }
        }
        if (gameFinished) {
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
        setPreferredSize(new Dimension(mainFrame.heapsCo() * mainFrame.widthPerHeap, mainFrame.height));
    }

    public void initGame(GameMode gameMode) {
        this.gameMode = gameMode;
        gameOn = true;
        playerOnMove = 0;
        prevMove = mainFrame.maxHeaps;
        setMessageLabel();

        if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU &&
                gameMode.getPlayerType(1) == GameMode.PlayerType.CPU) {
            CPUvCPUThread thread = new CPUvCPUThread(mainFrame, this);
            thread.start();
        } else if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU) {
            AI ai = new RandomAi(mainFrame, this);
            ai.start();
        }

    }

    private void setMessageLabel() {
        if (playerOnMove == 0) {
            messageLabel.setForeground(Color.green);
        } else {
            messageLabel.setForeground(Color.blue);
        }
        messageLabel.setText("On the move: " + gameMode.getMessage(playerOnMove));
    }

    public void stopGame() {
        gameOn = false;
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
