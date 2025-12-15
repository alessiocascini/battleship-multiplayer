package backend;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler implements Runnable {
  private final Socket clientSocket;

  public ClientHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      int[][][] shipPositions = (int[][][]) in.readObject();
      System.out.println("Received ship positions:" + Arrays.deepToString(shipPositions));

      Server.storeShipPositions(shipPositions);

      final boolean isFirstPlayer = Server.getShipPositions()[1] == null;
      if (isFirstPlayer) while (Server.getShipPositions()[1] == null) Thread.sleep(100);
      out.writeObject(isFirstPlayer);
    } catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
