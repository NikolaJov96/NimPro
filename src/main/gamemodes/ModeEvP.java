package main.gamemodes;

import main.GamePanel;

public class ModeEvP extends GameMode {

    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "CPU";
        }
        return "Player";
    }

    @Override
    public PlayerType getPlayerType(int player) {
        if (player == 0) {
            return PlayerType.CPU;
        }
        return PlayerType.HUMAN;
    }
}