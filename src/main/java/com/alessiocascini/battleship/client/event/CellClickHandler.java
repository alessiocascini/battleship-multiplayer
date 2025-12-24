package com.alessiocascini.battleship.client.event;

public interface CellClickHandler {
  int getSelectedShipIndex();

  int getSelectedOrientationIndex();

  void showMessage(String message);
}
