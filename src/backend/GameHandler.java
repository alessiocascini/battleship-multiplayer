package backend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class GameHandler {
  public GameHandler(final Socket clientSocket) {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      final boolean isPlayerOne = (Boolean) in.readObject();
      if (isPlayerOne == Server.isPlayerOneTurn()) {
        out.writeObject(true);

        final int[] move = (int[]) in.readObject();
        out.writeObject(processMove(move));
        Server.passTurn();
      } else out.writeObject(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
}

/*  private static String processMove(int[] move) {
  final int[][][] playerShips = Server.getShipPositions()[Server.isPlayerOneTurn() ? 1 : 0];

  for (int i = 0; i < playerShips.length; i++) {
    int[][] ship = playerShips[i];

    for (int j = 0; j < ship.length; j++)
      if (ship[j][0] == move[0] && ship[j][1] == move[1]) {
        ship[j] = ship[ship.length - 1];
        ship = java.util.Arrays.copyOf(ship, ship.length - 1);
        playerShips[i] = ship;

        return ship.length == 0 ? "Sunk" : "Hit";
      }
  }

  return "Miss";
}*/
