package backend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameHandler implements Runnable {
  private final Socket clientSocket;

  public GameHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    System.out.println("GameHandler started for client: " + clientSocket.getRemoteSocketAddress());

    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      final boolean isFirstPlayer = (Boolean) in.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
