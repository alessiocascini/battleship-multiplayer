package backend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameHandler {
  public GameHandler(final Socket clientSocket) {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      final boolean isPlayerOne = (Boolean) in.readObject();
      if (isPlayerOne == Server.isPlayerOneTurn()) {
        out.writeObject(true);

        final int[] move = (int[]) in.readObject();
        final int[][][] playerShips = Server.getShipPositions()[Server.isPlayerOneTurn() ? 1 : 0];

        String result = "Miss";

        for (int[][] ship : playerShips) {
          for (int[] cell : ship) {
            if (cell[0] == move[0] && cell[1] == move[1]) {
              result = "Hit";
              break;
            }
          }
          if (result.equals("Hit")) break;
        }

        out.writeObject(result);
        Server.passTurn();
      } else out.writeObject(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
