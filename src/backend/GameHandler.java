package backend;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class GameHandler implements Runnable {
  private final Socket clientSocket;

  public GameHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  private static int[][] processMove(int[] move) {
    final ArrayList<Server.Ship> playerShips = Server.ships[Server.isPlayerOneTurn() ? 1 : 0];

    for (Server.Ship ship : playerShips)
      for (int j = 0; j < ship.cells.size(); j++)
        if (ship.cells.get(j).row == move[0] && ship.cells.get(j).col == move[1]) {
          ship.cells.get(j).hit = true;

          boolean sunk = true;
          for (Server.Ship.Cell cell : ship.cells)
            if (!cell.hit) {
              sunk = false;
              break;
            }
          ship.sunk = sunk;

          int[][] cells = new int[ship.cells.size()][2];
          for (int k = 0; k < ship.cells.size(); k++) {
            cells[k][0] = ship.cells.get(k).row;
            cells[k][1] = ship.cells.get(k).col;
          }
          return sunk ? cells : new int[][] {move};
        }

    return new int[][] {};
  }

  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      final boolean isPlayerOne = (Boolean) in.readObject();
      if (isPlayerOne == Server.isPlayerOneTurn()) {
        out.writeObject(true);

        final int[] move = (int[]) in.readObject();
        out.writeObject(processMove(move));
        Server.passTurn();

        while (isPlayerOne == Server.isPlayerOneTurn()) Thread.sleep(100);
        out.writeObject("Your turn");
      } else out.writeObject(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
