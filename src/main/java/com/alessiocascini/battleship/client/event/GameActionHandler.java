package com.alessiocascini.battleship.client.event;

/**
 * Interface defining the actions required to update the game state during the battle phase. It
 * facilitates communication between the network logic (event listeners) and the user interface
 * (GameUI).
 *
 * @author Alessio Cascini
 */
public interface GameActionHandler {

  /**
   * Displays a game-related message to the player, such as turn notifications or win/loss alerts.
   *
   * @param message The text content to be shown in the UI
   */
  void showMessage(String message);

  /**
   * Processes and renders the result of a move on the game grids.
   *
   * @param isPlayerMove True if the move was initiated by the local player, false if it was an
   *     incoming move from the opponent
   * @param row The row index of the targeted cell
   * @param col The column index of the targeted cell
   * @param result A 2D array containing the outcome: - Empty: Miss - One cell: Hit - Multiple
   *     cells: Ship sunk - First cell as [-1, -1]: Game over sentinel
   */
  void processMove(boolean isPlayerMove, int row, int col, int[][] result);
}
