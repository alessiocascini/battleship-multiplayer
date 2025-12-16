package backend;

import java.io.*;
import java.net.Socket;

public class GameHandler implements Runnable {
  // Track how many ships each player has sunk
  private static final int[] sunkenShipsCount = {0, 0};
  private static boolean isPlayerOneTurn = true; // Track whose turn it is
  private static boolean isFirstTurn = true; // Special handling for second player on first turn
  private static int[] lastMove = null; // Store last move to sync players
  private static int[][] lastResult = null; // Store last move result
  private final Socket clientSocket;

  public GameHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  // Process a player's move and return result
  private static int[][] processMove(int[] move) {
    // Target opponent's ships
    final Server.Ship[] playerShips = Server.shipPositions[isPlayerOneTurn ? 1 : 0];

    for (Server.Ship ship : playerShips)
      for (int i = 0; i < ship.cells.length; i++)
        if (ship.cells[i].row == move[0] && ship.cells[i].col == move[1]) {
          ship.cells[i].hit = true; // Mark hit

          // Check if ship is now sunk
          boolean sunk = true;
          for (Server.Ship.Cell cell : ship.cells)
            if (!cell.hit) {
              sunk = false;
              break;
            }
          ship.sunk = sunk;

          // Return hit coordinates, or full ship coordinates if sunk
          int[][] cells = new int[ship.cells.length][2];
          for (int k = 0; k < ship.cells.length; k++) {
            cells[k][0] = ship.cells[k].row;
            cells[k][1] = ship.cells[k].col;
          }
          return sunk ? cells : new int[][] {move};
        }

    return new int[][] {}; // Miss
  }

  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Determine which player is connected
      final boolean isPlayerOne = (Boolean) in.readObject();

      if (isPlayerOne == isPlayerOneTurn) {
        out.writeObject(true); // Confirm it's player's turn

        // Handle first move for second player
        if (!isPlayerOne && isFirstTurn) {
          out.writeObject(lastMove);
          out.writeObject(lastResult);
          isFirstTurn = false;
        }

        // Read the player's move
        final int[] move = (int[]) in.readObject();
        final int[][] result = processMove(move);

        // Update sunk ships count if a ship was sunk
        if (result.length > 1) sunkenShipsCount[isPlayerOneTurn ? 1 : 0]++;

        // Check if player has won
        if (sunkenShipsCount[isPlayerOne ? 1 : 0]
            == Server.shipPositions[isPlayerOne ? 1 : 0].length) {
          final int[][] winMessage = new int[result.length + 1][2];
          winMessage[0] = new int[] {-1, -1}; // Special signal for win
          System.arraycopy(result, 0, winMessage, 1, result.length);
          out.writeObject(winMessage);
        } else out.writeObject(result); // Normal result

        // Save last move for opponent
        lastMove = move;
        lastResult = result;

        // Switch turn
        isPlayerOneTurn = !isPlayerOneTurn;

        // Wait for opponent's turn to finish
        while (isPlayerOne != isPlayerOneTurn) Thread.sleep(100);

        // Send opponent's move to player
        if (sunkenShipsCount[isPlayerOne ? 0 : 1]
            == Server.shipPositions[isPlayerOne ? 0 : 1].length)
          out.writeObject(new int[] {-1, -1}); // Signal game over
        else out.writeObject(lastMove);
        out.writeObject(lastResult);
      } else out.writeObject(false); // Not player's turn
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
