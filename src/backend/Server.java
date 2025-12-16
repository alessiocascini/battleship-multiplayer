package backend;

import java.io.IOException;
import java.net.*;

public class Server {
  // Store ship positions for two players
  public static final Ship[][] shipPositions = new Ship[2][];

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          // Accept a client for ship placement
          Socket clientSocket = serverSocket.accept();
          new Thread(new PlacementHandler(clientSocket)).start();

          // Wait until both players have placed ships
          Server.class.wait();
        } while (shipPositions[1] == null);
      }

      // After both players placed ships, handle game moves
      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new GameHandler(clientSocket)).start();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  // Store the ship positions received from a player
  public static synchronized void storeShipPositions(int[][][] positions) {
    int playerIndex = shipPositions[0] == null ? 0 : 1; // Determine player index
    shipPositions[playerIndex] = new Ship[positions.length];
    for (int i = 0; i < positions.length; i++)
      shipPositions[playerIndex][i] = new Ship(positions[i]);

    Server.class.notifyAll(); // Notify waiting threads that ships are stored
  }

  public static boolean isWaitingForSecondPlayer() {
    return shipPositions[1] == null;
  }

  // Ship class representing a ship and its cells
  public static class Ship {
    final Cell[] cells;
    boolean sunk;

    Ship(int[][] cells) {
      this.cells = new Cell[cells.length];
      for (int i = 0; i < cells.length; i++) this.cells[i] = new Cell(cells[i][0], cells[i][1]);
      this.sunk = false; // Ship is initially not sunk
    }

    // Represents a single cell of a ship
    public static class Cell {
      final int row;
      final int col;
      boolean hit;

      Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.hit = false; // Cell initially not hit
      }
    }
  }
}
