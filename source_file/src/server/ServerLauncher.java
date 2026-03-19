package server;

/**
 * Entry point for launching the server-side application.
 * This class parses command-line arguments to initialize the dictionary file path and server port,
 * then delegates setup and execution to the TCPInteractiveServer class.
 */
public class ServerLauncher {
	
	public static void main(String[] args) {
		// Validate command-line arguments: expecting exactly 2 arguments
        if (args.length == 2) {
        	// Check if the port is within a valid range
            if (Integer.valueOf(args[0]) < 1024 || Integer.valueOf(args[0]) > 65535) {
                System.out.println("Invalid port input.");
                System.exit(1);
            }
         // Create the server with the given dictionary path and port
            TCPInteractiveServer server = new TCPInteractiveServer(args[1], Integer.valueOf(args[0]));
            server.serverPrepare();
        } else {
            System.out.println("Invalid input.");
            System.exit(1);
        }

    }

}
