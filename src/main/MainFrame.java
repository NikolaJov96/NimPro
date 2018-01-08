package main;

import main.gamemodes.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

class RepaintThread extends Thread {
    private static final int sleepTime = 1000 / 30;

    private MainFrame mainFrame;

    RepaintThread(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

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

public class MainFrame extends JFrame implements ChangeListener {
    private static final int menuWidth = 200;
    public static final int widthPerHeap = 100;
    public static final int height = 400;
    public static final int maxHeaps = 10;

    private static final String windowName = "NimPro";
    private static final String [] radioButtonText =
            {"Player1 vs Player2", "Player vs CPU", "CPU vs Player", "CPU1 vs CPU2" };
    private static final String randomSetupText = "RANDOM";
    private static final String startGameText = "START";
    private static final String stopGameText = "STOP";
    public static final String [] cpuLabels = { "random", "minimax", "alpha-beta", "pro" };

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private Box menuBox = Box.createVerticalBox();

    public JRadioButton [] playerModes = new JRadioButton [4];
    {
        for (int i = 0; i < playerModes.length; i++) {
            playerModes[i] = new JRadioButton(radioButtonText[i]);
        }
    }

    private JSpinner heaps = new JSpinner(new SpinnerNumberModel(3, 1, maxHeaps, 1));

    private JButton randomSetup = new JButton(randomSetupText);
    private JButton toggleGame = new JButton(startGameText);

    private JComboBox<String> [] cpuChoice = new JComboBox[2];
    private JSpinner [] cpuDepth = new JSpinner[2];
    {
        cpuChoice[0] = new JComboBox<>(cpuLabels);
        cpuChoice[1] = new JComboBox<>(cpuLabels);
        cpuDepth[0] = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        cpuDepth[1] = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    }

    private GamePanel gamePanel = new GamePanel(this);

    public int [] initHeapStates = new int [maxHeaps];
    public volatile int [] heapStates = new int [maxHeaps];

    public enum Mode { MENU, GAME };
    private Mode mode = Mode.MENU;

    public boolean running = true;
    private RepaintThread repaintThread = new RepaintThread(this);

    public MainFrame() throws IOException {
        super(windowName);
        initWindow();
        randomSetupToggle();
        repaintThread.start();
    }

    private void initWindow() {
        menuBox.add(new JLabel("Chose player mode:"));
        playerModes[0].setSelected(true);
        ButtonGroup buttonGroup = new ButtonGroup();
        for (int i = 0; i < playerModes.length; i++) {
            menuBox.add(playerModes[i]);
            buttonGroup.add(playerModes[i]);
            playerModes[i].addActionListener(e -> updateCPUSettings());
        }

        {
            JPanel panel = new JPanel();
            panel.add(new JLabel("Chose number of heaps:"));
            heaps.setValue(3);
            heaps.addChangeListener(this);
            panel.add(heaps);
            menuBox.add(panel);
        }

        randomSetup.addActionListener(e -> randomSetupToggle());
        menuBox.add(randomSetup);

        menuBox.add(new JLabel("CPU1 level and depth:"));
        {
            JPanel panel = new JPanel();
            panel.add(cpuChoice[0]);
            panel.add(cpuDepth[0]);
            menuBox.add(panel);
        }
        menuBox.add(new JLabel("CPU2 level and depth:"));
        {
            JPanel panel = new JPanel();
            panel.add(cpuChoice[1]);
            panel.add(cpuDepth[1]);
            menuBox.add(panel);
        }

        toggleGame.addActionListener(e -> toggleGameToggle());
        menuBox.add(toggleGame);

        JPanel boxPanel = new JPanel();
        boxPanel.setPreferredSize(new Dimension(menuWidth, height));
        boxPanel.add(menuBox);
        mainPanel.add(boxPanel, BorderLayout.WEST);

        mainPanel.add(gamePanel, BorderLayout.CENTER);

        add(mainPanel);

        setSize();
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

    @Override
    public void stateChanged(ChangeEvent e) {
        gamePanel.resetSize();
        setSize();
    }

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

    public void toggleGameToggle() {
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
            // start the game
            for (int i = 0; i < heapsCo(); i++) {
                heapStates[i] = initHeapStates[i];
            }
            if (playerModes[0].isSelected()) { gamePanel.initGame(new ModePvP()); }
            else if (playerModes[1].isSelected()) { gamePanel.initGame(new ModePvE()); }
            else if (playerModes[2].isSelected()) { gamePanel.initGame(new ModeEvP()); }
            else if (playerModes[3].isSelected()) { gamePanel.initGame(new ModeEvE()); }
            initMode(Mode.GAME);
        } else {
            // restore heap states
            for (int i = 0; i < heapsCo(); i++) {
                heapStates[i] = initHeapStates[i];
            }
            // stop the game
            gamePanel.stopGame();
            initMode(Mode.MENU);
        }
    }

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

    private void updateCPUSettings() {
        cpuChoice[0].setEnabled(playerModes[2].isSelected() || playerModes[3].isSelected());
        cpuDepth[0].setEnabled(playerModes[2].isSelected() || playerModes[3].isSelected());
        cpuChoice[1].setEnabled(playerModes[1].isSelected() || playerModes[3].isSelected());
        cpuDepth[1].setEnabled(playerModes[1].isSelected() || playerModes[3].isSelected());
    }

    private void setSize() {
        pack();
    }

    public int heapsCo() {
        return (int) heaps.getValue();
    }

    public String getCPULevel(int player) {
        return cpuLabels[cpuChoice[player].getSelectedIndex()];
    }

    public int getCPUDepth(int player) {
        return (int) cpuDepth[player].getValue();
    }

    public static void main(String[] args) {
        try { MainFrame mainFrame = new MainFrame(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
