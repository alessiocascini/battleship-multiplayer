package frontend;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import static frontend.ShipPlacementUI.SIZE;

public class GameUI extends JFrame {
  // Panels for player's and opponent's grid
  private final JPanel playerPanel = new JPanel(new GridLayout(SIZE, SIZE));
  private final JPanel opponentPanel = new JPanel(new GridLayout(SIZE, SIZE));

  private final boolean isFirstPlayer;
  private boolean isFirstTurn = true; // Track first turn for the second player

  public GameUI(int[][][] shipPositions, boolean isFirstPlayer) {
    super("Game Started");

    this.isFirstPlayer = isFirstPlayer;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 800);
    setLayout(new GridLayout(2, 1)); // Two main rows: opponent and player

    // Create buttons for opponent's grid
    for (int i = 0; i < SIZE * SIZE; i++) {
      final JButton button = new JButton();
      final int row = i / SIZE;
      final int col = i % SIZE;
      // Clicking a cell starts a new thread to handle the move
      button.addActionListener(_ -> new Thread(() -> cellClicked(row, col)).start());
      opponentPanel.add(button);
    }

    // Create buttons for player's grid and mark ship positions
    for (int i = 0; i < SIZE * SIZE; i++) {
      final JButton button = new JButton();
      final int row = i / SIZE;
      final int col = i % SIZE;

      // Mark ships on player's grid as gray
      for (int[][] ship : shipPositions)
        for (int[] pos : ship) if (pos[0] == row && pos[1] == col) button.setBackground(Color.GRAY);

      button.setEnabled(false); // Player cannot click their own grid
      playerPanel.add(button);
    }

    // Add labels and panels to separate containers
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

  // Handle a cell click on opponent's grid
  private void cellClicked(int row, int col) {
    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      // Send player info and get turn status
      out.writeObject(isFirstPlayer);
      boolean isYourTurn = (Boolean) in.readObject();

      if (isYourTurn) {
        // If second player, process opponent's move on first turn
        if (isFirstTurn && !isFirstPlayer) {
          processOpponentMove(in);
          isFirstTurn = false;
        }

        // Send chosen cell to server
        out.writeObject(new int[] {row, col});
        int[][] result = (int[][]) in.readObject();

        // Update UI based on result
        SwingUtilities.invokeLater(
            () -> {
              switch (result.length) {
                case 0 -> { // Miss
                  JOptionPane.showMessageDialog(this, "Miss!");
                  opponentPanel.getComponent(row * SIZE + col).setBackground(Color.BLUE);
                }
                case 1 -> { // Hit
                  JOptionPane.showMessageDialog(this, "Hit!");
                  opponentPanel.getComponent(row * SIZE + col).setBackground(Color.RED);
                }
                default -> { // Ship sunk
                  JOptionPane.showMessageDialog(this, "You sunk a ship!");
                  for (int[] pos : result)
                    if (!(pos[0] == -1 && pos[1] == -1))
                      opponentPanel.getComponent(pos[0] * SIZE + pos[1]).setBackground(Color.BLACK);

                  if (result[0][0] == -1 && result[0][1] == -1) { // Win condition
                    JOptionPane.showMessageDialog(this, "You win!");
                    for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
                  }
                }
              }

              opponentPanel
                  .getComponent(row * SIZE + col)
                  .setEnabled(false); // Disable clicked cell
            });

        processOpponentMove(in); // Process opponent's move after player's turn
      } else JOptionPane.showMessageDialog(this, "It's not your turn!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Update player's grid based on opponent's move
  private void processOpponentMove(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    int[] opponentMove = (int[]) in.readObject();
    int[][] opponentResult = (int[][]) in.readObject();

    switch (opponentResult.length) {
      case 0 -> { // Opponent miss
        JOptionPane.showMessageDialog(
            this, "Opponent missed at (" + opponentMove[0] + ", " + opponentMove[1] + ")!");
        playerPanel
            .getComponent(opponentMove[0] * SIZE + opponentMove[1])
            .setBackground(Color.BLUE);
      }
      case 1 -> { // Opponent hit
        JOptionPane.showMessageDialog(
            this, "Opponent hit your ship at (" + opponentMove[0] + ", " + opponentMove[1] + ")!");
        playerPanel.getComponent(opponentMove[0] * SIZE + opponentMove[1]).setBackground(Color.RED);
      }
      default -> { // Opponent sunk a ship
        JOptionPane.showMessageDialog(this, "Opponent sunk your ship!");
        for (int[] pos : opponentResult)
          playerPanel.getComponent(pos[0] * SIZE + pos[1]).setBackground(Color.BLACK);

        if (opponentMove[0] == -1 && opponentMove[1] == -1) { // Lose condition
          JOptionPane.showMessageDialog(this, "You lose!");
          for (Component comp : opponentPanel.getComponents()) comp.setEnabled(false);
        }
      }
    }
  }
}
