package frontend;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import static frontend.Prepare.SIZE;

public class Game extends JFrame {
  private final JPanel playerPanel = new JPanel(new GridLayout(SIZE, SIZE));
  private final JPanel opponentPanel = new JPanel(new GridLayout(SIZE, SIZE));

  private final int[][][] shipPositions;
  private final boolean isFirstPlayer;

  public Game(int[][][] shipPositions, boolean isFirstPlayer) {
    super("Game Started");

    this.shipPositions = shipPositions;
    this.isFirstPlayer = isFirstPlayer;

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

  private void cellClicked(int row, int col) {

    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      out.writeObject(isFirstPlayer);
      boolean isYourTurn = (Boolean) in.readObject();

      if (isYourTurn) {
        out.writeObject(new int[] {row, col});
        String result = (String) in.readObject();
        JOptionPane.showMessageDialog(this, "Result of your attack: " + result);
      } else JOptionPane.showMessageDialog(this, "It's not your turn!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
