# Fight the Bebra 🎮

A cooperative 2D Pixel Top-Down online game for two players. The project demonstrates high-performance client-server architecture, blending a responsive frontend with a secure, multithreaded backend.

## 🛠️ Tech Stack & Architecture

*   **Frontend (Client):** Godot 4.6 (GDScript) — handles 2D rendering, pixel-art UI, and local game loop.
*   **Backend (Server):** Java — handles network synchronization, lobby management, and core game logic.
*   **Database:** PostgreSQL/MySQL (via JDBC) — manages user authentication and high scores.
*   **Networking:** Custom TCP protocol (`ServerSocket` / `Socket`) with custom packet structuring and AES/XOR encryption for sensitive data.

## 🚀 Key Features

*   **Private Lobby System:** Create or join rooms via unique server-generated codes.
*   **Real-time Synchronization:** Server-authoritative sync for player positions, monster spawns, and power-ups.
*   **Co-op Revival Mechanic:** 15-second survival window to resurrect a fallen teammate.
*   **In-game Live Chat:** Real-time text communication in lobbies and during matches.
*   **Multithreaded Architecture:** Dedicated threads per client connection and active game rooms for seamless scalability.