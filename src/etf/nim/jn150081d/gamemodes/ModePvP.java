package etf.nim.jn150081d.gamemodes;

/**
 * ModePvP represents PLayer vs Player game mode (P - player)
 */
public class ModePvP extends GameMode {

    /**
     * Returns the name of player that is on the move
     *
     * @param player is first or second player on the move
     * @return "Player 1" or "Player 2"
     */
    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "Player 1";
        }
        return "Player 2";
    }

    /**
     * Returns type of player that is on the move
     *
     * @param player is first or second player on the move
     * @return always HUMAN
     */
    @Override
    public PlayerType getPlayerType(int player) {
        return PlayerType.HUMAN;
    }
}
