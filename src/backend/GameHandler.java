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

        if (!isPlayerOne && Server.isFirstTurn) {
          out.writeObject(Server.lastMove);
          out.writeObject(Server.lastResult);

          Server.isFirstTurn = false;
        }

        final int[] move = (int[]) in.readObject();
        final int[][] result = processMove(move);

        if (result.length > 1) Server.sunkenShipsCount[Server.isPlayerOneTurn() ? 1 : 0]++;

        if (Server.sunkenShipsCount[isPlayerOne ? 1 : 0]
            == Server.ships[isPlayerOne ? 1 : 0].size()) {
          final int[][] winMessage = new int[result.length + 1][2];
          winMessage[0] = new int[] {-1, -1};
          System.arraycopy(result, 0, winMessage, 1, result.length);
          out.writeObject(winMessage);
        } else out.writeObject(result);

        Server.lastMove = move;
        Server.lastResult = result;

        Server.passTurn();

        while (isPlayerOne != Server.isPlayerOneTurn()) Thread.sleep(100);
        if (Server.sunkenShipsCount[isPlayerOne ? 0 : 1]
            == Server.ships[isPlayerOne ? 0 : 1].size()) out.writeObject(new int[] {-1, -1});
        else out.writeObject(Server.lastMove);
        out.writeObject(Server.lastResult);

      } else out.writeObject(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
