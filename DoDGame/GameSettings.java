package DoDGame;

/**
 * Keeps track of data about the current game and acts as the View Model for GameModel.
 */
public class GameSettings
{
	private final GameModel Model = new GameModel();
	private Map CurrentMap;
	private Player CurrentPlayer;
	
	/* Model Accessors */
	public boolean IsValidMapChar(char mapChar) { return Model.GetMapModel().MapCharacters.containsValue(mapChar); }
	
	/**
	 * Gets a char from the map model using the given key.
	 * @param key Key of the required map char.
	 * @return The character associated with the given key. Returns the null character if no character is associated.
	 */
	public char GetMapChar(String key)
	{
		Character mapChar = Model.GetMapModel().MapCharacters.get(key);
		return mapChar != null ? mapChar : '\u0000';
	}
	
	public String GetMapNameFilePrefix() { return Model.GetMapModel().NameFilePrefix; }
	
	public String GetMapGoldNeededFilePrefix() { return Model.GetMapModel().GoldNeededFilePrefix; };
	
	public String GetMapFolderPath() { return Model.GetMapModel().MapFolderPath; }
	
	public int GetCharacterViewDistance() { return Model.GetCharacterModel().ViewDistance; }
	
	public int GetMinMapHeight() { return Model.GetMapModel().MinMapHeight; }
	
	public int GetMinMapWidth() { return Model.GetMapModel().MinMapWidth; }
	
	public int GetNumberOfBots() { return Model.GetBotModel().NumberOfBots; }
	
	/* Game data. */
	public Map GetCurrentMap() { return CurrentMap; }
	
	public boolean SetCurrentMap(Map map)
	{
		if (CurrentMap == null)
		{
			CurrentMap = map;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Player GetCurrentPlayer()
	{
		return CurrentPlayer;
	}
	
	public void SetCurrentPlayer(Player player)
	{
		CurrentPlayer = player;
	}
}
