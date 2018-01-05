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
    private static final String radioButton1Text = "Player1 vs Player2";
    private static final String radioButton2Text = "Player vs CPU";
    private static final String radioButton3Text = "CPU vs Player";
    private static final String radioButton4Text = "CPU1 vs CPU2";
    private static final String randomSetupText = "RANDOM";
    private static final String startGameText = "START";
    private static final String stopGameText = "STOP";

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private Box menuBox = Box.createVerticalBox();

    public JRadioButton [] playerModes = new JRadioButton [4];
    {
        playerModes[0] = new JRadioButton(radioButton1Text);
        playerModes[1] = new JRadioButton(radioButton2Text);
        playerModes[2] = new JRadioButton(radioButton3Text);
        playerModes[3] = new JRadioButton(radioButton4Text);
    }

    private JSpinner heaps = new JSpinner(new SpinnerNumberModel(3, 1, maxHeaps, 1));

    private JButton randomSetup = new JButton(randomSetupText);
    private JButton toggleGame = new JButton(startGameText);

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
        buttonGroup.add(playerModes[0]);
        menuBox.add(playerModes[0]);
        buttonGroup.add(playerModes[1]);
        menuBox.add(playerModes[1]);
        buttonGroup.add(playerModes[2]);
        menuBox.add(playerModes[2]);
        buttonGroup.add(playerModes[3]);
        menuBox.add(playerModes[3]);

        menuBox.add(new JLabel("Chose number of heaps:"));
        heaps.setValue(3);
        heaps.addChangeListener(this);
        menuBox.add(heaps);

        randomSetup.addActionListener(e -> randomSetupToggle());
        menuBox.add(randomSetup);

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
            toggleGame.setText(startGameText);
        } else {
            // setup game UI
            mode = Mode.GAME;
            for (JRadioButton rButton : playerModes) {
                rButton.setEnabled(false);
            }
            heaps.setEnabled(false);
            randomSetup.setEnabled(false);
            toggleGame.setText(stopGameText);
        }
    }

    private void setSize() {
        pack();
    }

    public int heapsCo() {
        return (int) heaps.getValue();
    }

    public static void main(String[] args) {
        try { MainFrame mainFrame = new MainFrame(); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
