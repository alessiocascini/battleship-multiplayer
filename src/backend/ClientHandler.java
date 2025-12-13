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
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      int[][][] shipPositions = (int[][][]) in.readObject();
      System.out.println("Received ship positions:" + Arrays.deepToString(shipPositions));

      Server.storeShipPositions(shipPositions);

      if (Server.getShipPositions()[1] == null) {
        while (Server.getShipPositions()[1] == null) Thread.sleep(100);
        out.println(0);
      } else out.println(1);
    } catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
