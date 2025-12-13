package backend;

import java.io.IOException;
import java.net.*;

public class Server {
  private static final int[][][][] shipPositions = new int[2][][][];

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      do {
        Socket clientSocket = serverSocket.accept();
        new Thread(new ClientHandler(clientSocket)).start();
      } while (shipPositions[1] == null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static synchronized void storeShipPositions(int[][][] positions) {
    shipPositions[shipPositions[0] == null ? 0 : 1] = positions;
  }

  public static int[][][][] getShipPositions() {
    return shipPositions;
  }
}
