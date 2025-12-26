package com.alessiocascini.battleship.client.event;

import static com.alessiocascini.battleship.client.ui.PlacementUI.gridSize;
import static com.alessiocascini.battleship.client.ui.PlacementUI.shipInfos;

import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import com.alessiocascini.battleship.client.model.ShipInfo;
import java.awt.event.*;
import java.util.List;

public class PlacementCellListener implements ActionListener {
  private final PlacementHandler handler;
  private final List<Ship> ships;
  private final int row, col;

  public PlacementCellListener(PlacementHandler handler, List<Ship> ships, int row, int col) {
    this.handler = handler;
    this.ships = ships;
    this.row = row;
    this.col = col;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String errorMessage = handleCellClick();
    if (errorMessage != null) handler.showMessage(errorMessage);
  }

  private String handleCellClick() {
    final ShipInfo ship = shipInfos[handler.getSelectedShipIndex()];

    if (ships.stream().anyMatch(s -> s.info().equals(ship))) return "Ship already placed!";

    final boolean horizontal = handler.getSelectedOrientationIndex() == 0;

    if (horizontal ? col + ship.size() > gridSize : row + ship.size() > gridSize)
      return "Ship goes out of bounds!";

    final Cell[] coords = new Cell[ship.size()];

    for (int i = 0; i < ship.size(); i++) {
      final Cell current = horizontal ? new Cell(row, col + i) : new Cell(row + i, col);
      if (ships.stream().anyMatch(s -> List.of(s.cells()).contains(current)))
        return "Ship overlaps another ship!";
      coords[i] = current;
    }

    handler.highlightShipCells(new Ship(ship, coords));
    ships.add(new Ship(ship, coords));

    return null;
  }
}
