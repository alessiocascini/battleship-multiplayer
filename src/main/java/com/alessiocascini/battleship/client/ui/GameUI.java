package com.alessiocascini.battleship.client.ui;

import static com.alessiocascini.battleship.client.ui.PlacementUI.gridSize;

import com.alessiocascini.battleship.client.event.GameActionHandler;
import com.alessiocascini.battleship.client.event.GameCellListener;
import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * The main gameplay interface. Displays two grids: the opponent's grid (interactive for attacking)
 * and the player's grid (read-only, showing own ships and opponent's hits).
 *
 * @author Alessio Cascini
 */
public class GameUI extends JFrame implements GameActionHandler {
  /** Panel representing the opponent's sea where the player can strike */
  private final JPanel opponentPanel = new JPanel(new GridLayout(gridSize, gridSize));

  /** Panel representing the player's sea showing their fleet and hits taken */
  private final JPanel playerPanel = new JPanel(new GridLayout(gridSize, gridSize));

  /**
   * Constructs the game interface, initializes the grids, and renders the player's ships.
   *
   * @param shipPositions The list of ships placed by the player in the previous phase
   * @param isFirstPlayer Boolean flag indicating if this client starts the game
   */
  public GameUI(List<Ship> shipPositions, boolean isFirstPlayer) {
    super("Game Started");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 800);
    setLayout(new GridLayout(2, 1));

    // Initialize the Opponent's Grid (Top)
    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;
      // Each button sends a move to the server via GameCellListener
      button.addActionListener(new GameCellListener(this, isFirstPlayer, row, col));
      opponentPanel.add(button);
    }

    // Initialize the Player's Grid (Bottom)
    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;

      // Draw the player's ships on their own grid
      for (Ship ship : shipPositions)
        for (Cell cell : ship.cells())
          if (cell.row() == row && cell.col() == col) button.setBackground(Color.GRAY);

      button.setEnabled(false); // Player cannot click on their own grid
      playerPanel.add(button);
    }

    // Layout configuration with labels
    final JPanel opponentContainer = new JPanel(new BorderLayout());
    opponentContainer.add(new JLabel("Opponent's Grid", SwingConstants.CENTER), BorderLayout.NORTH);
    opponentContainer.add(opponentPanel, BorderLayout.CENTER);

    final JPanel playerContainer = new JPanel(new BorderLayout());
    playerContainer.add(new JLabel("Your Grid", SwingConstants.CENTER), BorderLayout.NORTH);
    playerContainer.add(playerPanel, BorderLayout.CENTER);

    add(opponentContainer);
    add(playerContainer);

    setVisible(true);
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message);
  }

  /**
   * Updates the UI based on the result of a move. Logic: - 0 elements: Miss (Blue) - 1 element: Hit
   * (Red) - >1 elements: Ship Sunk (Black) - Sentinel [-1, -1]: Game Over
   *
   * @param isPlayerMove True if the move was made by this client, false if by opponent
   * @param row The row of the move
   * @param col The column of the move
   * @param result Data from the server indicating the outcome
   */
  @Override
  public void processMove(boolean isPlayerMove, int row, int col, int[][] result) {
    if (result.length <= 1) {
      // Handle Miss or Hit
      JOptionPane.showMessageDialog(
          this,
          (isPlayerMove ? "You" : "Opponent") + (result.length == 0 ? " missed!" : " hit a ship!"));

      (isPlayerMove ? opponentPanel : playerPanel)
          .getComponent(row * gridSize + col)
          .setBackground(result.length == 0 ? Color.BLUE : Color.RED);
    } else {
      // Handle Sunken Ship
      JOptionPane.showMessageDialog(this, (isPlayerMove ? "You" : "Opponent") + " sunk a ship!");

      for (int[] pos : result)
        if (pos[0] != -1 && pos[1] != -1)
          (isPlayerMove ? opponentPanel : playerPanel)
              .getComponent(pos[0] * gridSize + pos[1])
              .setBackground(Color.BLACK);

      // Check for victory/defeat sentinel value
      if (result[0][0] == -1 && result[0][1] == -1) {
        JOptionPane.showMessageDialog(this, (isPlayerMove ? "You" : "Opponent") + " won the game!");
        // Disable further interactions
        for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
      }
    }

    // Disable the button for the move just made to prevent duplicates
    if (isPlayerMove) opponentPanel.getComponent(row * gridSize + col).setEnabled(false);
  }
}
