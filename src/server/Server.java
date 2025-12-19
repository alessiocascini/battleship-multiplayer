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
        } while (isWaitingForSecondPlayer);
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
    final Cell moveCell = new Cell(!isPlayerOne, move[0], move[1]);

    for (Ship ship : shipPositions)
      if (ship.cells.contains(moveCell)) {
        hitCells.add(moveCell);

        return hitCells.containsAll(ship.cells)
            ? ship.cells.stream().map(c -> new int[] {c.row, c.col}).toArray(int[][]::new)
            : new int[][] {move};
      }

    return new int[][] {};
  }

  private static class Ship {
    private final Set<Cell> cells = new HashSet<>();

    public Ship(boolean isPlayerOne, int[][] positions) {
      for (int[] pos : positions) cells.add(new Cell(isPlayerOne, pos[0], pos[1]));
    }
  }

  private record Cell(boolean isPlayerOne, int row, int col) {}
}
