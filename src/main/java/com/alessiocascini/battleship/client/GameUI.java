package com.alessiocascini.battleship.client;

import static com.alessiocascini.battleship.client.ShipPlacementUI.gridSize;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class GameUI extends JFrame {
  private final JPanel playerPanel = new JPanel(new GridLayout(gridSize, gridSize));
  private final JPanel opponentPanel = new JPanel(new GridLayout(gridSize, gridSize));

  private final boolean isFirstPlayer;
  private boolean isFirstTurn = true;

  public GameUI(int[][][] shipPositions, boolean isFirstPlayer) {
    super("Game Started");

    this.isFirstPlayer = isFirstPlayer;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 800);
    setLayout(new GridLayout(2, 1));

    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;
      button.addActionListener(_ -> new Thread(() -> cellClicked(row, col)).start());
      opponentPanel.add(button);
    }

    for (int i = 0; i < gridSize * gridSize; i++) {
      final JButton button = new JButton();
      final int row = i / gridSize;
      final int col = i % gridSize;

      for (int[][] ship : shipPositions)
        for (int[] pos : ship) if (pos[0] == row && pos[1] == col) button.setBackground(Color.GRAY);

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

  private void cellClicked(int row, int col) {
    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      out.writeObject(isFirstPlayer);
      boolean isYourTurn = (Boolean) in.readObject();

      if (isYourTurn) {
        if (isFirstTurn && !isFirstPlayer) {
          processOpponentMove(in);
          isFirstTurn = false;
        }

        out.writeObject(new int[] {row, col});
        int[][] result = (int[][]) in.readObject();

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
                      opponentPanel
                          .getComponent(pos[0] * gridSize + pos[1])
                          .setBackground(Color.BLACK);

                  if (result[0][0] == -1 && result[0][1] == -1) {
                    JOptionPane.showMessageDialog(this, "You won!");
                    for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
                  }
                }
              }

              opponentPanel.getComponent(row * gridSize + col).setEnabled(false);
            });

        processOpponentMove(in);
      } else JOptionPane.showMessageDialog(this, "It's not your turn!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processOpponentMove(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    int[] opponentMove = (int[]) in.readObject();
    int[][] opponentResult = (int[][]) in.readObject();

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
