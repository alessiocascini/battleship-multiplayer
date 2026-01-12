package com.alessiocascini.battleship.client.event;

import static com.alessiocascini.battleship.client.ui.PlacementUI.gridSize;
import static com.alessiocascini.battleship.client.ui.PlacementUI.shipInfos;

import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import com.alessiocascini.battleship.client.model.ShipInfo;
import java.awt.event.*;
import java.util.List;

/**
 * Listener responsible for handling clicks on the placement grid. It validates if a ship can be
 * placed at the selected coordinates based on bounds, overlaps, and whether the ship type has
 * already been used.
 *
 * @author Alessio Cascini
 */
public class PlacementCellListener implements ActionListener {
  private final PlacementHandler handler;
  private final List<Ship> ships;
  private final int row, col;

  /**
   * Constructs a listener for a specific cell in the grid.
   *
   * @param handler The handler to interact with the UI
   * @param ships The current list of placed ships to update
   * @param row The row index of the cell associated with this listener
   * @param col The column index of the cell associated with this listener
   */
  public PlacementCellListener(PlacementHandler handler, List<Ship> ships, int row, int col) {
    this.handler = handler;
    this.ships = ships;
    this.row = row;
    this.col = col;
  }

  /**
   * Invoked when a grid cell is clicked. Triggers the placement logic and shows an error message if
   * the placement is invalid.
   *
   * @param e The action event
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    final String errorMessage = handleCellClick();
    if (errorMessage != null) handler.showMessage(errorMessage);
  }

  /**
   * Internal logic to validate and process ship placement. Checks performed: 1. If the ship type is
   * already placed. 2. If the ship fits within the grid boundaries. 3. If the ship overlaps with
   * any existing ships.
   *
   * @return A string containing the error message, or null if placement is successful.
   */
  private String handleCellClick() {
    final ShipInfo ship = shipInfos[handler.getSelectedShipIndex()];

    // Check if a ship of the same type has already been placed
    if (ships.stream().anyMatch(s -> s.info().equals(ship))) return "Ship already placed!";

    final boolean horizontal = handler.getSelectedOrientationIndex() == 0;

    // Boundary check
    if (horizontal ? col + ship.size() > gridSize : row + ship.size() > gridSize)
      return "Ship goes out of bounds!";

    final Cell[] coords = new Cell[ship.size()];

    // Overlap check and coordinate generation
    for (int i = 0; i < ship.size(); i++) {
      final Cell current = horizontal ? new Cell(row, col + i) : new Cell(row + i, col);

      if (ships.stream().anyMatch(s -> List.of(s.cells()).contains(current)))
        return "Ship overlaps another ship!";
      coords[i] = current;
    }

    // If all checks pass, notify the UI and add the ship to the list
    handler.highlightShipCells(new Ship(ship, coords));
    ships.add(new Ship(ship, coords));

    return null;
  }
}
