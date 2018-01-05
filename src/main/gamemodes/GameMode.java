package main.gamemodes;

public abstract class GameMode {
    public enum PlayerType { HUMAN, CPU };

    public abstract String getMessage(int player);
    public abstract PlayerType getPlayerType(int player);

}
