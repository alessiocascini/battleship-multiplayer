package com.alessiocascini.battleship.client;

public interface CellClickHandler {
  int getSelectedShipIndex();

  int getSelectedOrientationIndex();

  void showMessage(String message);
}
