import DoDGame.CharacterBase;
import DoDGame.GameController;
import DoDGame.Globals;

import java.net.InetAddress;
import java.util.regex.Pattern;

public class DoDClient extends BotBase
{
	private GameController GameController = new GameController();
	
	/**
	 * Constructs a new DoDClient.
	 *
	 * @param remoteAddress The address of the server.
	 * @param remotePort    The port to connect to. (Server needs to be actively listening for connections on this port)
	 */
	public DoDClient(InetAddress remoteAddress, int remotePort)
	{
		super(remoteAddress, remotePort, Pattern.compile("^!dod\\s*"));
		
		SetFirstResponse("DoDBot");
	}
	
	/**
	 * Logic for when we receive a message.
	 *
	 * - After a dod client has connected to the server, players can join the DOD game by typing '!dod join'
	 * - The dod client will keep track of who's turn it is, a players commands will only be parsed when it is their go.
	 * - Players can join at any time
	 * - Players can stop playing by prematurely calling the 'quit' command.
	 * - Once every player has had their go, the bot will have their go.
	 * - The game ends when either when there are no players left, or if a player successfully exits with the correct amount of gold.
	 *
	 * @param message The message received.
	 */
	@Override
	public void OnMessageReceived(String message)
	{
		super.OnMessageReceived(message);
		
		// Extracting player name form the message
		String senderName = GetDisplayName(message);
		message = RemoveANSIEscapeCodes(message);
		message = RemoveDisplayName(message);
		
		String[] messageArray = message.split("\\s+");
		
		// Checking the sender exists as a player
		boolean playerExists = GameController.GetPlayerByName(senderName) != null;
		
		// Getting the name of the character who's supposed to be playing right now.
		CharacterBase currentCharacter = GameController.GetCurrentCharacter();
		String currentPlayerName = null;
		if (currentCharacter != null)
		{
			currentPlayerName = currentCharacter.GetName();
		}
		
		// Dealing clients disconnecting
		if (senderName.toLowerCase().equals("server") && message.contains("disconnect"))
		{
			// Treats first word as the clients name
			String nameToRemove = messageArray.length > 0 ? messageArray[0] : null;
			playerExists = nameToRemove != null && GameController.GetPlayerByName(nameToRemove) != null;
			
			// Removing player if they were part of the game
			if (playerExists)
			{
				SendMessage(String.format("%s has left the game.", nameToRemove));
				GameController.RemovePlayer(nameToRemove);
				GameController.SetupNextTurn();
			}
		}
		
		// Checking the message is intended as a dod command
		else if (messageArray.length == 0 || !IsPrefix(messageArray[0]))
		{
			return;
		}
		
		// Adding new players to the game
		else if (message.toLowerCase().contains("join"))
		{
			if (!playerExists)
			{
				SendMessage(String.format("%s has joined the game.", senderName));
				
				// Start game if first player
				if (!GameController.GetGameStarted())
				{
					Globals.UserIO.SetInOutStreams(GetServerIn(), GetServerOut());
					GameController.Start(senderName, true);
				}
				else
				{
					GameController.AddPlayer(senderName);
				}
			}
			else
			{
				SendMessage(String.format("%s, you have already joined the game.", senderName));
			}
		}
		
		else if (!playerExists)
		{
			SendMessage(String.format("%s, you need to first join the game before playing.", senderName));
		}
		
		// Correct player is having their turn
		else if (senderName.equals(currentPlayerName))
		{
			// Getting the command string from the message
			message = RemoveChatPrefix(message);
			
			// Splitting into command and argument
			String[] inputs = message.toLowerCase().split("\\s+");
			String commandKey = inputs.length > 0 ? inputs[0].toLowerCase() : null;
			String arg = inputs.length > 1 ? inputs[1] : null;
			
			// Executing command
			String commandResponse = GameController.TakeTurn(Globals.GameSettings.GetCurrentPlayer(), commandKey, arg);
			
			// Splitting response by newlines and sending them individually
			// This makes sure that responses that span multiple lines (look) get sent entirely as a private message
			for (String line: commandResponse.split("[\r\n]+"))
			{
				SendMessage(String.format("@%s %s", senderName, line));
			}
			
			GameController.SetupNextTurn();
		}
		
		// Incorrect player tries to have a turn
		else
		{
			SendMessage(String.format("%s, it is currently %s's turn.", senderName, currentPlayerName));
		}
	}
}
