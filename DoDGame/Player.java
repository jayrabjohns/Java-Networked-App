package DoDGame;

/**
 * A character controlled by the player.
 */
public class Player extends CharacterBase
{
	private int GoldCollected = 0;
	
	/**
	 * Don't directly create players, instead use ObjectController.
	 */
	public Player(Vector2 startPos, String name)
	{
		super(startPos, name);
	}
	
	/**
	 * Tries to pickup gold off the current tile.
	 * @return Whether gold was successfully picked up.
	 */
	public boolean TryPickupGold()
	{
		// Getting current column / row
		Map currentMap = Globals.GameSettings.GetCurrentMap();
		int currentCol = currentMap.GetColumn(Position.X);
		int currentRow = currentMap.GetRow(Position.Y);
		
		// Checking if gold can be picked up from the current tile
		if (currentMap.GetCharAtPos(currentCol, currentRow) == Globals.GameSettings.GetMapChar("gold")
				&& currentMap.TryChangeCharAtPos(currentCol, currentRow, "empty"))
		{
			GoldCollected++;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the player could win this turn.
	 * @return Whether this player could win this turn.
	 */
	public boolean HasWon()
	{
		Map currentMap = Globals.GameSettings.GetCurrentMap();
		int currentCol = currentMap.GetColumn(Position.X);
		int currentRow = currentMap.GetRow(Position.Y);
		
		boolean atExit = currentMap.GetCharAtPos(currentCol, currentRow) == Globals.GameSettings.GetMapChar("exit");
		boolean hasEnoughGold = GoldCollected >= currentMap.GetGoldNeeded();
		
		return atExit && hasEnoughGold;
	}
	
	@Override
	public String[] ChooseCommand()
	{
		// Gets an input from the console and splits by white space
		String[] inputs = Globals.UserIO.GetInput("\nEnter command: ").split("\\s+");
		
		if (inputs.length > 0)
		{
			inputs[0] = inputs[0].toLowerCase();
		}
		
		return inputs;
	}
	
	public int GetGoldCollected()
	{
		return GoldCollected;
	}
}
