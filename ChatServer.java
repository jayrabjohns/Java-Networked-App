import java.io.IOException;

/**
 * Uses a Server to connect to clients and relay their messages to each other.
 */
public class ChatServer
{
	/**
	 * Entry point of the program.
	 * @param args Arguments passed in when ran from the command line.
	 *             <ul>
	 *             <li>-csp [int] specifies the port used when listening for new connections</li>
	 *             </ul>
	 */
	public static void main(String[] args)
	{
		int listenPort = 14001;
		
		// Parsing arguments
		for (int i = 0; i < args.length; i++)
		{
			switch (args[i])
			{
				// Set the port to listen on
				case "-csp":
					// Checking if the next arg is an integer
					if (i + 1 < args.length && args[i + 1].matches("^\\d+$"))
					{
						listenPort = Integer.parseInt(args[i + 1]);
						i++; // Skipping next arg
					}
					else
					{
						ConsoleIO.LogError("-csp must come before a positive integer.");
						System.exit(1);
					}
					break;
					
				default:
					ConsoleIO.LogError("%s is an unrecognised argument.", args[i]);
					System.exit(1);
					break;
			}
		}
		
		// Creating / starting server
		Server server = new Server(listenPort);
		boolean run = server.TryStartListeningForConnections();
		
		// Waiting for, and parsing, commands from the terminal
		ConsoleIO consoleIO = new ConsoleIO();
		while (run)
		{
			try
			{
				String input = consoleIO.GetInput().toLowerCase();
				
				switch (input)
				{
					// Gracefully exit the program.
					case "exit":
						server.StopListeningForConnections();
						server.DisconnectAllClients();
						run = false;
						break;
					
					default:
						break;
				}
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
