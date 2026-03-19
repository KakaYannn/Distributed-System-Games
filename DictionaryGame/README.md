# DictionaryGame

A distributed dictionary application using TCP sockets and JSON messaging. Demonstrates traditional client-server architecture with multi-threading and persistent data storage.

## 📖 Overview

DictionaryGame is a networked dictionary service where multiple clients can simultaneously connect to a central server to perform CRUD operations on a shared dictionary. The server maintains a persistent JSON file and handles multiple concurrent connections using a custom thread pool.

### Key Features

- **Multi-threaded Server**: Fixed-size thread pool serving concurrent clients
- **Persistent Storage**: Dictionary stored as JSON file with atomic write operations
- **Thread-safe Operations**: Uses `ConcurrentHashMap` and `AtomicInteger` for thread safety
- **GUI Clients**: Interactive Swing-based user interface
- **JSON Protocol**: Structured request/response messaging using GSON

## 🏗️ Architecture

### Communication Model

- **Transport**: TCP Sockets
- **Message Format**: JSON (GSON serialization)
- **Response Format**: Plain text ("success", "unsuccess", or query results)
- **Port**: Configurable (default: 1025-65535)

### Components

#### Server-Side

| Component | Role |
|-----------|------|
| **ServerLauncher** | Entry point; validates CLI arguments (port, dictionary file path) |
| **TCPInteractiveServer** | Core server managing thread pool, dictionary cache, client connections |
| **Connection** | Runnable handler for each client; processes requests and sends responses |
| **ThreadPool** | Custom implementation with configurable worker threads |
| **ServerUI** | GUI displaying connection status, dictionary statistics, and logs |

#### Client-Side

| Component | Role |
|-----------|------|
| **ClientLauncher** | Entry point; validates CLI arguments (server IP, port) |
| **TCPInteractiveClient** | Network handler; manages socket communication and request queueing |
| **ClientUI** | Swing GUI with input fields and operation buttons |

### Data Structure

**In-memory**: `ConcurrentHashMap<String, String>`
- Key: word
- Value: semicolon-separated meanings (e.g., "meaning1;meaning2;meaning3")

**Persistent**: JSON file format
```json
{
  "java": ["language", "island", "programming"],
  "dictionary": ["reference", "word collection"],
  "server": ["computer providing service", "restaurant"]
}
```

## 📋 Supported Operations

### 1. ADD
- **Function**: Add a new word with initial meaning
- **Behavior**: Fails if word already exists
- **Format**: `{"op": "ADD", "word": "java", "meaning": "language"}`

### 2. DELETE
- **Function**: Remove word and all meanings
- **Behavior**: Fails if word not found
- **Format**: `{"op": "DELETE", "word": "java"}`

### 3. QUERY
- **Function**: Lookup all meanings for a word
- **Returns**: Semicolon-separated meanings or "unsuccess" if not found
- **Format**: `{"op": "QUERY", "word": "java"}`

### 4. ADD_MEANING
- **Function**: Append new meaning to existing word
- **Behavior**: Fails if word not found
- **Format**: `{"op": "ADD_MEANING", "word": "java", "meaning": "island"}`

### 5. UPDATE
- **Function**: Replace old meaning with new meaning
- **Behavior**: Fails if old meaning not found
- **Format**: `{"op": "UPDATE", "word": "java", "old_meaning": "language", "new_meaning": "programming language"}`

## 🚀 Getting Started

### Prerequisites

- JDK 8 or higher
- Maven or IDE with Java compiler
- GSON library (included in `lib/` directory)

### Compilation

Using IDE (IntelliJ IDEA / Eclipse):
1. Open project as Java project
2. Run `ServerLauncher` and `ClientLauncher` classes

Using Command Line:
```bash
cd DictionaryGame/source_file/src

# Compile server
javac -cp ../lib/gson-2.8.5.jar server/*.java

# Compile client
javac -cp ../lib/gson-2.8.5.jar client/*.java

# Create dictionary.json if not exists
touch dictionary.json
```

### Running the Server

```bash
java -cp .:../lib/gson-2.8.5.jar server.ServerLauncher <port> <dictionary_file_path>
```

**Example**:
```bash
java -cp .:../lib/gson-2.8.5.jar server.ServerLauncher 8888 dictionary.json
```

The server launches with a GUI window showing:
- Number of connected clients
- Dictionary size statistics
- Server logs

### Running Clients

In a separate terminal(s):
```bash
java -cp .:../lib/gson-2.8.5.jar client.ClientLauncher <server_ip> <server_port>
```

**Example**:
```bash
java -cp .:../lib/gson-2.8.5.jar client.ClientLauncher localhost 8888
```

Each client launches with GUI for:
- Word/meaning input fields
- Operation buttons (ADD, DELETE, QUERY, ADD_MEANING, UPDATE)
- Response display area

## 🔄 Multi-threading Details

### Server Threading Model

- **Main Thread**: Accepts incoming connections
- **Thread Pool**: Fixed-size worker pool (default: 100 threads)
- **Worker Threads**: Each handles one client connection
- **Blocking Queue**: Task queue for distributing connections to workers

### Synchronization Primitives

- `ConcurrentHashMap`: Dictionary access without explicit locking
- `AtomicInteger`: Atomic client count updates
- `synchronized` blocks: Dictionary write operations
- No explicit locks on query operations (read-only)

### Thread Safety Contract

The dictionary achieves thread safety through:
1. Atomic put/remove operations on ConcurrentHashMap
2. Synchronization of compound operations (updateMeaning)
3. Atomic broadcasting of operations to persistent storage

## 💾 Persistence

### File Format

Plain JSON object mapping words to arrays of meanings.

### Write Behavior

- **Immediate**: All writes to dictionary trigger immediate file persistence
- **Atomic**: Entire dictionary written synchronously
- **Recovery**: Dictionary loaded from file on server startup

### Example session

```json
// Initial state
{}

// After "ADD java language"
{"java": ["language"]}

// After "ADD_MEANING java island"
{"java": ["language", "island"]}

// After "UPDATE java language programming"
{"java": ["programming", "island"]}
```

## 📊 Concurrency Testing

The system handles:
- Multiple simultaneous ADD operations
- Concurrent QUERY while UPDATE in progress
- DELETE while other clients are reading
- Racing conditions on same word (last write wins)

## 🐛 Known Limitations

- No authentication/authorization
- No encryption (plain text over TCP)
- Single-instance design (no replication/backup)
- Dictionary loaded entirely in memory
- No transaction support

## 🎓 Learning Points

This project demonstrates:
- **TCP Socket Programming**: Raw socket communication and protocol design
- **Multi-threading**: Managing worker threads and blocking queues
- **Concurrency**: Thread-safe data structures and synchronization
- **GUI Development**: Swing components and event handling
- **Persistence**: File I/O and JSON serialization
- **Protocol Design**: Structured message format and request/response patterns

## 📝 License

See project root [LICENSE](../LICENSE) file.

## 🔗 Related

- [Main Project README](../README.md)
- [WhiteBoardGame](../WhiteBoardGame/README.md) - RMI-based collaborative app