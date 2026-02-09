# 📊 ChatLab: A Personal Laboratory for Real-time Communication Protocols

**ChatLab** is not a demo application; it is a **personal engineering tool** for testing, comparing, and selecting real-time communication protocols in Android projects. This project allows you to simulate complex network scenarios and choose the best protocol for your project's needs based on **real, reproducible data**.

## 🎯 Philosophy and Core Purpose

In the real world, choosing between WebSocket, MQTT, Socket.IO, or SignalR is not a simple decision. Each behaves differently under specific conditions (poor network, battery consumption, delivery guarantees). ChatLab is built to answer these questions:

1.  **Profile-Centric:** Create a complete configuration profile for each scenario and protocol.
2.  **Scientific Comparison:** Fairly compare the performance of different protocols by running identical scenarios.
3.  **Standardized Output:** Receive test results in JSON and CSV formats for analysis and reporting.
4.  **Extractable Code:** Key modules, such as the `outbox` (offline queue) and protocol implementations, are designed to be directly usable in your real-world projects.

---

## ✨ Key Features

*   **True Multi-module Architecture:** Complete separation of layers for easy maintenance and code extraction.
*   **Support for 5 Industrial Protocols:**
    *   WebSocket (with OkHttp and Ktor)
    *   MQTT (with Eclipse Paho)
    *   Socket.IO
    *   SignalR
*   **Durable Outbox (Offline Queue):** Guarantees message delivery after connection is restored, even after an application restart.
*   **Retry Engine:** With `Exponential Backoff` logic for smart handling of sending errors.
*   **Scenario-Based Laboratory:**
    *   **Stable:** Stability testing under ideal conditions.
    *   **Intermittent:** Testing under frequent, short network disconnections.
    *   **OfflineBurst:** Testing the sending of a large volume of messages after a long offline period.
    *   **Lossy:** Simulating an unreliable network with packet loss.
    *   **LoadBurst:** Stress testing the system under sudden high traffic.
*   **Scientific Reproducibility:** All scenarios are reproducible using a `seed`.
*   **Standardized and Analyzable Output:** Includes `profile_used.json`, `run_events.csv`, and `metrics_summary.json`.

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

## 📈 Analyzing the Results

After each scenario run, an **Export Bundle** is saved to your device's internal storage. This bundle includes three key files:

1.  **`profile_used.json`**: A complete copy of the profile used in the test.
2.  **`run_events.csv`**: A timeline of all important events (connection, disconnection, sending, receiving, errors) with precise timestamps.
3.  **`metrics_summary.json`**: A summary of the most important performance metrics, including:
    *   `successRatePercent`: The percentage of successful messages.
    *   `latencyP95Ms`: The 95th percentile latency.
    *   `throughputMsgPerSec`: The throughput (number of messages per second).

These files allow you to compare the performance of different protocols under the same conditions in a **scientific and defensible** manner.
