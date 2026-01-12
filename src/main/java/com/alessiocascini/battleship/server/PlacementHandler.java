package com.alessiocascini.battleship.server;

import java.io.*;
import java.net.Socket;

/**
 * Handles the initial ship placement phase for a single client connection. This class reads the
 * ship coordinates from the client, stores them in the server's state, and synchronizes the start
 * of the game between players.
 *
 * @author Alessio Cascini
 */
public class PlacementHandler implements Runnable {
  /** The socket connection to the client */
  private final Socket clientSocket;

  /**
   * Constructs a new PlacementHandler for the given client socket. * @param clientSocket The socket
   * through which the client is connected
   */
  public PlacementHandler(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  /**
   * Executes the placement logic: 1. Receives ship positions from the client. 2. Registers
   * positions in the central {@link Server}. 3. Waits until the second player has also completed
   * their placement. 4. Sends a confirmation to the client indicating their player role.
   */
  @Override
  public void run() {
    try (Socket socket = clientSocket;
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Receive the 3D array representing ship coordinates from the client
      int[][][] shipPositions = (int[][][]) in.readObject();

      // Store the received positions in the global server state
      Server.storeShipPositions(shipPositions);

      // Determine if this client is Player 1
      final boolean isFirstPlayer = Server.isWaitingForSecondPlayer();

      // Busy-wait loop (polling) until the second player joins and completes placement
      if (isFirstPlayer) while (Server.isWaitingForSecondPlayer()) Thread.sleep(100);

      // Send player identity back to the client
      out.writeObject(isFirstPlayer);

    } catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
