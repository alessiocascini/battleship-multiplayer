package com.alessiocascini.battleship.client.ui;

import com.alessiocascini.battleship.client.event.CellClickListener;
import com.alessiocascini.battleship.client.event.ConfirmButtonListener;
import com.alessiocascini.battleship.client.event.ShipPlacementHandler;
import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import com.alessiocascini.battleship.client.model.ShipInfo;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ShipPlacementUI extends JFrame implements ShipPlacementHandler {
  public static final int gridSize = 10;
  public static final ShipInfo[] shipInfos = {
    new ShipInfo("Carrier", 5),
    new ShipInfo("Battleship", 4),
    new ShipInfo("Cruiser", 3),
    new ShipInfo("Submarine", 3),
    new ShipInfo("Destroyer", 2)
  };

  private final JPanel gridPanel;
  private final JComboBox<String> shipSelector;
  private final JComboBox<String> orientationSelector;

  private final List<Ship> ships = new ArrayList<>();

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
      button.addActionListener(new CellClickListener(this, ships, row, col));
      gridPanel.add(button);
    }

    JPanel controlPanel = new JPanel(new GridLayout(4, 1));

    shipSelector = new JComboBox<>();
    for (ShipInfo ship : shipInfos)
      shipSelector.addItem(ship.name() + " (size: " + ship.size() + ")");

    orientationSelector = new JComboBox<>(new String[] {"Horizontal", "Vertical"});

    JButton confirmButton = new JButton("Confirm");
    confirmButton.addActionListener(new ConfirmButtonListener(this, ships));

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(
        _ -> {
          ships.clear();
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
  public void highlightShipCells(Ship ship) {
    for (Cell cell : ship.cells())
      gridPanel.getComponent(cell.row() * gridSize + cell.col()).setBackground(Color.GRAY);
  }

  @Override
  public void proceedToGameUI(boolean isFirstPlayer) {
    new GameUI(ships, isFirstPlayer);
    this.dispose();
  }
}
