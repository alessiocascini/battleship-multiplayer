# Battleship Multiplayer

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Socket](https://img.shields.io/badge/Networking-TCP%20Sockets-green)

A Java-based multiplayer implementation of the classic **Battleship** game. This project features a robust **Client-Server architecture** using Java Sockets for real-time communication and **Java Swing** for a responsive graphical user interface.

## 🚀 Features

- **Real-time Multiplayer:** Play against another player over a network using TCP Sockets.
- **Intuitive UI:** Fully functional graphical interface built with Java Swing.
- **Automated Placement Logic:** Smart validation for ship placement, preventing overlaps or out-of-bounds errors.
- **Turn-based Synchronization:** Server-side logic manages player turns and game state synchronization.
- **Modern Java Features:** Utilizes **Java Records** for clean data modeling.

## 📂 Project Structure

```text
src/com/alessiocascini/battleship/
├── client/
│   ├── event/   # ActionListeners & UI Handlers
│   ├── model/   # Data Records (Ship, Cell, ShipInfo)
│   └── ui/      # Swing Interfaces (PlacementUI, GameUI)
└── server/
    └── Server.java  # Game logic & Socket management
```

## 🛠️ Technical Stack

- **Language:** Java (JDK 17+).
- **Networking:** Java Sockets (TCP) for reliable data transmission.
- **Concurrency:** Multithreading to handle simultaneous client connections.
- **GUI Framework:** Java Swing / AWT.

## 🚦 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher.

### Running the Game

1. **Start the Server:** Run `Server.java`.
2. **Start the Clients:** Run `PlacementUI.java` twice (once for each player).
3. **Setup:** Place all ships on the grid and click **Confirm**.
4. **Play:** Once both players are connected, the game starts automatically.

> **Note on Networking:** By default, the client connects to `localhost`. To play across different networks, ensure the server's IP is correctly set in the client code and that port `5000` is open on the host's router.

## 🎮 Game Legend

| Status       | Color | Description                              |
| ------------ | ----- | ---------------------------------------- |
| 🌊 **Water** | Blue  | Your shot missed the target.             |
| 🔥 **Hit**   | Red   | You successfully hit an opponent's ship. |
| 💀 **Sunk**  | Black | The entire ship has been destroyed.      |
| 🚢 **Ship**  | Gray  | Your own ship's position.                |

## 🔮 Future Improvements

- **UI/UX Overhaul:** Replacing Swing components with custom graphics and animations.
- **Network Optimization:** Exploring **UDP** for faster state updates.
- **Code Refactoring:** Moving the `model` records into a shared library to be used by both Client and Server.
- **Media:** Adding gameplay videos and screenshots once assets are implemented.

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.
