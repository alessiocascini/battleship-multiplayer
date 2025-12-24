package com.alessiocascini.battleship.server;

import java.io.*;
import java.net.Socket;

public class PlacementHandler implements Runnable {
  private final Socket clientSocket;

  public PlacementHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      int[][][] shipPositions = (int[][][]) in.readObject();

      Server.storeShipPositions(shipPositions);

      final boolean isFirstPlayer = Server.isWaitingForSecondPlayer();
      if (isFirstPlayer) while (Server.isWaitingForSecondPlayer()) Thread.sleep(100);
      out.writeObject(isFirstPlayer);
    } catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
