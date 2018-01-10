package etf.nim.jn150081d.gamemodes;

public class ModeEvE extends GameMode {

    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "CPU 1";
        }
        return "CPU 2";
    }

    @Override
    public PlayerType getPlayerType(int player) {
        return PlayerType.CPU;
    }
}