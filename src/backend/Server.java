package backend;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class Server {
  public static final Ship[][] shipPositions = new Ship[2][];

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new PlacementHandler(clientSocket)).start();

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

  public record Ship(int[][] cells) {
    private static final Set<Cell> hitCells = new HashSet<>();

    private record Cell(boolean isPlayerOne, int row, int col) {}

    public static int[][] processMove(boolean isPlayerOne, int[] move) {
      for (Ship ship : shipPositions[isPlayerOne ? 1 : 0])
        for (int[] cell : ship.cells)
          if (cell[0] == move[0] && cell[1] == move[1]) {
            hitCells.add(new Cell(isPlayerOne, cell[0], cell[1]));

            for (int[] c : ship.cells)
              if (!hitCells.contains(new Cell(isPlayerOne, c[0], c[1]))) return new int[][] {move};

            return ship.cells;
          }

      return new int[][] {};
    }
  }
}
