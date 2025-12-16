package backend;

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

      // Read ship positions sent by the client
      int[][][] shipPositions = (int[][][]) in.readObject();

      // Store positions on the server
      Server.storeShipPositions(shipPositions);

      // Determine if this player is the first to play
      final boolean isFirstPlayer = Server.isWaitingForSecondPlayer();

      // If first player, wait until second player has placed ships
      if (isFirstPlayer) while (Server.isWaitingForSecondPlayer()) Thread.sleep(100);

      // Send to client whether they start first or second
      out.writeObject(isFirstPlayer);

    } catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
