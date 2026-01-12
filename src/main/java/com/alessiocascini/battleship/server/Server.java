package com.alessiocascini.battleship.server;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The main entry point for the Battleship game server. This class manages client connections,
 * player synchronization during the placement phase, and tracks the state of the game (ship
 * positions and hits).
 *
 * @author Alessio Cascini
 */
public class Server {
  /** Stores all ships placed by players */
  private static final Set<Ship> shipPositions = new HashSet<>();

  /** Tracks which cells have been successfully hit */
  private static final Set<Cell> hitCells = new HashSet<>();

  /** State flag to synchronize the placement phase between two players */
  private static boolean isWaitingForSecondPlayer = false;

  /**
   * Starts the server on port 5000 and handles the game lifecycle. Phase 1: Accepts connections for
   * ship placement. Phase 2: Accepts connections for game moves.
   *
   * @param args Command line arguments (not used)
   */
  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      // Synchronized block to manage the waiting logic for the second player
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new PlacementHandler(clientSocket)).start();

          Server.class.wait();
        } while (isWaitingForSecondPlayer);
      }

      // Continuous loop to handle game turns
      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new GameHandler(clientSocket)).start();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculates the number of ships based on the total number of occupied cells.
   *
   * @return The current ship count
   */
  public static int getShipCount() {
    return shipPositions.size() / 2;
  }

  /**
   * Stores the coordinates of ships placed by a player. This method is synchronized to safely
   * update the game state and notify waiting threads.
   *
   * @param positions A 3D array containing the coordinates for each ship
   */
  public static synchronized void storeShipPositions(int[][][] positions) {
    isWaitingForSecondPlayer = !isWaitingForSecondPlayer;

    for (int[][] position : positions)
      shipPositions.add(new Ship(isWaitingForSecondPlayer, position));

    Server.class.notifyAll();
  }

  /**
   * @return true if the server is still waiting for the second player to finish placement
   */
  public static boolean isWaitingForSecondPlayer() {
    return isWaitingForSecondPlayer;
  }

  /**
   * Processes a move from a player and checks if it's a hit, a miss, or a sinking move.
   *
   * @param isPlayerOne Boolean indicating if the attacker is Player 1
   * @param move Array representing the [row, col] of the move
   * @return An empty array for a miss, an array with one cell for a hit, or all ship cells if the
   *     ship is sunk.
   */
  public static int[][] processMove(boolean isPlayerOne, int[] move) {
    final Cell moveCell = new Cell(!isPlayerOne, move[0], move[1]);

    for (Ship ship : shipPositions)
      if (ship.cells.contains(moveCell)) {
        hitCells.add(moveCell);

        // Check if all cells of the current ship have been hit
        return hitCells.containsAll(ship.cells)
            ? ship.cells.stream().map(c -> new int[] {c.row, c.col}).toArray(int[][]::new)
            : new int[][] {move};
      }

    return new int[][] {};
  }

  /** Internal representation of a Ship, containing a set of occupied cells. */
  private static class Ship {
    private final Set<Cell> cells = new HashSet<>();

    public Ship(boolean isPlayerOne, int[][] positions) {
      for (int[] pos : positions) cells.add(new Cell(isPlayerOne, pos[0], pos[1]));
    }
  }

  /** Represents a single coordinate on the board associated with a specific player. */
  private record Cell(boolean isPlayerOne, int row, int col) {}
}
