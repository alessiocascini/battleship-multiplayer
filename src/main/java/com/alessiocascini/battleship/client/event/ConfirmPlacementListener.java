package com.alessiocascini.battleship.client.event;

import static com.alessiocascini.battleship.client.ui.PlacementUI.shipInfos;

import com.alessiocascini.battleship.client.model.Ship;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Listener that handles the confirmation of ship placement. It validates that all ships are placed,
 * sends the coordinates to the server, and receives the player's turn order (Player 1 or Player 2).
 *
 * @author Alessio Cascini
 */
public class ConfirmPlacementListener implements ActionListener {
  private final PlacementHandler handler;
  private final List<Ship> ships;

  /** Stores whether this client is the first player, assigned by the server */
  private Boolean isFirstPlayer = null;

  /**
   * Constructs the listener with a UI handler and the list of placed ships.
   *
   * @param handler The UI handler to manage messages and transitions
   * @param ships The list of ships placed by the user
   */
  public ConfirmPlacementListener(PlacementHandler handler, List<Ship> ships) {
    this.handler = handler;
    this.ships = ships;
  }

  /**
   * Triggered when the "Confirm" button is clicked. Initiates server connection and, upon success,
   * transitions to the game UI.
   *
   * @param e The action event
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    final String message = connectToServer();
    handler.showMessage(message);

    // If turn order was successfully received, proceed to the game
    if (isFirstPlayer != null) handler.proceedToGameUI(isFirstPlayer);
  }

  /**
   * Validates the fleet and communicates with the server via Socket. 1. Checks if the number of
   * placed ships is correct. 2. Converts ship objects into a serializable 3D array. 3. Sends data
   * to the server and waits for the player assignment.
   *
   * @return A status message describing the outcome of the operation
   */
  private String connectToServer() {
    // Validation: ensures the fleet is complete
    if (ships.size() < shipInfos.length) return "Place all ships before confirming!";

    // Transform the list of Ship objects into a primitive format for network transfer
    final int[][][] shipPositions = new int[shipInfos.length][][];
    for (int i = 0; i < ships.size(); i++) shipPositions[i] = ships.get(i).getCellsAsArray();

    // Establish connection to the server on localhost:5000
    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Send the placement data
      out.writeObject(shipPositions);

      // Receive player identity (true = Player 1, false = Player 2)
      isFirstPlayer = (Boolean) in.readObject();

      return isFirstPlayer ? "You start first!" : "Opponent starts first!";

    } catch (Exception e) {
      return "Failed to connect to server: " + e.getMessage();
    }
  }
}
