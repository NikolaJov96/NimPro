package etf.nim.jn150081d.gamemodes;

public class ModePvE extends GameMode {

    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "Player";
        }
        return "CPU";
    }

    @Override
    public PlayerType getPlayerType(int player) {
        if (player == 0) {
            return PlayerType.HUMAN;
        }
        return PlayerType.CPU;
    }
}