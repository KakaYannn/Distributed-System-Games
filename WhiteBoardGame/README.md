# WhiteBoardGame

A real-time collaborative whiteboard application using Java RMI. Demonstrates distributed object design, RMI callbacks, and architectural patterns for state synchronization in collaborative systems.

## 📐 Overview

WhiteBoardGame is a collaborative drawing application where a manager (host) creates a whiteboard session and guests remotely join to draw together. All clients maintain a synchronized view of the canvas through Java RMI remote method invocations and callback mechanisms.

### Key Features

- **Real-time Synchronization**: All drawing actions instantly propagated to connected clients
- **Java RMI**: Distributed objects with remote method callbacks
- **Multiple Drawing Tools**: Lines, ovals, rectangles, triangles, freehand, text, eraser
- **Color & Stroke Control**: Full RGB color picker and adjustable stroke widths
- **User Management**: Guest list, user renaming, kick functionality
- **Persistent Canvas**: Save/load drawing sessions
- **Chat System**: Embedded messaging between participants
- **Thread-safe Broadcast**: Asynchronous message delivery via ExecutorService

## 🏗️ Architecture

### Communication Model

- **Framework**: Java RMI (Remote Method Invocation)
- **Message Format**: JSON (GSON serialization)
- **Message Types**: draw, chat, userList, rename, batch, kick, new, shutdown
- **Callback Pattern**: Guest implements remote interface for manager to invoke

### Components

#### Manager-Side (Server/Host)

| Component | Role |
|-----------|------|
| **WhiteboardServerImpl** | RMI remote object; manages guest registry, broadcasts messages via ExecutorService |
| **ManagerUI** | Main window with canvas, user list, chat, file menu, and tool palette |
| **ManagerCanvasListener** | Mouse listener; handles local drawing and broadcasts to guests |
| **LoginWindow** | Manager login/session setup interface |
| **OpenFileWindow** | Load existing drawing from file |
| **SaveWindow** | Save drawing to file (overwrites) |
| **SaveAsWindow** | Save drawing with new filename |

#### Guest-Side (Client)

| Component | Role |
|-----------|------|
| **GuestRemoteImpl** | RMI callback object; receives messages from manager |
| **GuestUI** | Drawing canvas and chat interface (read-only file operations) |
| **GuestCanvasListener** | Mouse listener; sends drawing commands to manager |
| **MessageHandler** | Dispatches incoming JSON messages to appropriate handlers |
| **JoinWindow** | Connection dialog to join manager session |

#### Shared Components

| Component | Role |
|-----------|------|
| **SharedCanvasPainter** | JPanel rendering drawing instructions; parses shape format |
| **AbstractCanvasListener** | Base drawing handler; maintains instruction record |
| **WhiteboardServer** | RMI interface definition (manager exports) |
| **WhiteboardRemote** | RMI interface definition (guest exports) |
| **Message** | POJO for JSON message serialization |

## 🎨 Drawing Tools & Features

### Supported Shapes

1. **Line** - Straight line between two points
2. **Oval** - Ellipse/circle (axis-aligned)
3. **Rectangle** - Four-sided shape (axis-aligned)
4. **Triangle** - Three-sided polygon
5. **Freestyle** - Freehand drawing path
6. **Eraser** - Remove drawn content (white paint)
7. **Text** - Rendered text on canvas

### Instruction Format

Drawing instructions are stored as strings in a custom format:

```
Shape <strokeWidth> <R> <G> <B> <startX> <startY> <endX> <endY>
Text <strokeWidth> <R> <G> <B> <X> <Y> ::: <text_content>
```

**Example**:
```
Line 2 255 0 0 100 100 200 200       // Red line, width 2
Oval 3 0 255 0 50 50 150 150         // Green oval, width 3
Text 4 0 0 255 100 300 ::: Hello     // Blue text
```

### Tool Palette Features

- **Color Picker**: Full RGB selection with visual preview
- **Stroke Width**: Adjustable line thickness (1-20+)
- **Tool Selection**: Buttons for each drawing tool
- **Clear Canvas**: Reset all drawings
- **Undo/Redo**: (Instruction list-based)

## 🔄 RMI Interface Definitions

### WhiteboardServer (Manager exports)

```java
public interface WhiteboardServer extends Remote {
    String requestJoin(String name, WhiteboardRemote guest) 
        throws RemoteException;
    
    void broadcast(String json) throws RemoteException;
    
    void removeClient(String name) throws RemoteException;
    
    void updateUserList() throws RemoteException;
    
    void handleBegin(String sender) throws RemoteException;
    
    void kickGuest(String name) throws RemoteException;
}
```

### WhiteboardRemote (Guest exports)

```java
public interface WhiteboardRemote extends Remote {
    void receiveMessage(String json) throws RemoteException;
}
```

## 📨 Message Format

All messages use JSON with standard envelope:

```json
{
  "type": "draw|chat|userList|rename|batch|kick|new|shutdown",
  "sender": "username",
  "data": {
    // Type-specific payload
  }
}
```

### Message Types

#### draw
```json
{
  "type": "draw",
  "sender": "Alice",
  "data": {"instruction": "Line 2 255 0 0 100 100 200 200"}
}
```

#### chat
```json
{
  "type": "chat",
  "sender": "Alice",
  "data": {"message": "Hello everyone!"}
}
```

#### userList
```json
{
  "type": "userList",
  "sender": "Server",
  "data": {"users": ["Alice", "Bob", "Charlie"]}
}
```

#### rename
```json
{
  "type": "rename",
  "sender": "Server",
  "data": {"oldName": "Alice", "newName": "AliceNew"}
}
```

## 🔀 Message Flow Examples

### Drawing Flow (Manager → Guests)

```
1. Manager mouse event (draw line)
   ↓
2. ManagerCanvasListener.mouseDragged() 
   ↓
3. SendDrawMessage() → create Message(type="draw", instruction="...")
   ↓
4. WhiteboardServerImpl.broadcast(json)
   ↓
5. ExecutorService task: guest.receiveMessage(json) for each guest
   ↓
6. GuestRemoteImpl.receiveMessage() (RMI callback)
   ↓
7. MessageHandler.handle() → parse type="draw"
   ↓
8. Update GuestUI listener.record → canvas.repaint()
```

### Chat Flow (Bidirectional)

```
Client A sends chat message
   ↓
ClientUI → TCPInteractiveClient.sendMessage()
   ↓
Manager receives via broadcast
   ↓
Manager updates its own chat display
   ↓
Manager broadcasts to all other guests
   ↓
Other guests receive and update display
```

## 🚀 Getting Started

### Prerequisites

- JDK 8 or higher
- RMI Registry (usually included with JDK)
- GSON library (included in `lib/` directory)
- IDE with built-in Java compiler or command-line javac

### Compilation

Using IDE (IntelliJ IDEA / Eclipse):
1. Open `COMP90015_A2_submitted_1638660/` as Java project
2. Set up library dependencies from `lib/` folder
3. Compile all sources

Using Command Line:
```bash
cd WhiteBoardGame/COMP90015_A2_submitted_1638660/src

# Compile all sources
javac -cp ../lib/gson-2.8.5.jar:. */*.java
```

### Running - Manager (Host)

1. **Start RMI Registry** (in terminal):
```bash
cd WhiteBoardGame/COMP90015_A2_submitted_1638660
rmiregistry &
```

2. **Start Manager** (in separate terminal):
```bash
cd src
java -cp ../lib/gson-2.8.5.jar:. manager.ManagerUI
```

The manager window appears for setting up the whiteboard session.

### Running - Guest (Client)

In separate terminal(s):
```bash
cd WhiteBoardGame/COMP90015_A2_submitted_1638660/src
java -cp ../lib/gson-2.8.5.jar:. guest.GuestUI
```

Guest connects via dialog:
- Enter guest name
- Enter manager host (e.g., "localhost" or IP address)
- Request to join session

## 🔄 Multi-threading Details

### Server Threading Model

**RMI Broadcast Mechanism:**
```java
ExecutorService executor = Executors.newCachedThreadPool();

void broadcast(String json) {
    for (WhiteboardRemote guest : guests.values()) {
        executor.execute(() -> {
            guest.receiveMessage(json);  // RMI callback in thread pool
        });
    }
}
```

Benefits:
- Non-blocking broadcasts
- Prevents slow/hanging guests from blocking others
- Automatic thread management via cached pool

### GUI Thread Safety

All Swing updates marshaled through Event Dispatch Thread:
```java
SwingUtilities.invokeLater(() -> {
    canvas.repaint();
    chatBox.append(message);
});
```

## 💾 File Persistence

### Save Format

Drawing session stored as text file with one instruction per line:

```
Line 2 255 0 0 100 100 200 200
Oval 3 0 255 0 50 50 150 150
Oval 3 0 255 0 100 100 200 200
Text 4 0 0 255 150 250 ::: Hello World
```

### Save/Load Operations

- **Save**: All instructions written to file sequentially
- **Load**: Instructions read and replayed on canvas startup
- **Format**: Plain text, human-readable, line-based
- **Manager Only**: Load/save operations restricted to manager

## 🌐 Network Configuration

### Port Usage

- **RMI Registry**: Default port 1099 (configurable)
- **RMI Remote Objects**: Dynamically allocated ports
- **Manager IP**: Guests need manager's IP/hostname for RMI lookup

### RMI Naming

- **Service Name**: `WhiteboardService` (bound in registry)
- **Lookup**: `rmi://managerHost:1099/WhiteboardService`

## 🎓 Learning Points

This project demonstrates:
- **RMI Framework**: Remote object creation, binding, and invocation
- **Callback Patterns**: Two-way RMI communication
- **Distributed State**: Synchronization across network boundaries
- **Concurrent Broadcasting**: Async message delivery
- **Collaborative UX**: Real-time drawing synchronization
- **File Persistence**: Session save/restore
- **Thread-safe Swing**: GUI updates from RMI callback threads

## 🐛 Known Limitations

- No authentication (anyone can connect to RMI registry)
- No encryption or security (plain JSON messages)
- Limited to local network (RMI doesn't traverse firewalls easily)
- No automatic reconnection on connection loss
- Drawing optimization (sends instruction per operation, not batched)
- Single manager instance (no failover)

## ✨ Performance Notes

- **Canvas Rendering**: Redraws entire instruction list on each paint
- **Message Batching**: Could batch multiple instructions in single RMI call
- **Stroke Smoothing**: Text rendering uses default font metrics
- **Scalability**: Works best with 2-10 concurrent guests

## 📝 License

See project root [LICENSE](../../LICENSE) file.

## 🔗 Related

- [Main Project README](../../README.md)
- [DictionaryGame](../DictionaryGame/README.md) - TCP socket-based app