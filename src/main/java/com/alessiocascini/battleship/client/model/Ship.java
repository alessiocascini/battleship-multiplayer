package com.alessiocascini.battleship.client.model;

public record Ship(ShipInfo info, Cell[] cells) {
  public int[][] getCellsAsArray() {
    int[][] cells = new int[this.cells.length][2];
    for (int i = 0; i < this.cells.length; i++)
      cells[i] = new int[] {this.cells[i].row(), this.cells[i].col()};
    return cells;
  }
}
