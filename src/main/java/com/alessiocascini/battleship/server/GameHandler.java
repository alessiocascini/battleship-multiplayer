package com.alessiocascini.battleship.server;

import java.io.*;
import java.net.Socket;

/**
 * Manages the gameplay logic for an individual turn. This class handles the communication between
 * the two players, tracks whose turn it is, and monitors the number of sunken ships to determine
 * the game outcome. Since multiple instances run in different threads, it uses static fields to
 * maintain a shared game state.
 *
 * @author Alessio Cascini
 */
public class GameHandler implements Runnable {
  /**
   * Tracks the number of ships sunk by each player [Player 2's ships sunk, Player 1's ships sunk]
   */
  private static final int[] sunkenShipsCount = {0, 0};

  /** Flag to track whose turn it is (true for Player 1, false for Player 2) */
  private static boolean isPlayerOneTurn = true;

  /** Flag to handle the special case of the very first turn */
  private static boolean isFirstTurn = true;

  /** Stores the last move made to be sent to the opponent */
  private static int[] move = null;

  /** Stores the result of the last move (hit, miss, or sunk) to be shared */
  private static int[][] result = null;

  /** The socket connection for this specific client */
  private final Socket clientSocket;

  /**
   * Constructs a GameHandler for the connected client.
   *
   * @param clientSocket The socket connection to the player
   */
  public GameHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  /**
   * Main game loop logic for a single turn request: 1. Verifies if it is the player's turn. 2.
   * Processes the move through the Server logic. 3. Updates the win/loss state if a ship is sunk.
   * 4. Waits and notifies the player of the opponent's subsequent move.
   */
  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Identify which player is connecting
      final boolean isPlayerOne = (Boolean) in.readObject();

      // Send turn verification to the client
      out.writeObject(isPlayerOne == isPlayerOneTurn);
      if (isPlayerOne != isPlayerOneTurn) return;

      // Handle the initial state synchronization for Player 2
      if (!isPlayerOne && isFirstTurn) {
        out.writeObject(move);
        out.writeObject(result);

        isFirstTurn = false;
      }

      // Receive the move from the active player
      move = (int[]) in.readObject();
      result = Server.processMove(isPlayerOne, move);

      // If the result indicates a sunken ship (length > 1), increment the counter
      if (result.length > 1) sunkenShipsCount[isPlayerOne ? 1 : 0]++;

      // Send the result back: if all ships are sunk, send the special "game over" message
      if (sunkenShipsCount[isPlayerOne ? 1 : 0] < Server.getShipCount()) out.writeObject(result);
      else out.writeObject(createSunkMessage());

      // Switch turn and wait for the other player to move
      isPlayerOneTurn = !isPlayerOneTurn;

      // Wait loop (polling) until the opponent has completed their turn
      while (isPlayerOne != isPlayerOneTurn) Thread.sleep(100);

      // Send the opponent's move and the current game state to the player
      out.writeObject(move);
      if (sunkenShipsCount[isPlayerOne ? 0 : 1] < Server.getShipCount()) out.writeObject(result);
      else out.writeObject(createSunkMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a special message structure to signal that the game has ended. Adds a sentinel value
   * [-1, -1] at the beginning of the result array.
   *
   * @return A 2D array representing the final sunken ship and the game-over signal
   */
  private int[][] createSunkMessage() {
    final int[][] message = new int[result.length + 1][];
    message[0] = new int[] {-1, -1};
    System.arraycopy(result, 0, message, 1, result.length);
    return message;
  }
}
