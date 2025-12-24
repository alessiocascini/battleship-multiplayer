package com.alessiocascini.battleship.client.event;

import com.alessiocascini.battleship.client.model.Ship;

public interface CellClickHandler {
  int getSelectedShipIndex();

  int getSelectedOrientationIndex();

  void showMessage(String message);

  void highlightShipCells(Ship ship, int[][] coords);
}
