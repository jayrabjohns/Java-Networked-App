package DoDGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Use ObjectController to initialise and cleanup objects.
 */
public class ObjectController
{
	/* Enemies */
	public final List<Bot> LoadedBots = new ArrayList<>();
	
	/**
	 * Creates a bot.
	 * @param startPos The starting position of the bot.
	 * @return The newly created bot.
	 */
	public Bot CreateBot(Vector2 startPos)
	{
		Bot bot = new Bot(startPos);
		LoadedBots.add(bot);
		return bot;
	}
	
	/**
	 * Destroys a bot.
	 * @param bot The bot to destroy.
	 * @return Whether the bot was successfully destroyed.
	 */
	public boolean DestroyBot(Bot bot)
	{
		int index = LoadedBots.indexOf(bot);
		if (index != -1)
		{
			LoadedBots.remove(index);
			bot = null;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/* Players */
	public final List<Player> LoadedPlayers = new ArrayList<>();
	
	/**
	 * Creates a player.
	 * @param startPos The starting position of the player.
	 * @return The newly created player.
	 */
	public Player CreatePlayer(Vector2 startPos, String name)
	{
		Player player = new Player(startPos, name);
		LoadedPlayers.add(player);
		return player;
	}
	
	/**
	 * Destroys a player.
	 * @param player The player to destroy.
	 * @return Whether the player was successfully destroyed.
	 */
	public boolean DestroyPlayer(Player player)
	{
		int index = LoadedPlayers.indexOf(player);
		if (index != -1)
		{
			LoadedPlayers.remove(index);
			player = null;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/* Maps */
	private List<Map> LoadedMaps = new ArrayList<>();
	
	/**
	 * Creates a map.
	 * @param name Name of the map.
	 * @param goldNeeded Gold needed to be collected before progressing.
	 * @param layout The map layout.
	 * @return The newly created map.
	 */
	public Map CreateMap(String name, int goldNeeded, char[][]layout)
	{
		Map map = new Map(name, goldNeeded, layout);
		LoadedMaps.add(map);
		return map;
	}
	
	/**
	 * Creates an instance of a Map from a map file.
	 * @param filePath Path of the map file.
	 * @return The newly created map.
	 */
	public Map CreateMap(String filePath)
	{
		List<String> fileContents = new ArrayList<>();
		if (!Globals.FileIO.TryGetTxtFileContents(filePath, fileContents))
		{
			return null;
		}
		
		String name = null;
		int goldNeeded = 0;
		char[][] layout;
		
		// Check for map properties
		for (int i = fileContents.size() - 1; i >= 0; i--)
		{
			String line = fileContents.get(i);
			String namePrefix = Globals.GameSettings.GetMapNameFilePrefix();
			String goldNeededPrefix = Globals.GameSettings.GetMapGoldNeededFilePrefix();
			
			// Checking for name
			if (line.length() >= namePrefix.length()
					&& Objects.equals(line.substring(0, namePrefix.length()), namePrefix))
			{
				name = line.substring(namePrefix.length());
				fileContents.remove(i);
			}
			
			// Checking for gold needed
			else if (line.length() >= goldNeededPrefix.length()
					&& Objects.equals(line.substring(0, goldNeededPrefix.length()), goldNeededPrefix))
			{
				String goldString = line.substring(goldNeededPrefix.length());
				fileContents.remove(i);
				
				try
				{
					goldNeeded = Integer.parseInt(goldString);
					goldNeeded = goldNeeded < 0 ? 0 : goldNeeded;
				}
				catch (NumberFormatException e)
				{
					// Nothing to do.
				}
			}
		}
		
		// Getting longest line in layout
		int maxLength = 0;
		for (int i = 0; i < fileContents.size(); i++)
		{
			String line = fileContents.get(i);
			maxLength = line.length() > maxLength ? line.length() : maxLength;
		}
		
		// Using rest of file contents as map layout
		layout = new char[fileContents.size()][maxLength];
		for (int i = 0; i < fileContents.size(); i++)
		{
			char[] fileLine = fileContents.get(i).toCharArray();
			char[] layoutLine;
			
			// Creating new array if layout line is too short
			if (fileLine.length == maxLength)
			{
				layoutLine = fileLine;
			}
			else
			{
				layoutLine = new char[maxLength];
				System.arraycopy(fileLine, 0, layoutLine, 0, fileLine.length);
			}
			
			// Dealing with invalid / missing characters
			for (int j = 0; j < layoutLine.length; j++)
			{
				if (j >= fileLine.length || !Globals.GameSettings.IsValidMapChar(layoutLine[j]))
				{
					layoutLine[j] = Globals.GameSettings.GetMapChar("wall");
				}
			}
			
			layout[i] = layoutLine;
		}
		
		// Dealing with layouts that are too small / without a name
		if (name == null || layout.length < Globals.GameSettings.GetMinMapHeight() || maxLength < Globals.GameSettings.GetMinMapWidth())
		{
			return null;
		}
		
		return CreateMap(name, goldNeeded, layout);
	}
	
	/**
	 * Destroys a map.
	 * @param map The map to destroy.
	 * @return Whether the map was successfully destroyed.
	 */
	public boolean DestroyMap(Map map)
	{
		int index = LoadedMaps.indexOf(map);
		if (index != -1)
		{
			LoadedMaps.remove(index);
			return true;
		}
		else
		{
			return false;
		}
	}
}
