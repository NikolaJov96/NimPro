package main.minimax;

import main.GamePanel;
import main.MainFrame;

import java.util.ArrayList;

public class Minimax extends AI {
    private int prevMove;

    class MinimaxNode {
        public int [] state;
        public int prevMove;

        public MinimaxNode(int [] state, int prevMove) {
            this.state = state;
            this.prevMove = prevMove;
        }
    }

    public Minimax(MainFrame mainFrame, GamePanel gamePanel, int prevMove) {
        super(mainFrame, gamePanel);
        this.prevMove = prevMove;
    }

    @Override
    public void run() {
        MinimaxNode node = new MinimaxNode(mainFrame.heapStates.clone(), prevMove);
        iteration(node);
        // do stuff

        //gamePanel.makeMove();
    }

    private void iteration(MinimaxNode node) {
        // find all possible moves
        ArrayList<MinimaxNode> nodeList = new ArrayList<>();
        for (int i = 0; i < mainFrame.heapsCo(); i++) {
            for (int j = 0; j < mainFrame.heapStates[i]; j++) {
                if (gamePanel.isMoveValid(i, j, node.state, node.prevMove)) {
                    MinimaxNode newNode = new MinimaxNode( node.state.clone(), node.state[i] - j);
                    newNode.state[i] = j;
                    nodeList.add(newNode);
                }
            }
        }

        // play all possible opponent moves

        // make new iteration calls
    }
}
