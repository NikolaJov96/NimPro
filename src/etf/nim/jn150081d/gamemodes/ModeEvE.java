package etf.nim.jn150081d.gamemodes;

/**
 * ModeEvE represents CPU vs CPU game mode (E - environment)
 */
public class ModeEvE extends GameMode {

    /**
     * Returns the name of player that is on the move
     *
     * @param player is first or second player on the move
     * @return "CPU 1" or "CPU 2"
     */
    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "CPU 1";
        }
        return "CPU 2";
    }

    /**
     * Returns type of player that is on the move
     *
     * @param player is first or second player on the move
     * @return always CPU
     */
    @Override
    public PlayerType getPlayerType(int player) {
        return PlayerType.CPU;
    }
}
