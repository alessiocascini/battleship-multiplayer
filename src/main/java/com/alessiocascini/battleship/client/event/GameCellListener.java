package com.alessiocascini.battleship.client.event;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class GameCellListener implements ActionListener {
  private static boolean isFirstTurn = true;
  private final GameActionHandler handler;
  private final boolean isFirstPlayer;
  private final int row, col;

  public GameCellListener(GameActionHandler handler, boolean isFirstPlayer, int row, int col) {
    this.handler = handler;
    this.isFirstPlayer = isFirstPlayer;
    this.row = row;
    this.col = col;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    new Thread(() -> cellClicked(row, col)).start();
  }

  private void cellClicked(int row, int col) {
    try (Socket socket = new Socket("localhost", 5000);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

      out.writeObject(isFirstPlayer);
      boolean isYourTurn = (Boolean) in.readObject();

      if (isYourTurn) {
        if (isFirstTurn && !isFirstPlayer) {
          int[] opponentMove = (int[]) in.readObject();
          int[][] opponentResult = (int[][]) in.readObject();
          handler.processMove(false, opponentMove[0], opponentMove[1], opponentResult);

          isFirstTurn = false;
        }

        out.writeObject(new int[] {row, col});
        int[][] result = (int[][]) in.readObject();
        handler.processMove(true, row, col, result);

        int[] opponentMove = (int[]) in.readObject();
        int[][] opponentResult = (int[][]) in.readObject();
        handler.processMove(false, opponentMove[0], opponentMove[1], opponentResult);
      } else handler.showMessage("It's not your turn!");
    } catch (Exception e) {
      handler.showMessage("Error communicating with server: " + e.getMessage());
    }
  }
}
