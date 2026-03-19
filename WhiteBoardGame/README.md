# WhiteBoardGame

`WhiteBoardGame` is a collaborative whiteboard implemented with Java RMI and Swing. A manager hosts the session, approves join requests, synchronizes drawing events to guests, and can save or restore the board state.

## Highlights

- Java RMI host-and-guest architecture
- Manager approval flow for join requests
- Real-time synchronized drawing and chat
- Shared canvas with line, rectangle, triangle, oval, freestyle, eraser, text, color, and stroke controls
- Save and load support for whiteboard sessions

## Project Structure

```text
WhiteBoardGame/
├── lib/
│   └── gson-2.10.1.jar
├── save/
├── src/
│   ├── common/
│   ├── guest/
│   ├── icon/
│   ├── manager/
│   ├── remote/
│   └── util/
└── README.md
```

## Roles

### Manager

The manager starts the RMI registry, binds the whiteboard service, opens the main whiteboard window, and controls the session. Manager capabilities include:

- approving or rejecting join requests
- viewing the active member list
- kicking guests
- sending chat messages
- starting a new blank board
- saving the board as JSON
- saving the canvas as an image
- opening a saved JSON board and rebroadcasting it to guests

### Guest

Guests connect to the manager's RMI service, request admission, and receive callback updates through their own remote stub. Once accepted, a guest receives:

- the manager-assigned session username
- the current whiteboard drawing history as a batch
- user list updates
- new drawing, chat, kick, and shutdown events

## Main Components

- `manager.LoginWindow`: manager startup window and RMI binding entry point
- `manager.ManagerUI`: main host whiteboard window
- `manager.WhiteboardServerImpl`: RMI server implementation and broadcast coordinator
- `guest.JoinWindow`: guest startup window and RMI lookup entry point
- `guest.GuestUI`: guest whiteboard window
- `guest.MessageHandler`: client-side dispatcher for incoming manager messages
- `remote.WhiteboardServer`: remote API exposed by the manager
- `remote.WhiteboardRemote`: callback API implemented by each guest
- `util.Message`: shared JSON message envelope used for whiteboard events

## Build

From the `WhiteBoardGame/` directory:

```sh
mkdir -p out
javac -cp "lib/gson-2.10.1.jar:src" -d out $(find src -name '*.java')
```

## Run

Start the manager:

```sh
java -cp "out:src:lib/gson-2.10.1.jar" manager.LoginWindow localhost 1099 admin
```

Start a guest in another terminal:

```sh
java -cp "out:src:lib/gson-2.10.1.jar" guest.JoinWindow 127.0.0.1 1099 guest
```

If you omit command-line arguments, both launcher classes fall back to local defaults.

## Message Flow

The whiteboard uses JSON messages over RMI callbacks for synchronization. Important message types include:

- `draw`: append one drawing record
- `batch`: send a full drawing history to a newly joined guest
- `chat`: deliver a chat message
- `userList`: refresh the displayed participants
- `new`: clear the canvas for everyone
- `rename`: tell a guest their accepted username
- `kick`: force a guest to leave
- `shutdown`: close all guest sessions when the manager exits

## Notes

- The icon assets under `src/icon/` are loaded from the runtime classpath, so `src` is included when launching.
- The manager window is the source of truth for canvas state and rebroadcasts loaded board contents to connected guests.
- Join requests are approved interactively through a Swing confirmation dialog on the manager side.
