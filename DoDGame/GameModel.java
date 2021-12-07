package DoDGame;

import java.io.File;
import java.util.HashMap;

/**
 *  Model object for maps.
 */
class MapModel
{
	public final HashMap<String, Character> MapCharacters;
	public final String NameFilePrefix;
	public final String GoldNeededFilePrefix;
	public final String MapFolderPath;
	public final int MinMapHeight;
	public final int MinMapWidth;
	
	public MapModel(HashMap<String, Character> mapCharacters, String nameFilePrefix, String goldNeededFilePrefix, String mapFolderPath, int minMapHeight, int minMapWidth)
	{
		MapCharacters = mapCharacters;
		NameFilePrefix = nameFilePrefix;
		GoldNeededFilePrefix = goldNeededFilePrefix;
		MapFolderPath = mapFolderPath;
		MinMapHeight = minMapHeight;
		MinMapWidth = minMapWidth;
	}
}

/**
 * Model object for a character.
 */
class CharacterModel
{
	public final int ViewDistance;
	
	public CharacterModel(int viewDistance)
	{
		ViewDistance = viewDistance;
	}
}

/**
 * Model object for a bot.
 */
class BotModel
{
	public final int NumberOfBots;
	
	public BotModel(int numberOfBots)
	{
		NumberOfBots = numberOfBots;
	}
}

/**
 * Contains all game models.
 */
public class GameModel
{
	private final MapModel MapModel;
	private final CharacterModel CharacterModel;
	private final BotModel BotModel;
	
	public GameModel()
	{
		// TODO store model objects as JSON files.
		HashMap<String, Character> mapCharacters = new HashMap<>();
		mapCharacters.put("empty", '.');
		mapCharacters.put("wall", '#');
		mapCharacters.put("bot", 'B');
		mapCharacters.put("player", 'P');
		mapCharacters.put("gold", 'G');
		mapCharacters.put("exit", 'E');
		
		String mapSubFolder = "";// = "Maps;"
		MapModel = new MapModel(mapCharacters, "name ", "win ", System.getProperty("user.dir") + File.separator + mapSubFolder,  3, 3);
		CharacterModel = new CharacterModel(5);
		BotModel = new BotModel(1);
	}
	
	public MapModel GetMapModel() { return MapModel; }
	
	public CharacterModel GetCharacterModel() { return CharacterModel; }
	
	public BotModel GetBotModel() { return BotModel; }
}