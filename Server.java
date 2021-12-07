import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A server for handling connections with, and relaying messages amongst clients.
 */
public class Server
{
	private final int Port;
	private final HashMap<String, ClientData> ConnectedClients = new HashMap<>();
	private final String ServerDisplayName = "SERVER";
	
	private ServerSocket ServerSocket;
	private Thread NewConnectionsThread = null;
	private int TotalClientsConnected;
	
	public Server(int port)
	{
		Port = port;
	}
	
	/**
	 * Determines whether this server is currently accepting new connections.
	 * The server may still be connected to clients even if its not currently accepting new ones.
	 *
	 * @return Whether it's currently accepting new connections.
	 */
	public boolean IsListeningForConnections()
	{
		return ServerSocket != null && !ServerSocket.isClosed();
	}
	
	/**
	 * Starts listening for new client connections.
	 * This does not block the thread which it is called from.
	 * Calling this multiple times will have no effect.
	 *
	 * @return Whether it successfully started listening for connections.
	 */
	public boolean TryStartListeningForConnections()
	{
		// Already listening for connections
		if (IsListeningForConnections())
		{
			return false;
		}
		
		// Creates a new socket to listen for connections.
		try
		{
			ServerSocket = new ServerSocket(Port);
		}
		catch (IOException | IllegalArgumentException e)
		{
			// Port already taken / out of range
			ConsoleIO.LogError("Cannot listen for connections on port %d.", Port);
			return false;
		}
		
		// Creates a new thread for listening for new connections
		NewConnectionsThread = new Thread(() ->
		{
			ConsoleIO.Log("Listening for connections on port %d...", Port);
			while (!Thread.interrupted())
			{
				Socket clientSocket;
				try
				{
					// Blocks thread when waiting for new connection
					clientSocket = ServerSocket.accept();
				}
				catch (Exception e)
				{
					// Socket / Stream was closed
					if (e.getClass() == SocketException.class || e.getClass() == IOException.class)
					{
						StopListeningForConnections();
					}
					else
					{
						e.printStackTrace();
					}
					return;
				}
				
				// Create client
				String displayName = String.format("C#%d", TotalClientsConnected++);
				ClientData clientData = new ClientData(clientSocket, null, displayName);
				clientData.SetConnectionStatus(ClientConnectionStatus.Connecting);
				
				// Block until client sends expected behaviour
				clientData.GetOutputStream();
				String response = WaitForResponse(clientData);
				
				// Dealing with clients disconnecting immediately
				if (response == null)
				{
					CloseSocket(clientData);
					continue;
				}
				
				// Setting clients type based on response
				switch (response)
				{
					case "ChatBot":
						clientData.SetType(ClientType.ChatBot);
						break;
						
					case "DoDBot":
						clientData.SetType(ClientType.DoDBot);
						break;
						
					case "ChatClient":
					default:
						clientData.SetType(ClientType.RegularChatter);
						break;
				}
				
				// Adding client to list
				synchronized (ConnectedClients)
				{
					ConnectedClients.put(clientData.DisplayName, clientData);
				}
				
				// Finalising everything
				ConsoleIO.LogHighlight("New client %s connected from %s:%d", clientData.DisplayName, clientSocket.getInetAddress(), clientSocket.getPort());
				clientData.SetConnectionStatus(ClientConnectionStatus.Connected);
				StartRelayingMessages(clientData);
				
				// Alerting all connected clients of the new connection
				String connectionAlert = String.format("%s%s has connected!%s", ConsoleIO.TextColourGreen, clientData.DisplayName, ConsoleIO.TextColourReset);
				SendMessageToAllClients(connectionAlert, ServerDisplayName);
			}
		});
		
		NewConnectionsThread.start();
		return true;
	}
	
	/**
	 * Stops the server from accepting new connections.
	 * Calling this multiple times will have no effect.
	 */
	public void StopListeningForConnections()
	{
		// Already stopped listening
		if (!IsListeningForConnections())
		{
			return;
		}
		
		// Closing server socket
		if (!ServerSocket.isClosed())
		{
			try
			{
				ServerSocket.close();
				NewConnectionsThread.join();
			}
			catch (InterruptedException | IOException e)
			{
				e.printStackTrace();
			}
		}

		NewConnectionsThread = null;
		ConsoleIO.LogError("Stopped listening for connections on port %d.", Port);
	}
	
	/**
	 * Starts relaying messages from a given client to other clients.
	 *
	 * @param clientData Client which the server should start listening for messages from.
	 * @return Whether the server successfully started listening for messages from this client.
	 * Returns false if the client is no longer connected / is null.
	 */
	public boolean StartRelayingMessages(ClientData clientData)
	{
		// Checking if the client exists & whether it's already listening for messages
		synchronized (ConnectedClients)
		{
			if (clientData == null || !ConnectedClients.containsKey(clientData.DisplayName) || clientData.Socket.isClosed())
			{
				return false;
			}
		}
		
		//Pattern privateMessagePrefix = Pattern.compile("^\\s*!pm\\s*");
		
		// Creates new thread for listening for messages from the given client
		clientData.MessageRelayingThread = new Thread(() ->
		{
			String response;
			while (!Thread.interrupted())
			{
				response = WaitForResponse(clientData);
				
				// End the thread given the response is null, most likely cause is us disconnecting from client
				if (response == null)
				{
					break;
				}
				
				// Checking if this was meant to be a private message
				if (clientData.GetClientType() == ClientType.DoDBot && response.startsWith("@"))
				{
					SendPrivateMessageToClient(response, clientData.DisplayName);
				}
				else
				{
					SendMessageToAllClients(response, clientData.DisplayName);
				}
			}
		}, clientData.DisplayName + " Message Thread");
		
		clientData.MessageRelayingThread.start();
		ConsoleIO.Log("Now listening for messages from %s(%s:%d)", clientData.DisplayName, clientData.Socket.getInetAddress(), clientData.Socket.getPort());
		return true;
	}
	
	/**
	 * Sends a message to all clients connected to the server.
	 *
	 * @param message The message to send.
	 * @param senderName Display name of the sender.
	 */
	private void SendMessageToAllClients(String message, String senderName)
	{
		Iterator<Map.Entry<String, ClientData>> iterator = ConnectedClients.entrySet().iterator();
		while (iterator.hasNext())
		{
			ClientData clientData = iterator.next().getValue();
			SendMessageToClient(clientData, message, senderName);
		}
	}
	
	/**
	 * Sends a message to a specific client connected to the server.
	 *
	 * @param recipient The receiving client.
	 * @param message The message to send.
	 * @param senderName The name of the sender.
	 */
	private void SendMessageToClient(ClientData recipient, String message, String senderName)
	{
		PrintWriter outStream = recipient.GetOutputStream();
		outStream.printf("<%s> %s%n", senderName, message);
		outStream.flush();
	}
	
	/**
	 * Takes in a message in the format '@[recipientDisplayName] [response]', sends the response privately to the recipient.
	 *
	 * @param message A message in the format '@[recipientDisplayName] [response]'.
	 * @param senderName The display name of the sender.
	 */
	private void SendPrivateMessageToClient(String message, String senderName)
	{
		if (!message.startsWith("@"))
		{
			return;
		}
		
		message = message.substring(1);
		
		// Checking if they gave a name to private message
		int indexOfFirstSpace = message.indexOf(" ");
		if (indexOfFirstSpace != -1)
		{
			// Checking the given name is currently connected
			String displayName = message.substring(0, indexOfFirstSpace);
			if (ConnectedClients.containsKey(displayName))
			{
				message = message.substring(indexOfFirstSpace + 1);
				message = String.format("%s(DM) %s %s", ConsoleIO.TextColourYellow, message, ConsoleIO.TextColourReset);
				SendMessageToClient(ConnectedClients.get(displayName), message, senderName);
			}
			else
			{
				// Alerting the client that that client isn't connected
				message = String.format("%s%s is not connected(message was not sent)%s", ConsoleIO.TextColourYellow, displayName, ConsoleIO.TextColourReset);
				SendMessageToClient(ConnectedClients.get(senderName), message, ServerDisplayName);
			}
		}
		else
		{
			// Alerting the client they didn't provide a name
			message = String.format("%sProvide a name when trying to primate message(message was not sent)%s", ConsoleIO.TextColourYellow, ConsoleIO.TextColourReset);
			SendMessageToClient(ConnectedClients.get(senderName), message, ServerDisplayName);
		}
	}
	
	/**
	 * Waits for a response from a client.
	 *
	 * @param sender The client to wait for a response from.
	 * @return A string representation of the client's response.
	 * @throws NullPointerException If the provided client data is null.
	 */
	private String WaitForResponse(ClientData sender) throws NullPointerException
	{
		String response = null;
		
		try
		{
			response = sender.GetInputStream().readLine();
		}
		catch (Exception e)
		{
			// Socket / Stream was closed
			if (e.getClass() == SocketException.class || e.getClass() == IOException.class)
			{
				DisconnectClient(sender);
			}
			else
			{
				e.printStackTrace();
			}
		}
		
		return response;
	}
	
	/**
	 * Closes the socket of a given client.
	 *
	 * @param clientData The client who's socket needs ot be closed.
	 */
	private void CloseSocket(ClientData clientData)
	{
		if (!clientData.Socket.isClosed())
		{
			try
			{
				clientData.Socket.close();
			}
			catch (IOException e)
			{
				ConsoleIO.LogError("Couldn't close socket for '%s'", clientData.DisplayName);
			}
		}
	}
	
	/**
	 * Disconnects a given client from the server, disposes of any resources used.
	 * This will have no effect if the client does not exist / is no longer connected / is null.
	 *
	 * @param clientData The client to disconnect.
	 */
	public void DisconnectClient(ClientData clientData)
	{
		if (clientData == null || clientData.GetConnectionStatus() == ClientConnectionStatus.Disconnecting)
		{
			return;
		}
		
		clientData.SetConnectionStatus(ClientConnectionStatus.Disconnecting);
		
		// Removing client from list
		synchronized (ConnectedClients)
		{
			ConnectedClients.remove(clientData.DisplayName);
		}
		
		// Close client socket
		CloseSocket(clientData);
		
		// Close listening thread.
		if (clientData.MessageRelayingThread != null && Thread.currentThread() != clientData.MessageRelayingThread)
		{
			clientData.MessageRelayingThread.interrupt();
			try
			{
				clientData.MessageRelayingThread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			clientData.MessageRelayingThread = null;
		}
		
		// Alerting all connected clients of hte disconnect
		clientData.SetConnectionStatus(ClientConnectionStatus.Disconnected);
		ConsoleIO.LogError("Disconnected from %s(%s:%d)", clientData.DisplayName, clientData.Socket.getInetAddress(), clientData.Socket.getPort());
		String alert = String.format("%s%s has disconnected.%s", ConsoleIO.TextColourRed, clientData.DisplayName, ConsoleIO.TextColourReset);
		SendMessageToAllClients(alert, ServerDisplayName);
	}
	
	/**
	 * Disconnects all currently connected clients.
	 */
	public void DisconnectAllClients()
	{
		Iterator<Map.Entry<String, ClientData>> iterator = ConnectedClients.entrySet().iterator();
		while (iterator.hasNext())
		{
			ClientData clientData = iterator.next().getValue();
			DisconnectClient(clientData);
		}
	}
}
