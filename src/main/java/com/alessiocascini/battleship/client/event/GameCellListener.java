package com.alessiocascini.battleship.client.event;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

/**
 * Listener for the gameplay grid cells. This class manages the network communication for each move,
 * handling the turn-based logic and ensuring the UI remains responsive by running network tasks in
 * a separate thread.
 *
 * @author Alessio Cascini
 */
public class GameCellListener implements ActionListener {
  /** Flag to manage the specific synchronization required during the very first turn */
  private static boolean isFirstTurn = true;

  private final GameActionHandler handler;
  private final boolean isFirstPlayer;
  private final int row, col;

  /**
   * Constructs a listener for a specific coordinate on the opponent's grid.
   *
   * @param handler The UI handler to process move results
   * @param isFirstPlayer Boolean indicating if this client is Player 1
   * @param row The row index of the cell
   * @param col The column index of the cell
   */
  public GameCellListener(GameActionHandler handler, boolean isFirstPlayer, int row, int col) {
    this.handler = handler;
    this.isFirstPlayer = isFirstPlayer;
    this.row = row;
    this.col = col;
  }

  /**
   * Triggered when a cell on the opponent's grid is clicked. Starts a new thread to handle the
   * network transaction so the UI doesn't hang.
   *
   * @param e The action event
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    new Thread(() -> cellClicked(row, col)).start();
  }

  /**
   * Handles the core gameplay network protocol: 1. Connects to the server and verifies turn order.
   * 2. If it's the second player's first turn, it first receives the opponent's opening move. 3.
   * Sends the player's move and processes the result. 4. Waits and receives the opponent's
   * counter-move.
   *
   * @param row The target row
   * @param col The target column
   */
  private void cellClicked(int row, int col) {
    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Identification phase
      out.writeObject(isFirstPlayer);
      boolean isYourTurn = (Boolean) in.readObject();

      if (isYourTurn) {
        // Special case: Player 2 must see Player 1's move before making their own first move
        if (isFirstTurn && !isFirstPlayer) {
          int[] opponentMove = (int[]) in.readObject();
          int[][] opponentResult = (int[][]) in.readObject();
          handler.processMove(false, opponentMove[0], opponentMove[1], opponentResult);

          isFirstTurn = false;
        }

        // Send current move
        out.writeObject(new int[] {row, col});
        int[][] result = (int[][]) in.readObject();
        handler.processMove(true, row, col, result);

        // Wait for and process the opponent's next move
        int[] opponentMove = (int[]) in.readObject();
        int[][] opponentResult = (int[][]) in.readObject();
        handler.processMove(false, opponentMove[0], opponentMove[1], opponentResult);
      } else handler.showMessage("It's not your turn!");
    } catch (Exception e) {
      handler.showMessage("Error communicating with server: " + e.getMessage());
    }
  }
}
