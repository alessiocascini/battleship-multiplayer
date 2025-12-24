package com.alessiocascini.battleship.client.event;

import static com.alessiocascini.battleship.client.ui.ShipPlacementUI.shipInfos;

import com.alessiocascini.battleship.client.model.Ship;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ConfirmButtonListener implements ActionListener {
  private final ShipPlacementHandler handler;
  private final List<Ship> ships;
  private Boolean isFirstPlayer = null;

  public ConfirmButtonListener(ShipPlacementHandler handler, List<Ship> ships) {
    this.handler = handler;
    this.ships = ships;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String message = connectToServer();
    handler.showMessage(message);

    if (isFirstPlayer != null) handler.proceedToGameUI(isFirstPlayer);
  }

  private String connectToServer() {
    if (ships.size() < shipInfos.length) return "Place all ships before confirming!";

    final int[][][] shipPositions = new int[shipInfos.length][][];
    for (int i = 0; i < ships.size(); i++) shipPositions[i] = ships.get(i).getCellsAsArray();

    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
      out.writeObject(shipPositions);
      isFirstPlayer = (Boolean) in.readObject();
      return isFirstPlayer ? "You start first!" : "Opponent starts first!";
    } catch (Exception e) {
      return "Failed to connect to server: " + e.getMessage();
    }
  }
}
