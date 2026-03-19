# Distributed System Games

This repository contains two Java desktop applications built to demonstrate different distributed systems patterns:

- `DictionaryGame`: a multi-client dictionary service over TCP sockets.
- `WhiteBoardGame`: a collaborative whiteboard built on Java RMI callbacks.

The repo was reorganized from older coursework submission folders into cleaner top-level game directories. The current source of truth is the code under `DictionaryGame/` and `WhiteBoardGame/`.

## Repository Layout

```text
Distributed-System-Games/
├── DictionaryGame/
│   ├── lib/
│   ├── src/
│   ├── .classpath
│   ├── .project
│   └── README.md
├── WhiteBoardGame/
│   ├── lib/
│   ├── save/
│   ├── src/
│   └── README.md
├── LICENSE
└── README.md
```

## Projects

### DictionaryGame

`DictionaryGame` is a client-server dictionary application using raw TCP sockets and JSON messages. A Swing-based server manages the shared dictionary file and handles multiple clients concurrently through a custom thread pool. Clients connect through a Swing UI and can query, add, delete, append meanings, and update meanings for dictionary entries.

See `DictionaryGame/README.md` for setup and run instructions.

### WhiteBoardGame

`WhiteBoardGame` is a shared whiteboard system using Java RMI. One user acts as the manager, hosts the session, approves join requests, and can save or load whiteboard state. Guests join remotely, draw on the same canvas, and exchange chat messages in real time.

See `WhiteBoardGame/README.md` for setup and run instructions.

## Common Requirements

- JDK 8 or newer
- A desktop environment capable of running Swing
- Gson 2.10.1, already included under each game's `lib/` directory

## How To Work With This Repo

Each game is self-contained and can be compiled independently. The projects were originally developed in an IDE-centric workflow, so Eclipse and IntelliJ metadata are present, but both games can also be built from the command line with `javac` and run with `java`.

Recommended order if you are exploring the repository for the first time:

1. Start with `DictionaryGame/README.md` for the socket-based example.
2. Then review `WhiteBoardGame/README.md` for the RMI-based example.
3. Compare the two designs to see how the communication model changes the server, client, and synchronization logic.

## What Each Project Demonstrates

- TCP request/response messaging with a shared persistent data store
- Java RMI remote interfaces and callback-based updates
- Swing GUI development for both client and host applications
- Concurrency primitives such as thread-safe collections, atomics, and worker pools
- State synchronization for multi-user desktop applications

## License

The repository is distributed under the terms in `LICENSE`.
