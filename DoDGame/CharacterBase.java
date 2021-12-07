package DoDGame;

import java.util.Vector;

/**
 * Base class for all players, enemies.
 */
public abstract class CharacterBase
{
	protected Vector2 Position;
	protected String Name;
	protected char[][] LastKnownArea;
	
	public CharacterBase(Vector2 startPos, String name)
	{
		Position = startPos;
		Name = name;
	}
	
	/**
	 * Tries to move to a new target position.
	 * @param target The position to move to.
	 * @return Whether the move was successful.
	 */
	protected boolean TryMove(Vector2 target)
	{
		if (target == null)
		{
			return false;
		}
		
		boolean moved = false;
		
		if (Globals.GameSettings.GetCurrentMap().IsPositionTraversable(target))
		{
			// Moving to new position
			Position = target;
			moved = true;
		}
		
		return moved;
	}
	
	/**
	 * Gets a new position relative the character.
	 * @param direction Direction of the target position.
	 * @param distance Distanc of the target position.
	 * @return The new position. Returns null if direction is null.
	 */
	public Vector2 GetNewPos(Vector2 direction, int distance)
	{
		if (direction == null)
		{
			return null;
		}
		
		// Getting the targeted position
		direction = direction.Normalised();
		Vector2 move = Vector2.Mul(direction, distance);
		Vector2 newPos = Vector2.Add(Position, move);
		
		return newPos;
	}
	
	/**
	 * Decides what command will be played this turn.
	 * @return The command to be played this turn.
	 */
	public abstract String[] ChooseCommand();
	
	/**
	 * Gets the character's name.
	 * @return The character's name.
	 */
	public String GetName()
	{
		return Name;
	}
}
