package backend;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Server {
  public static final ArrayList<Ship>[] ships = new ArrayList[2]; // set to private after testing
  private static final int[][][][] shipPositions = new int[2][][][];
  private static boolean isPlayerOneTurn = true;

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new ClientHandler(clientSocket)).start();

          Server.class.wait();
        } while (shipPositions[1] == null);
      }

      for (int player = 0; player < 2; player++) {
        ships[player] = new ArrayList<>();
        for (int[][] shipPos : shipPositions[player]) {
          ArrayList<Ship.Cell> cells = new ArrayList<>();
          for (int[] pos : shipPos) {
            cells.add(new Ship.Cell(pos[0], pos[1]));
          }
          ships[player].add(new Ship(cells));
        }
      }

      while (true) {
        Socket clientSocket = serverSocket.accept();
        new GameHandler(clientSocket);
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static synchronized void storeShipPositions(int[][][] positions) {
    shipPositions[shipPositions[0] == null ? 0 : 1] = positions;
    Server.class.notifyAll();
  }

  public static int[][][][] getShipPositions() {
    return shipPositions;
  }

  public static boolean isPlayerOneTurn() {
    return isPlayerOneTurn;
  }

  public static void passTurn() {
    Server.isPlayerOneTurn = !isPlayerOneTurn;
  }

  public static class Ship {
    final ArrayList<Cell> cells;
    boolean sunk;

    Ship(ArrayList<Cell> cells) {
      this.cells = cells;
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
