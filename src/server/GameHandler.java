package server;

import java.io.*;
import java.net.Socket;

public class GameHandler implements Runnable {
  private static final int[] sunkenShipsCount = {0, 0};
  private static boolean isPlayerOneTurn = true;
  private static boolean isFirstTurn = true;
  private static int[] lastMove = null;
  private static int[][] lastResult = null;
  private final Socket clientSocket;

  public GameHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      final boolean isPlayerOne = (Boolean) in.readObject();
      if (isPlayerOne == isPlayerOneTurn) {
        out.writeObject(true);

        if (!isPlayerOne && isFirstTurn) {
          out.writeObject(lastMove);
          out.writeObject(lastResult);

          isFirstTurn = false;
        }

        final int[] move = (int[]) in.readObject();
        final int[][] result = Server.processMove(isPlayerOne, move);

        if (result.length > 1) sunkenShipsCount[isPlayerOne ? 1 : 0]++;

        if (sunkenShipsCount[isPlayerOne ? 1 : 0] == Server.getShipCount()) {
          final int[][] winMessage = new int[result.length + 1][2];
          winMessage[0] = new int[] {-1, -1};
          System.arraycopy(result, 0, winMessage, 1, result.length);
          out.writeObject(winMessage);
        } else out.writeObject(result);

        lastMove = move;
        lastResult = result;

        isPlayerOneTurn = !isPlayerOneTurn;

        while (isPlayerOne != isPlayerOneTurn) Thread.sleep(100);
        if (sunkenShipsCount[isPlayerOne ? 0 : 1] == Server.getShipCount())
          out.writeObject(new int[] {-1, -1});
        else out.writeObject(lastMove);
        out.writeObject(lastResult);
      } else out.writeObject(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
