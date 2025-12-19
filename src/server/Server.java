package server;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class Server {
  private static final Set<Ship> shipPositions = new HashSet<>();
  private static final Set<Cell> hitCells = new HashSet<>();
  private static boolean isWaitingForSecondPlayer = false;

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new PlacementHandler(clientSocket)).start();

          Server.class.wait();
        } while (shipPositions.isEmpty() || isWaitingForSecondPlayer);
      }

      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new GameHandler(clientSocket)).start();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static int getShipCount() {
    return shipPositions.size() / 2;
  }

  public static synchronized void storeShipPositions(int[][][] positions) {
    isWaitingForSecondPlayer = !isWaitingForSecondPlayer;

    for (int[][] position : positions)
      shipPositions.add(new Ship(isWaitingForSecondPlayer, position));

    Server.class.notifyAll();
  }

  public static boolean isWaitingForSecondPlayer() {
    return isWaitingForSecondPlayer;
  }

  public static int[][] processMove(boolean isPlayerOne, int[] move) {
    for (Ship ship : shipPositions)
      if (ship.isPlayerOne == !isPlayerOne)
        for (int[] cell : ship.cells)
          if (cell[0] == move[0] && cell[1] == move[1]) {
            hitCells.add(new Cell(!isPlayerOne, move[0], move[1]));

            for (int[] c : ship.cells)
              if (!hitCells.contains(new Cell(!isPlayerOne, c[0], c[1]))) return new int[][] {move};

            return ship.cells;
          }

    return new int[][] {};
  }

  private record Ship(boolean isPlayerOne, int[][] cells) {}

  private record Cell(boolean isPlayerOne, int row, int col) {}
}
