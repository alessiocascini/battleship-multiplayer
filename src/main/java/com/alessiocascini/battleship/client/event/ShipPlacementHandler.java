package com.alessiocascini.battleship.client.event;

import com.alessiocascini.battleship.client.model.Ship;

public interface ShipPlacementHandler {
  int getSelectedShipIndex();

  int getSelectedOrientationIndex();

  void showMessage(String message);

  void highlightShipCells(Ship ship);

  void proceedToGameUI(boolean isFirstPlayer);
}
