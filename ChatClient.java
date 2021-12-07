import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Uses a Client to connect, send, and receive messages with a server.
 */
public class ChatClient
{
	/**
	 * Entry point of the program.
	 * @param args Arguments passed in when ran from the command line.
	 *             <ul>
	 *             <li>-ccp [int] specifies the port to connect to (defaults to 14001).</li>
	 *             <li>-cca [IP] specifies the address to connect to (defaults to localhost).</li>
	 *             <li>-cb specifies this client should join as a chat bot</li>
	 *             <li>-dod specifies this client should join as a DoD client</li>
	 *             </ul>
	 */
	public static void main(String[] args)
	{
		int serverPort = 14001;
		InetAddress serverAddress = null;
		boolean isChatBotClient = false;
		boolean isDODClient = false;
		
		try
		{
			serverAddress = InetAddress.getByName("localhost");
		}
		catch (UnknownHostException e)
		{
			// Nothing to do
			// Client class will deal with the null value.
		}
		
		// Parsing arguments
		for (int i = 0; i < args.length; i++)
		{
			switch (args[i])
			{
				// Sets the port to connect to
				case "-ccp":
					// Checking if the next args is an int
					if (i + 1 < args.length && args[i + 1].matches("^\\d+$"))
					{
						serverPort = Integer.parseInt(args[i + 1]);
						i++; // Skipping next arg
					}
					else
					{
						ConsoleIO.LogError("-ccp must come before a positive integer.");
						System.exit(1);
					}
					break;
					
				// Set the address to connect to
				case "-cca":
					if (i + 1 < args.length)
					{
						try
						{
							serverAddress = InetAddress.getByName(args[i + 1]);
							i++; // Skipping next arg
						}
						catch (UnknownHostException e)
						{
							ConsoleIO.LogError("-cca must come before a valid IP address.");
							System.exit(1);
						}
					}
					break;
					
				// Enabling chat bot
				case "-cb":
					isChatBotClient = true;
					break;
					
				// Enabling dod
				case "-dod":
					isDODClient = true;
					break;
					
				default:
					ConsoleIO.LogError("%s is an unrecognised argument.", args[i]);
					System.exit(1);
					break;
			}
		}
		
		// Checking for incompatible args
		if (isChatBotClient && isDODClient)
		{
			ConsoleIO.LogError("Cannot be both a chat bot client (cbc) and a DoD client (dod) simultaneously.");
			return;
		}
		
		// Creating client
		Client client;
		if (isChatBotClient)
		{
			client = new ChatBotClient(serverAddress, serverPort);
		}
		else if (isDODClient)
		{
			client = new DoDClient(serverAddress, serverPort);
		}
		else
		{
			client = new Client(serverAddress, serverPort);
		}
		
		// Connecting client
		if (client.TryConnect())
		{
			client.StartSendingMessages();
			client.StartListeningForMessages();
		}
	}
}
