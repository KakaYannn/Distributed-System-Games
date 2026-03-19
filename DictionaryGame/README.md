# DictionaryGame

`DictionaryGame` is a distributed dictionary service built with Java sockets and Swing. A central server stores the dictionary in JSON, accepts concurrent TCP clients, and updates the shared file whenever a change is made.

## Highlights

- Multi-client TCP server using `ServerSocket`
- Custom fixed-size worker pool in `server.ThreadPool`
- Thread-safe in-memory dictionary with `ConcurrentHashMap`
- Persistent JSON backing store in `src/dictionary.json`
- Swing client and server dashboards

## Project Structure

```text
DictionaryGame/
├── lib/
│   └── gson-2.10.1.jar
├── src/
│   ├── client/
│   │   ├── ClientLauncher.java
│   │   ├── ClientUI.java
│   │   └── TCPInteractiveClient.java
│   ├── server/
│   │   ├── Connection.java
│   │   ├── ServerLauncher.java
│   │   ├── ServerUI.java
│   │   ├── TCPInteractiveServer.java
│   │   └── ThreadPool.java
│   └── dictionary.json
├── .classpath
├── .project
└── README.md
```

## Supported Operations

The client UI sends JSON commands with these operations:

- `QUERY`: fetch all meanings for a word
- `ADD`: create a new word with an initial meaning
- `DELETE`: remove a word from the dictionary
- `ADD_MEANING`: append another meaning to an existing word
- `UPDATE`: replace one existing meaning with a new value

Meanings are stored internally as semicolon-delimited strings and written back to the JSON file after each request.

## How It Works

### Server side

- `server.ServerLauncher` starts the application with a port and dictionary file path.
- `server.TCPInteractiveServer` loads the JSON dictionary, starts the Swing server dashboard, opens the listening socket, and dispatches each accepted socket to the custom thread pool.
- `server.Connection` parses each client request, applies the requested mutation or query, writes the response, and logs activity to the server UI and `server.log`.

### Client side

- `client.ClientLauncher` accepts the server address and port, prompts for a username, and opens the Swing client window.
- `client.ClientUI` collects user input and enqueues JSON commands.
- `client.TCPInteractiveClient` owns the socket connection and performs request/response communication with the server.

## Build

From the `DictionaryGame/` directory:

```sh
mkdir -p out
javac -cp "lib/gson-2.10.1.jar:src" -d out $(find src -name '*.java')
```

## Run

Start the server first:

```sh
java -cp "out:lib/gson-2.10.1.jar" server.ServerLauncher 5000 src/dictionary.json
```

Then start one or more clients:

```sh
java -cp "out:lib/gson-2.10.1.jar" client.ClientLauncher 127.0.0.1 5000
```

## Notes

- The server requires a port in the range `1024` to `65535`.
- The client prompts for a username on startup and includes it in request logs.
- If the server disconnects, the client disables its action buttons and shows an error message.
- `server.log` is written in the process working directory when the server is running.
