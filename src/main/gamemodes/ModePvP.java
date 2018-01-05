package main.gamemodes;

import main.GamePanel;

public class ModePvP extends GameMode {

    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "Player 1";
        }
        return "Player 2";
    }

    @Override
    public PlayerType getPlayerType(int player) {
        return PlayerType.HUMAN;
    }
}
