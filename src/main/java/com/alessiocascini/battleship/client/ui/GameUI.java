package com.alessiocascini.battleship.client.ui;

import static com.alessiocascini.battleship.client.ui.PlacementUI.gridSize;

import com.alessiocascini.battleship.client.event.GameActionHandler;
import com.alessiocascini.battleship.client.event.GameCellListener;
import com.alessiocascini.battleship.client.model.Cell;
import com.alessiocascini.battleship.client.model.Ship;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class GameUI extends JFrame implements GameActionHandler {
  private final JPanel playerPanel = new JPanel(new GridLayout(gridSize, gridSize));
  private final JPanel opponentPanel = new JPanel(new GridLayout(gridSize, gridSize));

  public GameUI(List<Ship> shipPositions, boolean isFirstPlayer) {
    super("Game Started");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 800);
    setLayout(new GridLayout(2, 1));

    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;
      button.addActionListener(new GameCellListener(this, isFirstPlayer, row, col));
      opponentPanel.add(button);
    }

    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;

      for (Ship ship : shipPositions)
        for (Cell cell : ship.cells())
          if (cell.row() == row && cell.col() == col) button.setBackground(Color.GRAY);

      button.setEnabled(false);
      playerPanel.add(button);
    }

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

  @Override
  public void processPlayerMove(int row, int col, int[][] result) {
    SwingUtilities.invokeLater(
        () -> {
          switch (result.length) {
            case 0 -> {
              JOptionPane.showMessageDialog(this, "Miss!");
              opponentPanel.getComponent(row * gridSize + col).setBackground(Color.BLUE);
            }
            case 1 -> {
              JOptionPane.showMessageDialog(this, "Hit!");
              opponentPanel.getComponent(row * gridSize + col).setBackground(Color.RED);
            }
            default -> {
              JOptionPane.showMessageDialog(this, "You sunk a ship!");
              for (int[] pos : result)
                if (!(pos[0] == -1 && pos[1] == -1))
                  opponentPanel.getComponent(pos[0] * gridSize + pos[1]).setBackground(Color.BLACK);

              if (result[0][0] == -1 && result[0][1] == -1) {
                JOptionPane.showMessageDialog(this, "You won!");
                for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
              }
            }
          }

          opponentPanel.getComponent(row * gridSize + col).setEnabled(false);
        });
  }

  @Override
  public void processOpponentMove(int[] opponentMove, int[][] opponentResult) {
    switch (opponentResult.length) {
      case 0 -> {
        JOptionPane.showMessageDialog(
            this, "Opponent missed at (" + opponentMove[0] + ", " + opponentMove[1] + ")!");
        playerPanel
            .getComponent(opponentMove[0] * gridSize + opponentMove[1])
            .setBackground(Color.BLUE);
      }
      case 1 -> {
        JOptionPane.showMessageDialog(
            this, "Opponent hit your ship at (" + opponentMove[0] + ", " + opponentMove[1] + ")!");
        playerPanel
            .getComponent(opponentMove[0] * gridSize + opponentMove[1])
            .setBackground(Color.RED);
      }
      default -> {
        JOptionPane.showMessageDialog(this, "Opponent sunk your ship!");
        for (int[] pos : opponentResult)
          playerPanel.getComponent(pos[0] * gridSize + pos[1]).setBackground(Color.BLACK);

        if (opponentMove[0] == -1 && opponentMove[1] == -1) {
          JOptionPane.showMessageDialog(this, "You lost!");
          for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
        }
      }
    }
  }
}
