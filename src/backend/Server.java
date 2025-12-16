package backend;

import java.io.IOException;
import java.net.*;

public class Server {
  public static final Ship[][] shipPositions = new Ship[2][];

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new ClientHandler(clientSocket)).start();

          Server.class.wait();
        } while (shipPositions[1] == null);
      }

      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new GameHandler(clientSocket)).start();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static synchronized void storeShipPositions(int[][][] positions) {
    int playerIndex = shipPositions[0] == null ? 0 : 1;
    shipPositions[playerIndex] = new Ship[positions.length];
    for (int i = 0; i < positions.length; i++)
      shipPositions[playerIndex][i] = new Ship(positions[i]);

    Server.class.notifyAll();
  }

  public static boolean isWaitingForSecondPlayer() {
    return shipPositions[1] == null;
  }

  public static class Ship {
    final Cell[] cells;
    boolean sunk;

    Ship(int[][] cells) {
      this.cells = new Cell[cells.length];
      for (int i = 0; i < cells.length; i++) this.cells[i] = new Cell(cells[i][0], cells[i][1]);

      this.sunk = false;
    }

    public static class Cell {
      final int row;
      final int col;
      boolean hit;

      Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.hit = false;
      }
    }
  }
}
