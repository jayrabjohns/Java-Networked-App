package DoDGame;

import java.util.ArrayList;
import java.util.List;

/**
 * An enemy which follows the player and wins by reaching the same position as the player.
 */
public class Bot extends CharacterBase
{
	private int TurnsSinceLastLook = 0;
	private final int MaxTurnsBetweenLooking = 3;
	private Vector2 LastLookPos;
	
	private boolean	FoundPlayer = false;
	private Vector2 LastKnownPlayerPos;
	
	/**
	 * Don't directly create bots, Instead use ObjectController.
	 */
	public Bot(Vector2 startPos)
	{
		super(startPos, "Bot");
	}
	
	/**
	 * Chooses the best move for the bot.
	 *
	 * Every few turns it uses 'look' to see its surroundings
	 *
	 * If it hasn't found the player, it moves in a random valid direction.
	 *
	 * If it has found the player it moves towards the last know position of the player.
	 *
	 * It prioritises the direction in which it can cover the most ground toward the player,
	 * i.e. in the direction of the largest component of the direction vector to the player.
	 *
	 * @return The command to play this turn.
	 */
	@Override
	public String[] ChooseCommand()
	{
		String command = "";
		String arg = "";
		
		// Getting valid moves from this position
		String[] directions = { "n", "e", "s", "w" };
		List<String> validMoves = new ArrayList<>();
		for (String dir: directions)
		{
			Vector2 newPos = GetNewPos(Vector2.CardinalToVector(dir), 1);
			if (Globals.GameSettings.GetCurrentMap().IsPositionTraversable(newPos))
			{
				validMoves.add(dir);
			}
		}
		
		// Checking new look data if we used look last turn
		if (TurnsSinceLastLook == 1 && LastKnownArea != null)
		{
			int areaLength = Globals.GameSettings.GetCharacterViewDistance();
			Map currentMap = Globals.GameSettings.GetCurrentMap();
			FoundPlayer = false;
			
			for (int row = 0; row < LastKnownArea.length; row++)
			{
				for (int col = 0; col < LastKnownArea[row].length; col++)
				{
					char currentChar = LastKnownArea[row][col];
					
					// Updating last known player pos
					if (currentChar == Globals.GameSettings.GetMapChar("player"))
					{
						FoundPlayer = true;
						
						// Converting local to world pos
						int mapCol = currentMap.GetColumn(LastLookPos.X) - (areaLength / 2) + col;
						int mapRow = currentMap.GetRow(LastLookPos.Y) + (areaLength / 2) - row;
						Vector2 worldPos = new Vector2(currentMap.GetX(mapCol), currentMap.GetY(mapRow));
						
						LastKnownPlayerPos = worldPos;
					}
				}
			}
		}
		
		// Uses look every few turns to keep information up to date
		if (LastKnownArea == null || TurnsSinceLastLook > MaxTurnsBetweenLooking)
		{
			Globals.UserIO.WriteToUser("Looking around the area...");
			command = "look";
			LastLookPos = Position;
			TurnsSinceLastLook = 0;
		}
		
		// Chasing player if they're found
		else if (FoundPlayer)
		{
			Globals.UserIO.WriteToUser("Chasing player!");
			command = "move";
			
			// Getting direction from bot position to last known player position
			Vector2 playerDir = Vector2.Sub(LastKnownPlayerPos, Position).Normalised();
			
			// Choosing to move in the direction of the largest component
			if (Math.abs(playerDir.Y) > Math.abs(playerDir.X))
			{
				arg = playerDir.Y < 0 ? "N" : "S";
			}
			else
			{
				arg = playerDir.X > 0 ? "E" : "W";
			}
		}
		
		// Player not found
		else
		{
			Globals.UserIO.WriteToUser("Searching for player...");
			command = "move";
			arg = validMoves.size() > 0 ? validMoves.get(Globals.RNG.nextInt(validMoves.size())) : "";
		}
		TurnsSinceLastLook++;
		
		// Converting command / args to array
		String[] inputs = arg.length() > 0 ? new String[2] : new String[1];
		inputs[0] = command;
		
		if (inputs.length > 1)
		{
			inputs[1] = arg;
		}
		
		return inputs;
	}
}
