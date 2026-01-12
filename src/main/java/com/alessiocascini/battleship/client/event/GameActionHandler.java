package com.alessiocascini.battleship.client.event;

public interface GameActionHandler {
  void showMessage(String message);

  void processMove(boolean isPlayerMove, int row, int col, int[][] result);
}
