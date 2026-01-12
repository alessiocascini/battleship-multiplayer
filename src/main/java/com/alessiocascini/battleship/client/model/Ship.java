package com.alessiocascini.battleship.client.model;

/**
 * Represents a placed ship on the game board. This record combines the ship's type information with
 * its physical coordinates on the grid.
 *
 * @param info The {@link ShipInfo} containing name and size
 * @param cells An array of {@link Cell} objects representing the ship's position
 * @author Alessio Cascini
 */
public record Ship(ShipInfo info, Cell[] cells) {

  /**
   * Converts the ship's cell coordinates into a primitive 2D integer array. This format is
   * typically used for network transmission to the server.
   *
   * @return A 2D array where each element is a [row, col] pair
   */
  public int[][] getCellsAsArray() {
    int[][] cells = new int[this.cells.length][2];
    for (int i = 0; i < this.cells.length; i++)
      cells[i] = new int[] {this.cells[i].row(), this.cells[i].col()};
    return cells;
  }
}
