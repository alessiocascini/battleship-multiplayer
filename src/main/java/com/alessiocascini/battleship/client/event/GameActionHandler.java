package com.alessiocascini.battleship.client.event;

public interface GameActionHandler {
  void showMessage(String message);

  void processPlayerMove(int row, int col, int[][] result);

  void processOpponentMove(int[] opponentMove, int[][] opponentResult);
}
