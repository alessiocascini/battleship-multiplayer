package com.alessiocascini.battleship.client.ui;

import com.alessiocascini.battleship.client.event.CellClickHandler;
import com.alessiocascini.battleship.client.event.CellClickListener;
import com.alessiocascini.battleship.client.model.Ship;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import javax.swing.*;

public class ShipPlacementUI extends JFrame implements CellClickHandler {
  public static final int gridSize = 10;
  public static final Ship[] ships = {
    new Ship("Carrier", 5),
    new Ship("Battleship", 4),
    new Ship("Cruiser", 3),
    new Ship("Submarine", 3),
    new Ship("Destroyer", 2)
  };

  private final JPanel gridPanel;
  private final JComboBox<String> shipSelector;
  private final JComboBox<String> orientationSelector;

  private final HashMap<Ship, int[][]> positions = new HashMap<>();

  public ShipPlacementUI() {
    super("Place your ships");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 700);
    setLayout(new BorderLayout());

    gridPanel = new JPanel(new GridLayout(gridSize, gridSize));
    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;
      button.addActionListener(new CellClickListener(this, positions, row, col));
      gridPanel.add(button);
    }

    JPanel controlPanel = new JPanel(new GridLayout(4, 1));

    shipSelector = new JComboBox<>();
    for (Ship ship : ships) shipSelector.addItem(ship.name() + " (size: " + ship.size() + ")");

    orientationSelector = new JComboBox<>(new String[] {"Horizontal", "Vertical"});

    JButton confirmButton = new JButton("Confirm");
    confirmButton.addActionListener(_ -> connectToServer());

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(
        _ -> {
          positions.clear();
          for (Component comp : gridPanel.getComponents()) comp.setBackground(null);
        });

    controlPanel.add(shipSelector);
    controlPanel.add(orientationSelector);
    controlPanel.add(confirmButton);
    controlPanel.add(resetButton);

    add(gridPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(ShipPlacementUI::new);
  }

  private void connectToServer() {
    if (positions.size() == ships.length) {
      final int[][][] shipPositions = new int[ships.length][][];
      for (int i = 0; i < ships.length; i++) shipPositions[i] = positions.get(ships[i]);

      try (Socket socket = new Socket("localhost", 5000);
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
        out.writeObject(shipPositions);

        boolean isFirstPlayer = (Boolean) in.readObject();

        if (isFirstPlayer) JOptionPane.showMessageDialog(this, "You start first!");
        else JOptionPane.showMessageDialog(this, "Opponent starts first!");

        new GameUI(shipPositions, isFirstPlayer);
        this.dispose();
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage());
      }
    } else JOptionPane.showMessageDialog(this, "Place all ships before confirming!");
  }

  @Override
  public int getSelectedShipIndex() {
    return shipSelector.getSelectedIndex();
  }

  @Override
  public int getSelectedOrientationIndex() {
    return orientationSelector.getSelectedIndex();
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
  }

  @Override
  public void highlightShipCells(Ship ship, int[][] coords) {
    for (int i = 0; i < ship.size(); i++)
      gridPanel.getComponent(coords[i][0] * gridSize + coords[i][1]).setBackground(Color.GRAY);
  }
}
