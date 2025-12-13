package frontend;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

import static frontend.Prepare.SIZE;

public class Game extends JFrame {
  private final int[][][] shipPositions;

  private final JPanel playerPanel = new JPanel(new GridLayout(SIZE, SIZE));
  private final JPanel opponentPanel = new JPanel(new GridLayout(SIZE, SIZE));

  private final Socket socket;

  public Game(int[][][] shipPositions, Socket socket) {
    super("Game Started");

    this.shipPositions = shipPositions;
    this.socket = socket;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 800);
    setLayout(new GridLayout(2, 1));

    for (int i = 0; i < SIZE * SIZE; i++) {
      final JButton button = new JButton();
      final int row = i / SIZE;
      final int col = i % SIZE;
      button.addActionListener(_ -> cellClicked(row, col));
      opponentPanel.add(button);
    }

    for (int i = 0; i < SIZE * SIZE; i++) {
      final JButton button = new JButton();
      final int row = i / SIZE;
      final int col = i % SIZE;

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

  public static void main(String[] args) {
    int[][][] shipPositions = {
      {{0, 0}, {0, 1}},
      {{3, 1}, {3, 2}},
      {{1, 1}, {1, 2}, {1, 3}}
    };
    SwingUtilities.invokeLater(() -> new Game(shipPositions, null));
  }

  private void cellClicked(int row, int col) {}
}
