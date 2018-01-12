package etf.nim.jn150081d.minimax;

import etf.nim.jn150081d.GamePanel;
import etf.nim.jn150081d.MainFrame;

import java.util.ArrayList;

/**
 * Minimax is most basic implementation of minimax AI algorithm
 */
public class Minimax extends AI {
    int prevMove;
    int depth;

    /**
     * MinimaxNode represents node of the search tree (although the tree itself is not generated)
     */
    class MinimaxNode {
        int [] state;
        int prevMove;

        /**
         * MinimaxNode constructor
         *
         * @param state state of the game in that particular node
         * @param prevMove number of chips removed by previous player
         */
        MinimaxNode(int[] state, int prevMove) {
            this.state = state;
            this.prevMove = prevMove;
        }
    }

    /**
     * Minimax constructor
     *
     * @param mainFrame assigned mainFrame
     * @param gamePanel assigned gamePanel
     * @param callMakeMove should this AI call makeMove method or not
     * @param prevMove number of chips removed by previous player
     * @param depth the maximal depth of the search tree
     */
    public Minimax(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove, int prevMove, int depth) {
        super(mainFrame, gamePanel, callMakeMove);
        this.prevMove = prevMove;
        this.depth = depth;
    }

    /**
     * Calculates a score for each possible move and calls makeMove is needed
     */
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

        int id = (int) (Math.random() * bestMoveColumns.size());
        if (callMakeMove) {
            gamePanel.makeMove(bestMoveColumns.get(id), bestMoveRows.get(id));
        } else {
            selectedColumn = bestMoveColumns.get(id);
            selectedRow = bestMoveRows.get(id);
        }
    }

    /**
     * Recursively calculates a score for each possible move until the maximal search depth
     *
     * @param node state in which next move information is needed
     * @param depth current depth
     * @param maxPlayer is the player on the move maximizing or minimizing score
     * @return returns the score of starting state
     */
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
        if (heuristic == 0) {
            ret = 0.8f;
        }

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
                for (int j = 0; j < node.state[i]; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        float value = iteration(newNode, depth - 1, false);
                        if (value == -1) {
                            value = ret;
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
                for (int j = 0; j < node.state[i]; j++) {
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
