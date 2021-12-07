import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Connection status of a client.
 */
enum ClientConnectionStatus
{
	Disconnected,
	Connecting,
	Connected,
	Disconnecting
}

/**
 * Different types of possible client.
 */
enum ClientType
{
	RegularChatter,
	ChatBot,
	DoDBot
}

/**
 * Stores data on a connected client.
 */
class ClientData
{
	public final Socket Socket;
	public Thread MessageRelayingThread;
	public final String DisplayName;
	
	private ClientConnectionStatus ConnectionStatus;
	private ClientType Type;
	private PrintWriter OutputStream;
	private BufferedReaderEx InputStream;
	
	public ClientData(Socket socket, Thread messageRelayingThread, String displayName)
	{
		Socket = socket;
		MessageRelayingThread = messageRelayingThread;
		DisplayName = displayName;
		ConnectionStatus = ClientConnectionStatus.Disconnected;
		
		try
		{
			OutputStream = new PrintWriter(Socket.getOutputStream());
			InputStream = new BufferedReaderEx(new InputStreamReader(Socket.getInputStream()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the clients out stream.
	 *
	 * @return A PrintWriter tied to the client sockets output stream.
	 */
	public PrintWriter GetOutputStream()
	{
		return OutputStream;
	}
	
	/**
	 * Gets the clients in stream.
	 *
	 * @return A BufferedReader tied to the client sockets input stream.
	 */
	public BufferedReaderEx GetInputStream()
	{
		return InputStream;
	}
	
	/**
	 * Gets the clients connection status
	 *
	 * @return The clients connection status.
	 */
	public synchronized ClientConnectionStatus GetConnectionStatus()
	{
		return ConnectionStatus;
	}
	
	/**
	 * Sets the client connection status.
	 *
	 * @param newState Status to give the client.
	 */
	public synchronized void SetConnectionStatus(ClientConnectionStatus newState)
	{
		ConnectionStatus = newState;
	}
	
	/**
	 * Gets what type of client this is.
	 *
	 * @return The type of client.
	 */
	public synchronized ClientType GetClientType()
	{
		return Type;
	}
	
	/**
	 * Sets the clients type.
	 *
	 * @param newType The type to give this client.
	 */
	public synchronized void SetType(ClientType newType)
	{
		Type = newType;
	}
}