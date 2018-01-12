package etf.nim.jn150081d.gamemodes;

/**
 * ModePvE represents PLayer vs CPU game mode (P - player, E - environment)
 */
public class ModePvE extends GameMode {

    /**
     * Returns the name of player that is on the move
     *
     * @param player is first or second player on the move
     * @return "Player" or "CPU"
     */
    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "Player";
        }
        return "CPU";
    }

    /**
     * Returns type of player that is on the move
     *
     * @param player is first or second player on the move
     * @return HUMAN or CPU
     */
    @Override
    public PlayerType getPlayerType(int player) {
        if (player == 0) {
            return PlayerType.HUMAN;
        }
        return PlayerType.CPU;
    }
}
