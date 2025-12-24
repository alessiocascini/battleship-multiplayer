package com.alessiocascini.battleship.server;

import java.io.*;
import java.net.Socket;

public class GameHandler implements Runnable {
  private static final int[] sunkenShipsCount = {0, 0};
  private static boolean isPlayerOneTurn = true;
  private static boolean isFirstTurn = true;
  private static int[] move = null;
  private static int[][] result = null;
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

      out.writeObject(isPlayerOne == isPlayerOneTurn);
      if (isPlayerOne != isPlayerOneTurn) return;

      if (!isPlayerOne && isFirstTurn) {
        out.writeObject(move);
        out.writeObject(result);

        isFirstTurn = false;
      }

      move = (int[]) in.readObject();
      result = Server.processMove(isPlayerOne, move);

      if (result.length > 1) sunkenShipsCount[isPlayerOne ? 1 : 0]++;

      if (sunkenShipsCount[isPlayerOne ? 1 : 0] < Server.getShipCount()) out.writeObject(result);
      else {
        final int[][] message = new int[result.length + 1][];
        message[0] = new int[] {-1, -1};
        System.arraycopy(result, 0, message, 1, result.length);
        out.writeObject(message);
      }

      isPlayerOneTurn = !isPlayerOneTurn;

      while (isPlayerOne != isPlayerOneTurn) Thread.sleep(100);

      if (sunkenShipsCount[isPlayerOne ? 0 : 1] < Server.getShipCount()) out.writeObject(move);
      else out.writeObject(new int[] {-1, -1});
      out.writeObject(result);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
