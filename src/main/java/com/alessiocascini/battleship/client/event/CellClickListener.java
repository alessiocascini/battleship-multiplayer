package com.alessiocascini.battleship.client.event;

import static com.alessiocascini.battleship.client.ui.ShipPlacementUI.gridSize;
import static com.alessiocascini.battleship.client.ui.ShipPlacementUI.ships;

import com.alessiocascini.battleship.client.model.Ship;
import java.awt.event.*;
import java.util.HashMap;

public class CellClickListener implements ActionListener {
  private final CellClickHandler handler;
  private final HashMap<Ship, int[][]> positions;
  private final int row, col;

  public CellClickListener(
      CellClickHandler handler, HashMap<Ship, int[][]> positions, int row, int col) {
    this.handler = handler;
    this.positions = positions;
    this.row = row;
    this.col = col;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String errorMessage = handleCellClick();
    if (errorMessage != null) handler.showMessage(errorMessage);
  }

  private String handleCellClick() {
    final Ship ship = ships[handler.getSelectedShipIndex()];

    if (positions.containsKey(ship)) return "Ship already placed!";

    final boolean horizontal = handler.getSelectedOrientationIndex() == 0;

    if (horizontal ? col + ship.size() > gridSize : row + ship.size() > gridSize)
      return "Ship goes out of bounds!";

    final int[][] coords = new int[ship.size()][2];

    for (int i = 0; i < ship.size(); i++) {
      int r = row + (horizontal ? 0 : i);
      int c = col + (horizontal ? i : 0);

      for (int[][] position : positions.values())
        for (int[] cell : position)
          if (cell[0] == r && cell[1] == c) return "Ship overlaps another ship!";

      coords[i] = new int[] {r, c};
    }

    handler.highlightShipCells(ship, coords);
    positions.put(ship, coords);

    return null;
  }
}
