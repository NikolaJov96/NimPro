package etf.nim.jn150081d.minimax;

import etf.nim.jn150081d.GamePanel;
import etf.nim.jn150081d.MainFrame;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ProAI is tweaked AlphaBeta for stronger AI
 */
public class ProAI extends Minimax {

    /**
     * Entry struct for hash map of recorded states
     */
    public class StateEntry {
        int [] columnPerLastMove = new int [mainFrame.getMaxHeaps() / 2 + 1];
        int [] rowPerLastMove = new int [mainFrame.getMaxHeaps() / 2 + 1];
        float [] scorePerLastMove = new float [mainFrame.getMaxHeaps() / 2 + 1];
        StateEntry() {
            for (int i = 0; i < mainFrame.getMaxHeaps() / 2 + 1; i++) {
                columnPerLastMove[i] =  -1;
                rowPerLastMove[i] = -1;
                scorePerLastMove[i] = -1.0f;
            }
        }
    }
    private static HashMap<String, StateEntry> hashedStates = new HashMap<>();

    /**
     * ProAI constructor
     *
     * @param mainFrame assigned mainFrame
     * @param gamePanel assigned gamePanel
     * @param callMakeMove should this AI call makeMove method or not
     * @param prevMove number of chips removed by previous player
     * @param depth the maximal depth of the search tree
     */
    public ProAI(MainFrame mainFrame, GamePanel gamePanel, boolean callMakeMove, int prevMove, int depth) {
        super(mainFrame, gamePanel, callMakeMove, prevMove, depth);
    }

    /**
     * Returns the array of indexes of values in the state array that form a sorted array in descending order
     * @param state initial state
     * @param size length of relevant array entries
     * @return array of indexes
     */
    private int [] indexState(int[] state, int size) {
        int [] index = new int [size];
        ArrayList<Integer> availableIndex = new ArrayList<>();
        for (int i = 0; i < size; i++) availableIndex.add(i);

        for (int i = 0; i < size; i++) {
            int maxInd = -1;
            for (int j = 0; j < availableIndex.size(); j++) {
                if (maxInd == -1 || state[availableIndex.get(j)] > state[availableIndex.get(maxInd)]) {
                    maxInd = j;
                }
            }
            Integer rem = availableIndex.get(maxInd);
            availableIndex.remove(rem);
            index[i] = rem;
        }
        return index;
    }

    /**
     * Takes initial state and array of indexes and returns a string representing sorted state array
     * @param state initial state
     * @param index index array
     * @return generated string
     */
    private String normState(int[] state, int[] index) {
        StringBuilder norm = new StringBuilder();
        for (int ind : index) {
            norm.append(Integer.toHexString(state[ind]));
        }
        while (norm.length() < mainFrame.getMaxHeaps()) {
            norm.append("0");
        }
        return norm.toString();
    }


    /**
     * Checks if initial state is already visited, if not
     * calculates a score for each possible move,
     * calls makeMove is needed
     */
    @Override
    public void run() {
        moveStart();

        ArrayList<Integer> bestMoveColumns = new ArrayList<>();
        ArrayList<Integer> bestMoveRows = new ArrayList<>();

        int [] index = indexState(mainFrame.heapStates, mainFrame.heapsCo());
        String norm = normState(mainFrame.heapStates, index);
        StateEntry selState = hashedStates.get(norm);
        int prevMoveAsIndex = (prevMove <= mainFrame.getMaxHeaps() / 2) ? (prevMove - 1) : (mainFrame.getMaxHeaps() / 2);
        float bestMoveValue = -1;

        if (selState != null && selState.columnPerLastMove[prevMoveAsIndex] != -1) {
            bestMoveColumns.add(index[selState.columnPerLastMove[prevMoveAsIndex]]);
            bestMoveRows.add(selState.rowPerLastMove[prevMoveAsIndex]);
        } else {
            MinimaxNode node = new MinimaxNode(mainFrame.heapStates.clone(), prevMove);

            // iterate through first possible moves
            for (int i = 0; i < mainFrame.heapsCo(); i++) {
                for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        float move = iteration(newNode, depth - 1, 0, 1, false);
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
        }

        moveEnd();

        int id = (int) (Math.random() * bestMoveColumns.size());
        selectedColumn = bestMoveColumns.get(id);
        selectedRow = bestMoveRows.get(id);

        // record new state
        if (selState == null || selState.columnPerLastMove[prevMoveAsIndex] == -1) {
            if (selState == null) {
                selState = new StateEntry();
                hashedStates.put(norm, selState);
            }
            if (bestMoveValue == 0 || bestMoveValue == 1) {
                int columnInd = 0;
                while (index[columnInd] != selectedColumn) columnInd++;
                selState.columnPerLastMove[prevMoveAsIndex] = columnInd;
                selState.rowPerLastMove[prevMoveAsIndex] = selectedRow;
                selState.scorePerLastMove[prevMoveAsIndex] = bestMoveValue;
            }
        }

        if (callMakeMove) {
            gamePanel.makeMove(bestMoveColumns.get(id), bestMoveRows.get(id));
        }
    }


    /**
     * Recursively calculates a score for some of the moves until the maximal search depth, or until
     * it comes across a state that has already been visited, and is able to use recorded data
     *
     * Some moves can be proved not to be able to produce better results than already achieved,
     * so they can be skipped and we can still be sure that we will get to the best possible move
     *
     * Records all visited states and remembers potential move that guarantees victory, so that data
     * can later be used to enhance play strength and performance
     *
     * @param node state in which next move information is needed
     * @param depth current depth
     * @param alpha current minimal guaranteed score that can be achieved from the starting state
     * @param beta current maximal possible score that can be achieved from the stating state
     * @param maxPlayer is the player on the move maximizing or minimizing score
     * @return returns the score of starting state
     */
    private float iteration(MinimaxNode node, int depth, float alpha, float beta, boolean maxPlayer) {
        if (gamePanel.isGameFinished(node.state, mainFrame.heapsCo())) {
            if (maxPlayer) return 0;
            else return 1;
        }

        int [] index = indexState(node.state, mainFrame.heapsCo());
        String norm = normState(node.state, index);
        StateEntry selState = hashedStates.get(norm);
        int prevMoveAsIndex = (node.prevMove <= mainFrame.getMaxHeaps() / 2) ? (node.prevMove - 1) : (mainFrame.getMaxHeaps() / 2);
        float bestMoveValue;
        int bestColumn = -1;
        int bestRow = -1;

        if (selState != null && selState.columnPerLastMove[prevMoveAsIndex] != -1) {
            if (maxPlayer) return selState.scorePerLastMove[prevMoveAsIndex];
            else return 1 - selState.scorePerLastMove[prevMoveAsIndex];
        }

        int heuristic = node.state[0];
        for (int i = 1; i < mainFrame.heapsCo(); i++) heuristic ^= node.state[i]; // xor
        float ret = 0.5f;
        if (heuristic == 0) ret = 0.8f;

        if (depth == 0) {
            // return heuristic value
            if (maxPlayer) return ret;
            else return -1; // flag to use parents heuristic value
        }

        if (maxPlayer) {
            // find best next move
            bestMoveValue = 0;
            boolean terminated = false;
            for (int i = 0; i < mainFrame.heapsCo() && !terminated; i++) {
                for (int j = 0; j < node.state[i] && !terminated; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        float value = iteration(newNode, depth - 1, alpha, beta, false);
                        if (value == -1) value = ret;
                        //bestMoveValue = Math.max(bestMoveValue, value);
                        if (value >= bestMoveValue) {
                            bestMoveValue = value;
                            bestColumn = i;
                            bestRow = j;
                        }
                        alpha = Math.max(alpha, bestMoveValue);
                        if (beta <= alpha) terminated = true;
                    }
                }
            }
        } else {
            // find worst next move
            bestMoveValue = 1;
            boolean terminated = false;
            for (int i = 0; i < mainFrame.heapsCo() && !terminated; i++) {
                for (int j = 0; j < node.state[i] && !terminated; j++) {
                    if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                        MinimaxNode newNode = new MinimaxNode(node.state.clone(), node.state[i] - j);
                        newNode.state[i] = j;
                        float value = iteration(newNode, depth - 1, alpha, beta, true);
                        //bestMoveValue = Math.min(bestMoveValue, value);
                        if (value <= bestMoveValue) {
                            bestMoveValue = value;
                            bestColumn = i;
                            bestRow = j;
                        }
                        beta = Math.min(beta, bestMoveValue);
                        if (beta <= alpha) terminated = true;
                    }
                }
            }
        }

        // record new state
        if (selState == null || selState.columnPerLastMove[prevMoveAsIndex] == -1) {
            if (selState == null) {
                selState = new StateEntry();
                hashedStates.put(norm, selState);
            }
            if (bestMoveValue == 0 || bestMoveValue == 1) {
                int columnInd = 0;
                while (columnInd < index.length && index[columnInd] != bestColumn) columnInd++;
                selState.columnPerLastMove[prevMoveAsIndex] = columnInd;
                selState.rowPerLastMove[prevMoveAsIndex] = bestRow;
                if (maxPlayer) selState.scorePerLastMove[prevMoveAsIndex] = bestMoveValue;
                else selState.scorePerLastMove[prevMoveAsIndex] = 1 - bestMoveValue;
            }
        }
        return bestMoveValue;
    }
}
