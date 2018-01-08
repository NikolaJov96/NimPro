package main.minimax;

import main.GamePanel;
import main.MainFrame;

public class Minimax extends AI {
    private int prevMove;
    private int depth;

    class MinimaxNode {
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
        int bestMoveColumn = -1;
        int bestMoveRow = -1;
        float bestMoveValue = -1;

        // iterate through first possible moves
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                    MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                    newNode.state[i] = j;
                    float move = iteration(newNode, depth - 1, false);
                    if (move > bestMoveValue) {
                        bestMoveColumn = i;
                        bestMoveRow = j;
                        bestMoveValue = move;
                    }
                }
            }
        }

        moveEnd();

        if (callMakeMove) {
            gamePanel.makeMove(bestMoveColumn, bestMoveRow);
        } else {
            selectedColumn = bestMoveColumn;
            selectedRow = bestMoveRow;
        }
    }

    private float iteration(MinimaxNode node, int depth, boolean maxPlayer) {
        int asd = mainFrame.heapsCo();
        if (gamePanel.isGameFinished(node.state, asd)) {
            if (maxPlayer) {
                return 0;
            } else {
                return 1;
            }
        }
        if (depth == 0) {
            // return heuristic value
            return 0.5f;
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
                        if (value > bestValue) {
                            bestValue = value;
                        }
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
                        float value = iteration(newNode, depth - 1, true);
                        if (value < bestValue) {
                            bestValue = value;
                        }
                    }
                }
            }
            return bestValue;
        }
    }
}
