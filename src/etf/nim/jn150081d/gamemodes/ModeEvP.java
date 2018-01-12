package etf.nim.jn150081d.gamemodes;

/**
 * ModeEvP represents CPU vs Player game mode (E - environment, P - player)
 */
public class ModeEvP extends GameMode {

    /**
     * Returns the name of player that is on the move
     *
     * @param player is first or second player on the move
     * @return "CPU" or "Player"
     */
    @Override
    public String getMessage(int player) {
        if (player == 0) {
            return "CPU";
        }
        return "Player";
    }

    /**
     * Returns type of player that is on the move
     *
     * @param player is first or second player on the move
     * @return CPU or HUMAN
     */
    @Override
    public PlayerType getPlayerType(int player) {
        if (player == 0) {
            return PlayerType.CPU;
        }
        return PlayerType.HUMAN;
    }
}
