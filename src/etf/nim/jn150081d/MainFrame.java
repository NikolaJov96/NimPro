package etf.nim.jn150081d;

import etf.nim.jn150081d.gamemodes.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * RepaintThread is started on the beginning of the program and is used to repaint screen
 */
class RepaintThread extends Thread {
    private static final int sleepTime = 1000 / 30;

    private MainFrame mainFrame;

    /**
     * RepaintThread constructor
     *
     * @param mainFrame Reference to the main application window
     */
    RepaintThread(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Runs during application execution and repaints main frame
     */
    @Override
    public void run() {
        while (mainFrame.running) {
            try {
                mainFrame.repaint();
                sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * MainFrame is encapsulation of GUI features of the application
 */
public class MainFrame extends JFrame implements ChangeListener {
    static final int widthPerHeap = 100;
    static final int height = 400;
    static final int maxHeaps = 10;

    private static final String windowName = "NimPro";
    private static final String [] radioButtonText =
            {"Player1 vs Player2", "Player vs CPU", "CPU vs Player", "CPU1 vs CPU2" };
    private static final String randomSetupText = "RANDOM";
    private static final String startGameText = "START";
    private static final String stopGameText = "STOP";
    static final String [] cpuLabels = { "random", "minimax", "alpha-beta", "pro" };

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel menuPanel = new JPanel();

    private JRadioButton [] playerModes = new JRadioButton [4];
    {
        for (int i = 0; i < playerModes.length; i++) {
            playerModes[i] = new JRadioButton(radioButtonText[i]);
        }
    }

    private JSpinner heaps = new JSpinner(new SpinnerNumberModel(3, 1, maxHeaps, 1));

    private JButton randomSetup = new JButton(randomSetupText);
    private JButton toggleGame = new JButton(startGameText);

    private JComboBox[] cpuChoice = new JComboBox[2];
    private JSpinner [] cpuDepth = new JSpinner[2];
    {
        cpuChoice[0] = new JComboBox<>(cpuLabels);
        cpuChoice[1] = new JComboBox<>(cpuLabels);
        cpuDepth[0] = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        cpuDepth[1] = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    }

    private GamePanel gamePanel = new GamePanel(this);

    int [] initHeapStates = new int [maxHeaps];
    public volatile int [] heapStates = new int [maxHeaps];

    public enum Mode { MENU, GAME }
    private Mode mode = Mode.MENU;

    boolean running = true;

    /**
     * MainFrame constructor initializes GUI parameters
     *
     * @throws IOException when gamePanel initialization fails
     */
    private MainFrame() throws IOException {
        super(windowName);
        initWindow();
        randomSetupToggle();
        new RepaintThread(this).start();
    }

    /**
     * Configures and adds helper panel to the main menu panel
     *
     * @param panel to be added to the main menu panel
     */
    private void addPanel(JPanel panel) {
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(panel.getPreferredSize());
        menuPanel.add(panel);
    }

    /**
     * Adds GUI components to the main frame
     */
    private void initWindow() {
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("  Chose player mode:"));
            playerModes[0].setSelected(true);
            ButtonGroup buttonGroup = new ButtonGroup();
            for (JRadioButton playerMode : playerModes) {
                playerMode.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(playerMode);
                buttonGroup.add(playerMode);
                playerMode.addActionListener(e -> updateCPUSettings());
            }
            addPanel(panel);
        }

        {
            JPanel panel = new JPanel();
            panel.add(new JLabel("Chose number of heaps:"));
            heaps.setValue(3);
            heaps.addChangeListener(this);
            panel.add(heaps);
            addPanel(panel);
        }

        {
            JPanel panel = new JPanel();
            panel.add(new JLabel("Heap states:"));
            randomSetup.setAlignmentX(Component.LEFT_ALIGNMENT);
            randomSetup.addActionListener(e -> randomSetupToggle());
            panel.add(randomSetup);
            addPanel(panel);
        }

        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("  CPU1 level and depth:"));
            {
                JPanel inPanel = new JPanel();
                inPanel.add(cpuChoice[0]);
                inPanel.add(cpuDepth[0]);
                inPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(inPanel);
            }
            panel.add(new JLabel("  CPU2 level and depth:"));
            {
                JPanel inPanel = new JPanel();
                inPanel.add(cpuChoice[1]);
                inPanel.add(cpuDepth[1]);
                inPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(inPanel);
            }
            addPanel(panel);
        }

        {
            JPanel panel = new JPanel(new GridBagLayout());
            toggleGame.addActionListener(e -> toggleGameToggle());
            Dimension dim = toggleGame.getPreferredSize();
            toggleGame.setPreferredSize(new Dimension((int)(dim.width * 1.5), (int)(dim.height * 1.5)));
            panel.add(toggleGame);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuPanel.add(panel);
        }

        mainPanel.add(menuPanel, BorderLayout.WEST);

        mainPanel.add(gamePanel, BorderLayout.CENTER);

        add(mainPanel);

        pack();
        setResizable(false);
        setVisible(true);

        initMode(mode);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                running = false;
                dispose();
            }
        });
    }

    /**
     * Resize frame and game panel when number of heaps is changed
     *
     * @param e standard event handler parameter
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        gamePanel.resetSize();
        pack();
    }

    /**
     * Random button callback method
     */
    private void randomSetupToggle() {
        if (mode == Mode.MENU) {
            // random heap setup
            ArrayList<Integer> valueSet = new ArrayList<>(maxHeaps);
            for (int i = 1; i <= maxHeaps; i++) {
                valueSet.add(i);
            }
            for (int i = 0; i < maxHeaps; i++)
            {
                int randomIndex = valueSet.size();
                while (randomIndex > valueSet.size() - 1) {
                    randomIndex = (int)(Math.random() * valueSet.size());
                }
                heapStates[i] = initHeapStates[i] = valueSet.get(randomIndex);
                valueSet.remove(randomIndex);
            }
        }
    }

    /**
     * Start/stop button callback method
     */
    void toggleGameToggle() {
        if (mode == Mode.MENU) {
            // check heap setup
            for (int i = 0; i < heapsCo(); i++) {
                for (int j = 0; j < i; j++) {
                    if (initHeapStates[i] == initHeapStates[j]) {
                        JOptionPane.showMessageDialog(null, "Invalid initial heap states!");
                        return;
                    }
                }
            }
            // setup heap states
            System.arraycopy(initHeapStates, 0, heapStates, 0, heapsCo());
            // start the game
            if (playerModes[0].isSelected()) { gamePanel.initGame(new ModePvP()); }
            else if (playerModes[1].isSelected()) { gamePanel.initGame(new ModePvE()); }
            else if (playerModes[2].isSelected()) { gamePanel.initGame(new ModeEvP()); }
            else if (playerModes[3].isSelected()) { gamePanel.initGame(new ModeEvE()); }
            initMode(Mode.GAME);
        } else {
            // restore heap states
            System.arraycopy(initHeapStates, 0, heapStates, 0, heapsCo());
            // stop the game
            gamePanel.stopGame();
            initMode(Mode.MENU);
        }
    }

    /**
     * Initializes (changes) application mode
     *
     * @param newMode mode to be initialized (MENU / GAME)
     */
    private void initMode(Mode newMode) {
        if (newMode == Mode.MENU) {
            // setup menu UI
            mode = Mode.MENU;
            for (JRadioButton rButton : playerModes) {
                rButton.setEnabled(true);
            }
            heaps.setEnabled(true);
            randomSetup.setEnabled(true);
            updateCPUSettings();
            toggleGame.setText(startGameText);
        } else {
            // setup game UI
            mode = Mode.GAME;
            for (JRadioButton rButton : playerModes) {
                rButton.setEnabled(false);
            }
            heaps.setEnabled(false);
            randomSetup.setEnabled(false);
            cpuChoice[0].setEnabled(false);
            cpuDepth[0].setEnabled(false);
            cpuChoice[1].setEnabled(false);
            cpuDepth[1].setEnabled(false);
            toggleGame.setText(stopGameText);
        }
    }

    /**
     * JRadioButton clicked callback method, enables and disables parts of the GUI
     */
    private void updateCPUSettings() {
        cpuChoice[0].setEnabled(playerModes[2].isSelected() || playerModes[3].isSelected());
        cpuDepth[0].setEnabled(playerModes[2].isSelected() || playerModes[3].isSelected());
        cpuChoice[1].setEnabled(playerModes[1].isSelected() || playerModes[3].isSelected());
        cpuDepth[1].setEnabled(playerModes[1].isSelected() || playerModes[3].isSelected());
    }

    /**
     * Number of heaps getter
     *
     * @return number of heaps
     */
    public int heapsCo() {
        return (int) heaps.getValue();
    }

    /**
     * Getter for level of the CPU player on the moge
     *
     * @param player is first or second player on the move
     * @return string label of the CPU player level
     */
    String getCPULevel(int player) {
        return cpuLabels[cpuChoice[player].getSelectedIndex()];
    }

    /**
     * Getter for depth of the search tree for CPU player on the move
     *
     * @param player is first or second player on the move
     * @return depth of the search tree
     */
    int getCPUDepth(int player) {
        return (int) cpuDepth[player].getValue();
    }

    /**
     * Main application method
     *
     * @param args system args
     */
    public static void main(String[] args) {
        try { new MainFrame(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
