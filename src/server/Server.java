package server;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
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
    final Cell moveCell = new Cell(!isPlayerOne, move[0], move[1]);

    hitCells.add(moveCell);

    for (Ship ship : shipPositions)
      if (Arrays.asList(ship.cells).contains(moveCell))
        return Arrays.stream(ship.cells).allMatch(hitCells::contains)
            ? Arrays.stream(ship.cells).map(c -> new int[] {c.row, c.col}).toArray(int[][]::new)
            : new int[][] {move};

    return new int[][] {};
  }

  private static class Ship {
    private final Cell[] cells;

    public Ship(boolean isPlayerOne, int[][] cells) {
      this.cells = new Cell[cells.length];
      for (int i = 0; i < cells.length; i++)
        this.cells[i] = new Cell(isPlayerOne, cells[i][0], cells[i][1]);
    }
  }

  private record Cell(boolean isPlayerOne, int row, int col) {}
}
