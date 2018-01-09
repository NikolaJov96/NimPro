package main.minimax;

import main.GamePanel;
import main.MainFrame;

import java.util.ArrayList;

public class Minimax extends AI {
    protected int prevMove;
    protected int depth;

    protected class MinimaxNode {
        public int [] state;
        public int prevMove;

        public MinimaxNode(int [] state, int prevMove) {
            this.state = state;
            this.prevMove = prevMove;
        }
    }

    public Minimax(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove, int prevMove, int depth) {
        super(mainFrame, gamePanel, callMakeMove);
        this.prevMove = prevMove;
        this.depth = depth;
    }

    @Override
    public void run() {
        moveStart();

        MinimaxNode node = new MinimaxNode(mainFrame.heapStates.clone(), prevMove);
        ArrayList<Integer> bestMoveColumns = new ArrayList<>();
        ArrayList<Integer> bestMoveRows = new ArrayList<>();
        float bestMoveValue = -1;

        // iterate through first possible moves
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                    MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                    newNode.state[i] = j;
                    float move = iteration(newNode, depth - 1, false);
                    if (move > bestMoveValue) {
                        bestMoveColumns.clear();
                        bestMoveRows.clear();
                    }
                    if (move >= bestMoveValue) {
                        bestMoveColumns.add(i);
                        bestMoveRows.add(j);
                        bestMoveValue = move;
                    }
                }
            }
        }

        moveEnd();

        if (bestMoveValue == 1) {
            System.out.println("win");
        }
        if (bestMoveValue == 0) {
            System.out.println("loss");
        }

        int id = (int) (Math.random() * bestMoveColumns.size());
        if (callMakeMove) {
            gamePanel.makeMove(bestMoveColumns.get(id), bestMoveRows.get(id));
        } else {
            selectedColumn = bestMoveColumns.get(id);
            selectedRow = bestMoveRows.get(id);
        }
    }

    private float iteration(MinimaxNode node, int depth, boolean maxPlayer) {
        if (gamePanel.isGameFinished(node.state, mainFrame.heapsCo())) {
            if (maxPlayer) {
                return 0;
            } else {
                return 1;
            }
        }

        int heuristic = node.state[0];
        for (int i = 1; i < mainFrame.heapsCo(); i++) {
            heuristic ^= node.state[i]; // xor
        }
        float ret = 0.5f;
//        if (heuristic == 0) {
//            ret = 0.8f;
//        }

        if (depth == 0) {
            // return heuristic value
            if (maxPlayer) {
                return ret;
            } else {
                // flag to use parents heuristic value
                return -1;
            }
        }

        if (maxPlayer) {
            // find best next move
            float bestValue = 0;
            for (int i = 0; i < mainFrame.heapsCo(); i++) {
                for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        float value = iteration(newNode, depth - 1, false);
                        if (value == -1) {
                            value = 0.5f;//ret;
                        }
                        bestValue = Math.max(bestValue, value);
                    }
                }
            }
            return bestValue;
        } else {
            // find worst next move
            float bestValue = 1;
            for (int i = 0; i < mainFrame.heapsCo(); i++) {
                for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        bestValue = Math.min(bestValue, iteration(newNode, depth - 1, true));
                    }
                }
            }
            return bestValue;
        }
    }
}
