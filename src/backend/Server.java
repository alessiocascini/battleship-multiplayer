package backend;

import java.io.IOException;
import java.net.*;

public class Server {
  private static final int[][][][] shipPositions = new int[2][][][];
  private static boolean isPlayerOneTurn = true;

  public static void main(String[] args) {
    try (ServerSocket serverSocket = new ServerSocket(5000)) {
      synchronized (Server.class) {
        do {
          Socket clientSocket = serverSocket.accept();
          new Thread(new ClientHandler(clientSocket)).start();

          Server.class.wait();
        } while (shipPositions[1] == null);
      }

      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new GameHandler(clientSocket)).start();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static synchronized void storeShipPositions(int[][][] positions) {
    shipPositions[shipPositions[0] == null ? 0 : 1] = positions;
    Server.class.notifyAll();
  }

  public static int[][][][] getShipPositions() {
    return shipPositions;
  }

  public static boolean isPlayerOneTurn() {
    return isPlayerOneTurn;
  }

  public static void passTurn() {
    Server.isPlayerOneTurn = !isPlayerOneTurn;
  }
}
