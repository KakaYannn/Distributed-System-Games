# Distributed-System-Games

A collection of distributed system applications demonstrating different networking paradigms and concurrency patterns in Java.

## 📋 Overview

This project contains two fully-featured distributed applications built with Java, each showcasing different architectural approaches for networked systems:

1. **DictionaryGame** - A TCP socket-based multi-client dictionary service
2. **WhiteBoardGame** - A collaborative whiteboard application using Java RMI

## 🎮 Projects

### [DictionaryGame](./DictionaryGame/README.md)
A distributed dictionary application where multiple clients can connect to a centralized server to perform CRUD operations on a shared, persistent word dictionary.

- **Architecture**: TCP Sockets + JSON
- **Key Features**: Multi-threaded server, persistent JSON storage, custom thread pool
- **Use Case**: Demonstrates traditional client-server architecture and socket programming

### [WhiteBoardGame](./WhiteBoardGame/README.md)
A real-time collaborative whiteboard where a manager hosts a session and guests remotely join to draw together.

- **Architecture**: Java RMI with RMI callbacks
- **Key Features**: Real-time synchronization, collaborative drawing, user management
- **Use Case**: Demonstrates distributed objects and stateful peer-to-peer communication

## 🏗️ Directory Structure

```
Distributed-System-Games/
├── DictionaryGame/
│   ├── source_file/
│   │   ├── src/
│   │   │   ├── client/          # Client implementation
│   │   │   ├── server/          # Server implementation
│   │   │   └── dictionary.json  # Data file
│   │   └── lib/                 # Dependencies
│   └── README.md
│
├── WhiteBoardGame/
│   ├── COMP90015_A2_submitted_1638660/
│   │   ├── src/
│   │   │   ├── manager/         # Server/manager side
│   │   │   ├── guest/           # Client/guest side
│   │   │   ├── common/          # Shared components
│   │   │   ├── remote/          # RMI interfaces
│   │   │   ├── icon/            # UI resources
│   │   │   └── util/            # Utilities
│   │   └── lib/                 # Dependencies
│   └── README.md
│
└── README.md                    # This file
```

## 🔧 Common Requirements

- **Java**: JDK 8 or higher
- **Build Tool**: Maven or IDE with built-in compiler
- **IDE**: IntelliJ IDEA or Eclipse recommended

## 📚 Documentation

Each game includes its own detailed README with setup instructions, usage guide, and architecture documentation. Start here:

- [DictionaryGame README](./DictionaryGame/README.md) - TCP socket-based architecture
- [WhiteBoardGame README](./WhiteBoardGame/README.md) - RMI-based architecture

## 🎓 Educational Value

This project is suitable for:
- Learning distributed systems concepts
- Understanding different networking approaches (sockets vs RMI)
- Multi-threading and concurrency in Java
- GUI development with Swing
- Network protocol design

## 📝 License

See [LICENSE](./LICENSE) for details.

## 👨‍💻 Development Notes

Both applications were developed as academic coursework to demonstrate distributed system design patterns:

- **DictionaryGame**: Emphasizes traditional client-server architecture with custom thread pool management
- **WhiteBoardGame**: Showcases Java RMI for building distributed objects with real-time collaboration

For detailed architecture information, refer to individual README files for each game.
