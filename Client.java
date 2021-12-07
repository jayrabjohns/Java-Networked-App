import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;


/**
 * Connects, sends, and receives messages with a server at a specified address / port.
 */
public class Client
{
	private final InetAddress RemoteAddress;
	private final int RemotePort;
	private final ConsoleIO ConsoleIO = new ConsoleIO();
	
	private Socket Socket = null;
	private PrintWriter ServerOut = null;
	private BufferedReader ServerIn = null;
	private String FirstResponse = "ChatClient";
	
	private Thread MessageListeningThread = null;
	private Thread MessageSendingThread = null;
	
	/**
	 * Constructs a new Client instance.
	 *
	 * @param remoteAddress The address of the server.
	 * @param remotePort	The port to connect to. (Server needs to be actively listening for connections on this port)
	 */
	public Client(InetAddress remoteAddress, int remotePort)
	{
		RemoteAddress = remoteAddress;
		RemotePort = remotePort;
	}
	
	/**
	 * Determines if this client is connected to a server.
	 *
	 * @return Whether the client is connected.
	 */
	public boolean Connected()
	{
		return Socket != null && !Socket.isClosed();
	}
	
	/**
	 * Tries to connect to the server with the address / port given at instantiation.
	 * Can only connect to one server at a time.
	 *
	 * @return Whether we successfully connected to the server. Returns false if already connected.
	 */
	public boolean TryConnect()
	{
		if (Connected())
		{
			return false;
		}
		
		try
		{
			Socket = new Socket(RemoteAddress, RemotePort);
			ServerOut = new PrintWriter(Socket.getOutputStream());
			ServerIn = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
			
			ConsoleIO.Log("Connected to %s:%d", RemoteAddress, RemotePort);
			SendMessage(FirstResponse);
			
		}
		catch (IOException | IllegalArgumentException | NullPointerException e)
		{
			// Failed connecting / port out of range
			ConsoleIO.LogError("Failed connecting to %s:%d", RemoteAddress, RemotePort);
		}
		
		return Connected();
	}
	
	/**
	 * Disconnects from the currently connected server, disposes of any resources used.
	 * Can reconnect to a new server once disconnected.
	 * Calling this multiple times has no effect.
	 */
	public void Disconnect()
	{
		CloseConnection();
		StopListeningForMessages();
		StopSendingMessages();
	}
	
	/**
	 * Closes connection with the connected server.
	 * A new connection can be made after closing this one.
	 * Calling this multiple times has no effect.
	 * Be aware that this call does not deal with remaining resources used.
	 */
	private void CloseConnection()
	{
		if (Socket == null)
		{
			return;
		}
		
		// Closing socket
		if (!Socket.isClosed())
		{
			try
			{
				Socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		Socket = null;
		ServerOut = null;
		ConsoleIO.LogError("Closed connection with server.");
	}
	
	/**
	 * Starts listening for messages from the server.
	 * Calling this does not block the thread it is called from.
	 * Calling this multiple times has no effect.
	 */
	public void StartListeningForMessages()
	{
		// Already listening for messages / not connected
		if (!Connected() || MessageListeningThread != null)
		{
			return;
		}
		
		// Creating new thread for receiving messages from the server
		MessageListeningThread = new Thread(() ->
		{
			try
			{
				while (!Thread.interrupted() && Connected())
				{
					String message = ServerIn.readLine();
					
					if (message != null)
					{
						OnMessageReceived(message);
					}
					else
					{
						// Server has disconnected us
						ConsoleIO.LogError("Disconnected by server (it could be shutting down).");
						break;
					}
				}
			}
			catch (Exception e)
			{
				// Socket / Stream was closed
				if (e.getClass() == SocketException.class || e.getClass() == IOException.class)
				{
					ConsoleIO.LogError("Unexpected disconnect from server.");
				}
				else
				{
					e.printStackTrace();
				}
			}
			finally
			{
				// Stop sending messages so the program can exit gracefully
				Disconnect();
			}
		},"MessageListeningThread");
		
		MessageListeningThread.start();
		ConsoleIO.Log("Started listening for messages from server.");
	}
	
	/**
	 * Logic for when we receive a message.
	 *
	 * @param message The message received.
	 */
	public void OnMessageReceived(String message)
	{
		ConsoleIO.Log(message);
	}
	
	/**
	 * Stops listening for messages from the connected server.
	 * Calling this multiple times has no effect.
	 */
	public void StopListeningForMessages()
	{
		if (MessageListeningThread == null || MessageListeningThread.isInterrupted())
		{
			return;
		}
		
		// Since the thread is requesting it's self be disposed, we can skip this and let it end naturally
		if (Thread.currentThread() != MessageListeningThread)
		{
			MessageListeningThread.interrupt();
			try
			{
				MessageListeningThread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		ConsoleIO.Log("Stopped listening for messages from server");
		MessageListeningThread = null;
	}
	
	/**
	 * Starts treating inputs from the terminal as messages to be sent to the connected server.
	 * Calling this multiple times has no effect.
	 */
	public void StartSendingMessages()
	{
		// Already sending messages / not connected
		if (!Connected() || MessageSendingThread != null)
		{
			return;
		}
		
		// Creating a new thread for sending messages to the server
		MessageSendingThread = new Thread(() ->
		{
			try
			{
				// Wait for input from user, then send it
				while (!Thread.interrupted() && Connected())
				{
					String message = ConsoleIO.GetInput();
					SendMessage(message);
				}
			}
			catch (Exception e)
			{
				if (e.getClass() == SocketException.class || e.getClass() == IOException.class)
				{
					// Socket / Stream closed
					ConsoleIO.LogError("Unexpected disconnect from server.");
				}
				else if (e.getClass() == InterruptedException.class)
				{
					// Interrupted while waiting for input
					// Just let thread die gracefully
				}
				else
				{
					e.printStackTrace();
				}
			}
			finally
			{
				// Stop sending messages so the program can exit gracefully
				Disconnect();
			}
			
			
		}, "MessageSendingThread");
		
		MessageSendingThread.start();
		ConsoleIO.Log("Started sending messages to %s:%d", RemoteAddress, RemotePort);
	}
	
	/**
	 * Sends a message to the server.s
	 *
	 * @param message The message to send.
	 */
	public void SendMessage(String message)
	{
		if (!Connected())
		{
			return;
		}
		
		ServerOut.println(message);
		ServerOut.flush();
	}
	
	/**
	 * Stops sending terminal inputs as messages to the server.
	 * Calling this multiple times has no effect.
	 */
	public void StopSendingMessages()
	{
		if (MessageSendingThread == null || MessageSendingThread.isInterrupted())
		{
			return;
		}
		
		// Since the thread is requesting it's self be disposed, we can skip this and let it end naturally
		if (Thread.currentThread() != MessageSendingThread)
		{
			ConsoleIO.StopInputs();
			MessageSendingThread.interrupt();
			try
			{
				MessageSendingThread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			ConsoleIO.Log("Stopped sending messages to %s:%d", RemoteAddress, RemotePort);
		}
		MessageSendingThread = null;
	}
	
	/**
	 * Sets the first response that should be sent to the server when connceting.
	 *
	 * @param firstResponse The response to send.
	 */
	public void SetFirstResponse(String firstResponse)
	{
		FirstResponse = firstResponse;
	}
	
	/**
	 * Gets the server out stream.
	 *
	 * @return A PrintWriter tied to the sockets out stream.
	 */
	public PrintWriter GetServerOut()
	{
		return ServerOut;
	}
	
	/**
	 * Gets the server in stream.
	 *
	 * @return A buffered reader tied to the sockets in stream
	 */
	public BufferedReader GetServerIn()
	{
		return ServerIn;
	}
}
