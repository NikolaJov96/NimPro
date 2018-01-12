package etf.nim.jn150081d.gamemodes;

/**
 * GameMode is abstract class representing the mode of the game
 */
public abstract class GameMode {

    /**
     * Player types inside the game (HUMAN / CPU)
     */
    public enum PlayerType { HUMAN, CPU }

    /**
     * Returns the name of player that is on the move
     *
     * @param player is first or second player on the move
     * @return string name
     */
    public abstract String getMessage(int player);

    /**
     * Returns type of player that is on the move
     *
     * @param player is first or second player on the move
     * @return player type
     */
    public abstract PlayerType getPlayerType(int player);

}
