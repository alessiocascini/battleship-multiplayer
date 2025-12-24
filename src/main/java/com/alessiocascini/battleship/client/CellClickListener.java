package com.alessiocascini.battleship.client;

import static com.alessiocascini.battleship.client.ShipPlacementUI.gridSize;
import static com.alessiocascini.battleship.client.ShipPlacementUI.ships;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;

public class CellClickListener implements ActionListener {
  private final CellClickHandler handler;
  private final HashMap<Ship, int[][]> positions;
  private final JPanel gridPanel;
  private final int row, col;

  public CellClickListener(
      CellClickHandler handler,
      HashMap<Ship, int[][]> positions,
      JPanel gridPanel,
      int row,
      int col) {
    this.handler = handler;
    this.positions = positions;
    this.gridPanel = gridPanel;
    this.row = row;
    this.col = col;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Ship ship = ships[handler.getSelectedShipIndex()];

    if (!positions.containsKey(ship)) {
      final boolean horizontal = handler.getSelectedOrientationIndex() == 0;

      if (!(horizontal ? col + ship.size() > gridSize : row + ship.size() > gridSize)) {
        final int[][] coords = new int[ship.size()][2];

        for (int i = 0; i < ship.size(); i++) {
          int r = row + (horizontal ? 0 : i);
          int c = col + (horizontal ? i : 0);

          for (int[][] position : positions.values())
            for (int[] cell : position)
              if (cell[0] == r && cell[1] == c) {
                handler.showMessage("Ship overlap!");
                return;
              }

          coords[i][0] = r;
          coords[i][1] = c;
        }

        for (int i = 0; i < ship.size(); i++)
          gridPanel.getComponent(coords[i][0] * gridSize + coords[i][1]).setBackground(Color.GRAY);

        positions.put(ship, coords);
      } else handler.showMessage("Ship out of bounds!");
    } else handler.showMessage("Ship already placed!");
  }
}
