package DoDGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controls game logic.
 */
public class GameController
{
	private HashMap<String, ICommand> Commands;
	private int TotalNumberOfTurns = 0;
	private int CurrentCharacterIndex = 0;
	private boolean GameStarted = false;
	
	public GameController()
	{
		// Defining all valid commands that can be used during a character's turn.
		Commands = new HashMap<>();
		
		Commands.put("hello", (character, arg) ->
		{
			Hello();
			return "";
		});
		
		Commands.put("gold", (character, arg) ->
		{
			Gold();
			return "";
		});
		
		Commands.put("move", (character, directionStr) -> Move(character, directionStr));
		
		Commands.put("pickup", (character, arg) ->
		{
			if (character instanceof Player)
			{
				Player player = (Player)character;
				return Pickup(player);
			}
			return "";
		});
		
		Commands.put("look", (character, arg) -> Look(character));
		
		Commands.put("quit", (character, arg) ->
		{
			if (character instanceof Player)
			{
				Player player = (Player)character;
				Quit(player);
			}
			
			return "";
		});
	}
	
	/**
	 * Called at the start of the game to set everything up.
	 * Removes the need to specify a character to start the game with.
	 */
	public void Start()
	{
		Start(null, false);
	}
	
	/**
	 * Called at the start of the game to set everything up.
	 * @param firstPlayerName The name of the first player of the game.
	 */
	public void Start(String firstPlayerName, boolean randomMap)
	{
		// Setting up the map
		Map currentMap = Globals.UserIO.SelectMap(randomMap);
		Globals.GameSettings.SetCurrentMap(currentMap);
		Globals.UserIO.WriteToUser("Entering %s...", currentMap.GetName());
		
		// Setting up the bots
		for (int i = 0; i < Globals.GameSettings.GetNumberOfBots(); i++)
		{
			Vector2 enemyStartPos = currentMap.GetRandomSpawnPos();
			currentMap.InvalidSpawnPositions.add(enemyStartPos);
			
			// Stop adding bots since there's no more free space.
			if (enemyStartPos == null)
			{
				break;
			}
			
			Globals.ObjectController.CreateBot(enemyStartPos);
		}
		
		// Setting up the player
		AddPlayer(firstPlayerName);
		
		// Setting to -1 so it will be 0 on the first turn.
		CurrentCharacterIndex = -1;
		SetupNextTurn();
		
		GameStarted = true;
	}
	
	public void StopGame()
	{
	
	}
	
	/**
	 * Called to progress the game by one frame.
	 */
	public void Update()
	{
		TakeTurn(GetCurrentCharacter());
		SetupNextTurn();
	}
	
	public void SetupNextTurn()
	{
		CurrentCharacterIndex = ++CurrentCharacterIndex % GetCurrentCharacterCount();
		CharacterBase character = GetCurrentCharacter();
		
		Globals.UserIO.WriteToUser("%s has begun their turn...", character.Name);
		
		if (character instanceof Player)
		{
			Globals.GameSettings.SetCurrentPlayer((Player)character);
		}
		else if (character instanceof Bot)
		{
			Update();
		}
	}
	
	/**
	 * Gives a character the opportunity to choose a command and play it.
	 * @param character The character who's turn it is.
	 */
	public void TakeTurn(CharacterBase character)
	{
		TakeTurn(character, null, null);
	}
	
	/**
	 * Makes a given character take a turn with by executing the given command.
	 * @param character The character who's turn it is.
	 * @param commandKey The command to execute.
	 * @param arg The arg to pass into the command.
	 */
	public String TakeTurn(CharacterBase character, String commandKey, String arg)
	{
		if (character == null)
		{
			return null;
		}
		
		//Globals.UserIO.WriteToUser("\n\n%s's turn...", character.Name);
		
		// If command was included as a parameter
		if (commandKey == null)
		{
			String[] inputs = character.ChooseCommand();
			
			// Splitting into command and argument
			commandKey = inputs.length > 0 ? inputs[0].toLowerCase() : null;
			arg = inputs.length > 1 ? inputs[1] : null;
		}
		
		// Executing command
		ICommand command = Commands.get(commandKey);
		String response = command != null ? command.Invoke(character, arg) : String.format("'%s' is not a recognised command.", commandKey);
		
		// Checking if the bot has won
		if (character instanceof Bot)
		{
			Iterator<Player> playersIterator = Globals.ObjectController.LoadedPlayers.iterator();
			while (playersIterator.hasNext())
			{
				Player player = playersIterator.next();
				if (character.Position.Equals(player.Position))
				{
					Globals.UserIO.WriteToUser("%s has caught player %s.", character.GetName(), player.GetName());
					RemovePlayer(player);
				}
			}
		}
		
		TotalNumberOfTurns++;
		return response;
	}
	
	/**
	 * Creates a new player and add it to the game.
	 * @param name The name of the new player.
	 * @return The newly instantiated player.
	 */
	public Player AddPlayer(String name)
	{
		Map currentMap = Globals.GameSettings.GetCurrentMap();
		Vector2 playerStartPos = currentMap.GetRandomSpawnPos();
		currentMap.InvalidSpawnPositions.add(playerStartPos);
		
		return Globals.ObjectController.CreatePlayer(playerStartPos, name);
	}
	
	/**
	 * Removes a player from the game. If there are no more players afterwards, the game ends.
	 * @param player The player to remove.
	 */
	public void RemovePlayer(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		// Decrementing ensures we don't skip the next characters turn when removing the current player
		if (GetCurrentCharacter() == player)
		{
			CurrentCharacterIndex--;
		}
		
		// Synchronising so we can remove items while iterating
		synchronized (Globals.ObjectController.LoadedPlayers)
		{
			Globals.ObjectController.DestroyPlayer(player);
		}
		
		// Last player is being removed
		if (Globals.ObjectController.LoadedPlayers.size() == 0)
		{
			Globals.UserIO.WriteToUser("Last player has been eliminated, ending game.");
			ExitGame();
		}
	}
	
	/**
	 * Removes a player from the game. If there are no more players afterwards, the game ends.
	 * @param name The name of the player being removed.
	 */
	public void RemovePlayer(String name)
	{
		Player player = GetPlayerByName(name);
		RemovePlayer(player);
	}
	
	/**
	 * Gets the characters who's turn it currently is.
	 * @return The current character. Returns null if the game has no characters.
	 */
	public CharacterBase GetCurrentCharacter()
	{
		if (GetCurrentCharacterCount() == 0)
		{
			return null;
		}
		
		List<Player> players = Globals.ObjectController.LoadedPlayers;
		List<Bot> bots = Globals.ObjectController.LoadedBots;
		
		if (CurrentCharacterIndex < players.size())
		{
			return players.get(CurrentCharacterIndex);
		}
		else
		{
			return bots.get(CurrentCharacterIndex - players.size());
		}
	}
	
	/**
	 * Gets the total number of characters left in the game.
	 * @return The total remaining characters.
	 */
	private int GetCurrentCharacterCount()
	{
		return Globals.ObjectController.LoadedPlayers.size() + Globals.ObjectController.LoadedBots.size();
	}
	
	/**
	 * Searches for and returns the first player with the given name.
	 * @param name The name to search for.
	 * @return The player with the given name. Returns null if no player has the given name.
	 */
	public Player GetPlayerByName(String name)
	{
		for (Player player: Globals.ObjectController.LoadedPlayers)
		{
			if (name.equals(player.Name))
			{
				return player;
			}
		}
		return null;
	}
	
	/**
	 * Gets whether the game has started.
	 * @return Whether the game has started.
	 */
	public boolean GetGameStarted()
	{
		return GameStarted;
	}
	
	/**
	 * Exits the program immediately.
	 */
	public static void ExitGame()
	{
		System.exit(0);
	}
	
	/**
	 * Displays total amount of gold required for the human player to be eligible to win.
	 */
	private void Hello()
	{
		int goldToWin = Globals.GameSettings.GetCurrentMap().GetGoldNeeded();
		Globals.UserIO.WriteToUser("Gold to win: %d", goldToWin);
	}
	
	/**
	 * Displays the current gold owned.
	 */
	private void Gold()
	{
		int goldOwned = Globals.GameSettings.GetCurrentPlayer().GetGoldCollected();
		Globals.UserIO.WriteToUser("Gold collected: %d", goldOwned);
	}
	
	/**
	 * Moves a player one square in the given cardinal direction.
	 * @param character The character to move.
	 * @param directionStr The direction to move in.
	 * @return A string responding to this action.
	 */
	private String Move(CharacterBase character, String directionStr)
	{
		String ret = "";
		
		if (character != null && directionStr != null && directionStr.strip().length() > 0)
		{
			Vector2 direction = Vector2.CardinalToVector(directionStr);
			Vector2 target = character.GetNewPos(direction, 1);
			boolean success = character.TryMove(target);
			
			ret = String.format("%s%s\n", character instanceof Bot ? "\n" : "", success ? "Success" : "Fail");
		}
		return ret;
	}
	
	/**
	 * Picks up the gold on the player's current location.
	 * @param player The player attempting to pickup gold.
	 * @return A string responding to this action.
	 */
	private String Pickup(Player player)
	{
		boolean success = player.TryPickupGold();
		return String.format("%s. Gold owned: %d", success ? "Success" : "Fail", player.GetGoldCollected());
	}
	
	/**
	 * Shows a 5x5 area of the map around a character.
	 * @param character The character to make the area relative to.
	 * @return A string representing the area. Returns an empty string if the character is a bot.
	 */
	private String Look(CharacterBase character)
	{
		String ret = "";
		
		if (character == null)
		{
			return ret;
		}
		
		// Combining players & bots into one list
		List<CharacterBase> allCharacters = Stream.of(Globals.ObjectController.LoadedPlayers, Globals.ObjectController.LoadedBots).flatMap(Collection::stream).collect(Collectors.toList());
		
		char[][] immediateArea = Globals.GameSettings.GetCurrentMap().GetImmediateArea(character.Position, Globals.GameSettings.GetCharacterViewDistance(), allCharacters);
		character.LastKnownArea = immediateArea;
		
		// Printing the immediate area for players.
		if (character instanceof Player)
		{
			StringBuilder areaString = new StringBuilder();
			
			for (int row = 0; row < immediateArea.length; row++)
			{
				for (int col = 0; col < immediateArea[row].length; col++)
				{
					areaString.append(immediateArea[row][col]);
				}
				
				if (row < immediateArea.length - 1)
				{
					areaString.append("\n");
				}
			}
			
			ret = areaString.toString();
		}
		
		return ret;
	}
	
	/**
	 * Quits the game. The player wins if they're standing at an exit and have enough gold to leave, otherwise they lose.
	 * @param player The player trying to quit.
	 */
	private void Quit(Player player)
	{
		if (player.HasWon())
		{
			Globals.UserIO.WriteToUser(String.format("%s has won the game!", player.GetName()));
			ExitGame();
		}
		else
		{
			Globals.UserIO.WriteToUser(String.format("%s tried to exit early, they are eliminated.", player.GetName()));
			RemovePlayer(player.GetName());
		}
	}
}
