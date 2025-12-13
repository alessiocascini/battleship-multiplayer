package frontend;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Prepare extends JFrame {
  private static final int SIZE = 4; // Should be 10 for production
  private static final Ship[] SHIPS = {
    new Ship("Ship A", 2), new Ship("Ship B", 2), new Ship("Ship C", 3)
  };

  private final JPanel gridPanel;
  private final JComboBox<String> shipSelector;
  private final JComboBox<String> orientationSelector;

  private final HashMap<Ship, int[][]> positions = new HashMap<>();

  public Prepare() {
    super("Place your ships");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(400, 600);
    setLayout(new BorderLayout());

    gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
    for (int i = 0; i < SIZE * SIZE; i++) {
      final JButton button = new JButton();
      final int row = i / SIZE;
      final int col = i % SIZE;
      button.addActionListener(_ -> cellClicked(row, col));
      gridPanel.add(button);
    }

    JPanel controlPanel = new JPanel(new GridLayout(4, 1));

    shipSelector = new JComboBox<>();
    for (Ship ship : SHIPS) shipSelector.addItem(ship.name + " (size: " + ship.size + ")");

    orientationSelector = new JComboBox<>(new String[] {"Horizontal", "Vertical"});

    JButton confirmButton = new JButton("Confirm");
    confirmButton.addActionListener(_ -> connectToServer());

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(
        _ -> {
          positions.clear();
          for (Component comp : gridPanel.getComponents()) comp.setBackground(null);
        });

    controlPanel.add(shipSelector);
    controlPanel.add(orientationSelector);
    controlPanel.add(confirmButton);
    controlPanel.add(resetButton);

    add(gridPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Prepare::new);
  }

  private void connectToServer() {
    if (positions.size() == SHIPS.length) {
      final int[][][] shipPositions = new int[SHIPS.length][][];
      for (int i = 0; i < SHIPS.length; i++) shipPositions[i] = positions.get(SHIPS[i]);

      try (Socket socket = new Socket("localhost", 5000);
          BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
        out.writeObject(shipPositions);

        int response = Integer.parseInt(in.readLine());
        if (response == 0 || response == 1) {
          this.dispose();
          new Game(shipPositions, response);
        } else JOptionPane.showMessageDialog(this, "Unexpected response from server: " + response);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage());
      }

    } else JOptionPane.showMessageDialog(this, "Place all ships before confirming!");
  }

  private void cellClicked(final int row, final int col) {
    final Ship ship = SHIPS[shipSelector.getSelectedIndex()];

    if (!positions.containsKey(ship)) {
      final boolean horizontal = orientationSelector.getSelectedIndex() == 0;

      if (!(horizontal ? col + ship.size > SIZE : row + ship.size > SIZE)) {
        final int[][] coords = new int[ship.size][2];

        for (int i = 0; i < ship.size; i++) {
          coords[i][0] = row + (horizontal ? 0 : i);
          coords[i][1] = col + (horizontal ? i : 0);

          gridPanel.getComponent(coords[i][0] * SIZE + coords[i][1]).setBackground(Color.GRAY);
        }
        positions.put(ship, coords);
      } else JOptionPane.showMessageDialog(this, "Ship out of bounds!");
    } else JOptionPane.showMessageDialog(this, "Ship already placed!");
  }

  private record Ship(String name, int size) {}
}
