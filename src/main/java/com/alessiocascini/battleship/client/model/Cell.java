package com.alessiocascini.battleship.client.model;

/**
 * Represents a single coordinate on the battleship grid. This is a data carrier used to identify a
 * specific position by its row and column.
 *
 * @param row The vertical index on the grid
 * @param col The horizontal index on the grid
 * @author Alessio Cascini
 */
public record Cell(int row, int col) {}
