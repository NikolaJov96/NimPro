package etf.nim.jn150081d;

import etf.nim.jn150081d.gamemodes.GameMode;
import etf.nim.jn150081d.minimax.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * CPUvCPUThread is used when CPU vs CPU mode is selected, it runs selected AIs nad waits for them to finish the move
 */
class CPUvCPUThread extends Thread {
    private GamePanel gamePanel;

    /**
     * CPUvCPUThread constructor
     *
     * @param gamePanel parent gamePanel needed to access needed information
     */
    CPUvCPUThread(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Alternates between AI opponents and waits for their moves until the end of the game or an interrupt
     */
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

/**
 * GamePanel is an actual drawing panel and a class that encapsulates the game data
 */
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

    boolean gameOn = false;
    private GameMode gameMode;
    private CPUvCPUThread cpuVScpu;
    int playerOnMove;
    public int prevMove = MainFrame.maxHeaps;

    /**
     * GamePanel constructor, called only once on the start of the program
     *
     * @param mainFrame reference to the mainFrame for access to needed values
     * @throws IOException when texture loading fails
     */
    GamePanel(MainFrame mainFrame) throws IOException {
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

    /**
     * Paints heaps and chips to the panel
     *
     * @param g standard inherited paintComponent argument
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.ORANGE);
        g.fillRect(0, MainFrame.height * 3/4,
                mainFrame.heapsCo() * MainFrame.widthPerHeap, MainFrame.height / 4);

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

    /**
     * Paints rings to the panel
     *
     * Received images are images of parts of the chips in front or behind the sticks
     *
     * @param g standard inherited paintComponent argument
     * @param image image to be painted
     * @param transparentImage transparent image to be painted
     */
    private void printRings(Graphics g, BufferedImage image, BufferedImage transparentImage) {
        int selectedColumn = highlightedColumn;
        int selectedRow = highlightedRow;
        if (gameOn && !isMoveValid(selectedColumn, selectedRow, mainFrame.heapStates, prevMove)) {
            selectedColumn = selectedRow = -1;
        }
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            int continueIndex = 0;
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                int x1 = (int) (MainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                int y1 = ringStartPos - (j * ringSpacing);
                if (selectedRow != -1 && selectedColumn == i &&
                        ((!gameOn && j > selectedRow) || (gameOn && j >= selectedRow))) {
                    g.drawImage(transparentImage, x1, y1,
                            (int)(MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                } else {
                    g.drawImage(image, x1, y1,
                            (int) (MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                }
            }
            if (!gameOn && selectedRow >= mainFrame.heapStates[i] && selectedColumn == i) {
                for (int j = continueIndex; j <= selectedRow && j < MainFrame.maxHeaps; j++) {
                    int x1 = (int) (MainFrame.widthPerHeap * (i + horizontalSelectionMargin));
                    int y1 = ringStartPos - (j * ringSpacing);
                    g.drawImage(transparentImage, x1, y1,
                            (int)(MainFrame.widthPerHeap * (1 - 2 * horizontalSelectionMargin)),
                            ringHeight, null);
                }
            }
        }
    }

    /**
     * Detects mouse movement and updates flags for selected chip
     *
     * @param e standard event handler parameter
     */
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

    /**
     * Calculates chip row in column, given y mouse coordinate
     *
     * @param yPos y mouse coordinate
     * @return row number from the bottom
     */
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

    /**
     * Calculates chip column, given x mouse coordinate
     *
     * @param xPos x mouse coordinate
     * @return column number from the left
     */
    private int getColumnNo(int xPos) {
        int columnWidth = MainFrame.widthPerHeap;
        int column = xPos / columnWidth;
        xPos %= columnWidth;
        if (xPos < horizontalSelectionMargin * columnWidth || xPos > (1 - horizontalSelectionMargin) * columnWidth) {
            return -1;
        }
        return column;
    }

    /**
     * Generates and returns AI player given player on the move
     *
     * @param player is first or second plater on the move
     * @param callMakeMove should bot call makeMove method or it will be done by caller
     * @return returns reference to created AI
     */
    AI getAI(int player, boolean callMakeMove) {
        String level = mainFrame.getCPULevel(player);
        if (level.equals(MainFrame.cpuLabels[0])) {
            return new RandomAi(mainFrame, this, callMakeMove);
        } else if (level.equals(MainFrame.cpuLabels[1])) {
            return new Minimax(mainFrame, this, callMakeMove, prevMove, mainFrame.getCPUDepth(player));
        } else if (level.equals(MainFrame.cpuLabels[2])) {
            return new AlphaBeta(mainFrame, this, callMakeMove, prevMove, mainFrame.getCPUDepth(player));
        } else if (level.equals(MainFrame.cpuLabels[3])) {
            return new ProAI(mainFrame, this, callMakeMove, prevMove, mainFrame.getCPUDepth(player));
        } else {
            return null;
        }
    }

    /**
     * Detects mouse click and executes action based on the game state
     *
     * @param e standard event handler parameter
     */
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

    /**
     * Checks if game with the given state is finished
     *
     * @param heapStates state of the heps
     * @param numHeaps number of heaps
     * @return returns whether game is finished
     */
    public boolean isGameFinished(int [] heapStates, int numHeaps) {
        for (int i = 0; i < numHeaps; i++) {
            if (heapStates[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Executes requested move inside the active game
     *
     * @param column column of the selected chip
     * @param row row of the selected chip
     * @return returns whether the move execution was successful
     */
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

    /**
     * Checks if there are any available moves
     *
     * @return whether any valid moves are available
     */
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

    /**
     * Checks if desired move is valid, given the game state
     *
     * @param column column of the selected chip
     * @param row row of the selected chip
     * @param states states of heaps
     * @param prevMove number of heaps
     * @return whether the desired move is valid
     */
    public boolean isMoveValid(int column, int row, int [] states, int prevMove) {
        if (column < 0 || row < 0 || states[column] <= row) {
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
        return states[column] - row <= 2 * prevMove;
    }

    /**
     * Deselects selected chip is mouse exits the game window
     * @param e standard event handler parameter
     */
    public void mouseExited(MouseEvent e) {
        highlightedRow = highlightedColumn = -1;
    }

    /**
     * Resize gamePanel when number of chips is changed
     */
    void resetSize() {
        setPreferredSize(new Dimension(mainFrame.heapsCo() * MainFrame.widthPerHeap, MainFrame.height));
    }

    /**
     * Initializes new game
     *
     * @param gameMode mode of the game to be initialized (player - CPU combination)
     */
    void initGame(GameMode gameMode) {
        this.gameMode = gameMode;
        gameOn = true;
        playerOnMove = 0;
        prevMove = MainFrame.maxHeaps;
        setMessageLabel();

        if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU &&
                gameMode.getPlayerType(1) == GameMode.PlayerType.CPU) {
            cpuVScpu = new CPUvCPUThread(this);
            cpuVScpu.start();
        } else if (gameMode.getPlayerType(0) == GameMode.PlayerType.CPU) {
            AI ai = getAI(playerOnMove, true);
            ai.start();
        }

    }

    /**
     * Updates text inside the message label in the top center of the gamePanel
     */
    private void setMessageLabel() {
        if (playerOnMove == 0) {
            messageLabel.setForeground(Color.green);
        } else {
            messageLabel.setForeground(Color.blue);
        }
        messageLabel.setText("On the move: " + gameMode.getMessage(playerOnMove));
    }

    /**
     * Interrupts game and AI and resets all flags
     */
    void stopGame() {
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

    /**
     * Empty method inherited from a listener
     * @param e standard event handler parameter
     */
    @Override
    public void mouseDragged(MouseEvent e) {}

    /**
     * Empty method inherited from a listener
     * @param e standard event handler parameter
     */
    @Override
    public void mousePressed(MouseEvent e) {}

    /**
     * Empty method inherited from a listener
     * @param e standard event handler parameter
     */
    @Override
    public void mouseReleased(MouseEvent e) {}

    /**
     * Empty method inherited from a listener
     * @param e standard event handler parameter
     */
    @Override
    public void mouseEntered(MouseEvent e) {}
}
