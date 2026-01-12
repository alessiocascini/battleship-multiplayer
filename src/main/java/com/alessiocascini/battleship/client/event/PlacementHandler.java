package com.alessiocascini.battleship.client.event;

import com.alessiocascini.battleship.client.model.Ship;

/**
 * Interface defining the necessary operations for handling the ship placement phase. It acts as a
 * contract between the UI and the event listeners to manage user selections and transitions.
 *
 * @author Alessio Cascini
 */
public interface PlacementHandler {

  /**
   * Retrieves the index of the currently selected ship from the UI.
   *
   * @return The index of the selected ship in the fleet list
   */
  int getSelectedShipIndex();

  /**
   * Retrieves the selected orientation (e.g., Horizontal or Vertical).
   *
   * @return The index representing the chosen orientation
   */
  int getSelectedOrientationIndex();

  /**
   * Displays a feedback message to the user, such as errors or instructions.
   *
   * @param message The string content to be displayed
   */
  void showMessage(String message);

  /**
   * Updates the UI to visually represent a ship that has been successfully placed.
   *
   * @param ship The {@link Ship} object containing the cells to be highlighted
   */
  void highlightShipCells(Ship ship);

  /**
   * Transitions the application from the placement screen to the main game screen.
   *
   * @param isFirstPlayer Boolean flag to identify if this client is Player 1 or Player 2
   */
  void proceedToGameUI(boolean isFirstPlayer);
}
