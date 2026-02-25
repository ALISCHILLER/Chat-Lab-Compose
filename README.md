<!--
***
***
***
This README is generated from a template. To edit it, open the `.github/README.md.template` file.
***
***
***
-->
<p align="center">
  <img src="docs/logo.png" alt="ChatLab Logo" width="150" />
</p>

# 🧪 ChatLab

**ChatLab** is an Android application for protocol testing and performance analysis. It allows you to create different communication profiles (WebSocket, MQTT, Socket.IO, SignalR), connect to endpoints, send/receive messages, and run predefined test scenarios to measure performance.

It serves as a reference implementation for a modular, clean-architecture Android project.

---

## 🎯 Core Problem

When building real-time applications, choosing the right communication protocol is critical. In the real world, choosing between WebSocket, MQTT, Socket.IO, or SignalR is not just about features, but also about performance, reliability, and battery consumption under different network conditions.

This project aims to provide a standardized testbed for evaluating these protocols on a real Android device.

---

## 🏗️ Project Architecture

The project is designed based on the principles of **Clean Architecture** with the goal of maximum layer separation.

### Module Structure

| Module Group | Primary Responsibility |
| :--- | :--- |
| **`app`** | The main shell, entry point, and Navigation management. |
| **`core`** | **The heart of the system.** Contains business logic, domain models, and contracts. |
| **`feature`** | **User Interface (UI).** Each screen or feature (Settings, Lab, Chat) is a separate module. |
| **`protocol`** | **Interchangeable implementations** of communication protocols. |

### Golden Rules of Architecture

1.  **Complete Separation:** The `feature` layer **never** directly accesses the `protocol` or `core-storage` layers. All interactions are done through `core-data`.
2.  **Pure Core:** The `core-domain` module is completely independent of Android and external libraries.
3.  **Contract-Driven:** All protocols adhere to a single contract (`TransportContract`).
4.  **Single Source of Truth:** All settings are managed through `Profile`.

---


## 🛠️ Tech Stack

*   **Language:** 100% Kotlin
*   **UI:** Jetpack Compose (Single-Activity)
*   **Architecture:** Clean Architecture, MVI (State/Event/Effect)
*   **Asynchrony:** Coroutines + Flow
*   **Dependency Injection (DI):** Koin
*   **Storage:** Room
*   **Networking:** OkHttp, Ktor, Paho MQTT, Socket.IO Client, SignalR Client

---

## 🚀 Quick Start

1.  **Create a Profile:** Go to the **Settings** screen and create a new profile for your desired protocol (e.g., `WS_OKHTTP`) by clicking the `+` button.

    *   **Recommended Endpoint for Testing:** `wss://echo.websocket.events`
2.  **Activate the Profile:** After creating the profile, click the **Apply** button.
3.  **Test the Connection:** Go to the **Connect** screen. Here you should see the name of the active profile and the connection status (`Idle`). Click the **Connect** button. The status should change to `Connected`.
4.  **Send a Manual Message:** Go to the **Chat** screen. Send a message. If you are using the `echo` server, you should receive the same message as a response.
5.  **Test Offline Mode:** On the **Chat** screen, turn on the **Simulate Offline** option. Send a few messages. The messages should be displayed with a different color and as "Queued". Then turn off `Simulate Offline`. The messages should be sent automatically.
6.  **Run a Scenario:** Go to the **Lab** screen and run one of the default scenarios (e.g., `Stable`). After completion, a summary of the results will be displayed.

---
