package com.alessiocascini.battleship.client.model;

/**
 * Metadata for a ship type. This record defines the static properties of a ship, such as its name
 * and the number of cells it occupies on the grid.
 *
 * @param name The display name of the ship (e.g., "Carrier")
 * @param size The number of grid cells the ship covers
 * @author Alessio Cascini
 */
public record ShipInfo(String name, int size) {}
