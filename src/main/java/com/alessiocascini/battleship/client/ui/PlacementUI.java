package com.alessiocascini.battleship.client.ui;

import com.alessiocascini.battleship.client.event.ConfirmPlacementListener;
import com.alessiocascini.battleship.client.event.PlacementCellListener;
import com.alessiocascini.battleship.client.event.PlacementHandler;
import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import com.alessiocascini.battleship.client.model.ShipInfo;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Graphical interface for the ship placement phase. Provides a grid for players to position their
 * fleet and controls to select ship types and orientation.
 *
 * @author Alessio Cascini
 */
public class PlacementUI extends JFrame implements PlacementHandler {
  /** The size of the game board (10x10) */
  public static final int gridSize = 10;

  /** Predefined fleet configuration with ship names and their respective sizes */
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

  /** List of ships currently placed on the board */
  private final List<Ship> ships = new ArrayList<>();

  /**
   * Initializes the placement window, sets up the grid of buttons, and configures the control
   * panel.
   */
  public PlacementUI() {
    super("Place your ships");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 700);
    setLayout(new BorderLayout());

    // Initialize the 10x10 grid with buttons representing cells
    gridPanel = new JPanel(new GridLayout(gridSize, gridSize));
    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;
      // Attach listener to handle ship placement logic on click
      button.addActionListener(new PlacementCellListener(this, ships, row, col));
      gridPanel.add(button);
    }

    // Control panel for ship selection, orientation, and actions
    JPanel controlPanel = new JPanel(new GridLayout(4, 1));

    shipSelector = new JComboBox<>();
    for (ShipInfo ship : shipInfos)
      shipSelector.addItem(ship.name() + " (size: " + ship.size() + ")");

    orientationSelector = new JComboBox<>(new String[] {"Horizontal", "Vertical"});

    JButton confirmButton = new JButton("Confirm");
    confirmButton.addActionListener(new ConfirmPlacementListener(this, ships));

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(
        _ -> {
          ships.clear();
          for (Component comp : gridPanel.getComponents()) comp.setBackground(null);
          // Enable buttons that might have been disabled during placement
          for (Component comp : gridPanel.getComponents()) comp.setEnabled(true);
        });

    controlPanel.add(shipSelector);
    controlPanel.add(orientationSelector);
    controlPanel.add(confirmButton);
    controlPanel.add(resetButton);

    add(gridPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  /**
   * Main entry point for the client application.
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(PlacementUI::new);
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

  /**
   * Colors the cells occupied by a newly placed ship to provide visual feedback.
   *
   * @param ship The ship object containing the coordinates to highlight
   */
  @Override
  public void highlightShipCells(Ship ship) {
    for (Cell cell : ship.cells()) {
      final JButton button = (JButton) gridPanel.getComponent(cell.row() * gridSize + cell.col());
      button.setBackground(Color.GRAY);
      button.setEnabled(false); // Prevents overlapping ships
    }
  }

  /**
   * Closes the placement screen and opens the main game interface.
   *
   * @param isFirstPlayer Boolean indicating if this client is Player 1
   */
  @Override
  public void proceedToGameUI(boolean isFirstPlayer) {
    new GameUI(ships, isFirstPlayer);
    this.dispose();
  }
}
