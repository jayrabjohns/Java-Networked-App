package DoDGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a map of a dungeon.
 */
public class Map
{
	public final Set<Vector2> InvalidSpawnPositions = new HashSet<>();
	
	private String Name;
	private int GoldNeeded;
	private final char[][] Layout;
	private final int LayoutWidth;
	private final int LayoutHeight;
	
	/**
	 * Don't directly create maps, instead use ObjectController.
	 */
	public Map(String name, int goldNeeded, char[][] layout)
	{
		Name = name;
		GoldNeeded = goldNeeded;
		Layout = layout;
		LayoutHeight = layout.length;
		LayoutWidth = LayoutHeight > 0 ? Layout[0].length : 0;
	}
	
	/**
	 * Gets the char at the given position of the layout.
	 * @param col The column of the char.
	 * @param row The row of the char.
	 * @return : The character at a given position. Returns the null character if given an invalid row / column.
	 */
	public char GetCharAtPos(int col, int row)
	{
		if (col < 0 || row < 0)
		{
			return Globals.GameSettings.GetMapChar("wall");
		}
		else if (col < GetLayoutWidth() && row < GetLayoutHeight())
		{
			return Layout[row][col];
		}
		else
		{
			return '\u0000';
		}
	}
	
	/**
	 * Attempts to change the char at a given position.
	 * @param col The column of the char to change.
	 * @param row The row of the char to change.
	 * @param mapCharKey The key of the replacement map char.
	 * @return Whether the replacement was successful.
	 */
	public boolean TryChangeCharAtPos(int col, int row, String mapCharKey)
	{
		char mapChar = Globals.GameSettings.GetMapChar(mapCharKey);
		if (row < GetLayoutHeight() && col < GetLayoutWidth() && mapChar != '\u0000')
		{
			Layout[row][col] = mapChar;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks whether a given position is traversable.
	 * @param position The position to check.
	 * @return Whether the position is traversable.
	 */
	public boolean IsPositionTraversable(Vector2 position)
	{
		// Getting column / row of position
		int newCol = GetColumn(position.X);
		int newRow = GetRow(position.Y);
		
		// Checking position is traversable
		char newChar = GetCharAtPos(newCol, newRow);
		boolean traversable = newChar != Globals.GameSettings.GetMapChar("wall") && newChar != '\u0000';
		
		return traversable;
	}
	
	/**
	 * Gets a random spawn point in the map.
	 * @return The chosen spawn point. Returns null if there are no available spawn positions.
	 */
	public Vector2 GetRandomSpawnPos()
	{
		// Getting all valid positions
		List<Vector2> validSpawnPoints = new ArrayList<>();
		for (int row = 0; row < GetLayoutHeight(); row++)
		{
			for (int col = 0; col < GetLayoutWidth(); col++)
			{
				Vector2 currentPos = new Vector2(GetX(col), GetY(row));
				char mapChar = GetCharAtPos(col, row);
				
				if (mapChar != Globals.GameSettings.GetMapChar("wall")
						&& mapChar != Globals.GameSettings.GetMapChar("gold")
						&& !InvalidSpawnPositions.contains(currentPos))
				{
					validSpawnPoints.add(currentPos);
				}
			}
		}
		
		// Getting a random pos from all valid positions
		if (validSpawnPoints.size() > 0)
		{
			int posIndex = Globals.RNG.nextInt(validSpawnPoints.size());
			return validSpawnPoints.get(posIndex);
		}
		
		return null;
	}
	
	/**
	 * Gets a square area of the layout around a position.
	 * @param centrePos Where the area is relative to.
	 * @param areaLength The length of the square area.
	 * @param characters A list of characters to take into account.
	 * @return A 2D char array representing the area around a given position.
	 */
	public char[][] GetImmediateArea(Vector2 centrePos, int areaLength, List<CharacterBase> characters)
	{
		if (centrePos == null || characters == null)
		{
			return null;
		}
		
		// Converting the centre position to rows / columns
		Map currentMap = Globals.GameSettings.GetCurrentMap();
		int centreCol = currentMap.GetColumn(centrePos.X);
		int centreRow = currentMap.GetRow(centrePos.Y);
		
		char[][] immediateArea = new char[areaLength][areaLength];
		int localCol = 0;
		int localRow = 0;
		
		for (int worldRow = centreRow - (areaLength / 2); worldRow <= centreRow + (areaLength / 2); worldRow++)
		{
			localCol = 0;
			for (int worldCol = centreCol - (areaLength / 2); worldCol <= centreCol + (areaLength / 2); worldCol++)
			{
				char mapChar = currentMap.GetCharAtPos(worldCol, worldRow);
				
				// Representing invalid / out of bounds chars as walls
				mapChar = mapChar == '\u0000' ? Globals.GameSettings.GetMapChar("wall") : mapChar;
				
				// Adding players / bots
				for (CharacterBase character: characters)
				{
					Vector2 pos = character.Position;
					if (worldCol == currentMap.GetColumn(pos.X) && worldRow == currentMap.GetRow(pos.Y))
					{
						if (character instanceof Player)
						{
							mapChar = 'P';
						}
						else if (character instanceof Bot)
						{
							mapChar = 'B';
						}
					}
				}
				
				immediateArea[localRow][localCol] = mapChar;
				localCol++;
			}
			localRow++;
		}
		
		return immediateArea;
	}
	
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < Layout.length; i++)
		{
			stringBuilder.append(Layout[i]);
			stringBuilder.append("\n");
		}
		
		return stringBuilder.toString();
	}
	
	/**
	 * Converts an x position into a column.
	 * @param x X to convert.
	 * @return Column represented by the given x value.
	 */
	public int GetColumn(float x) { return (int)x; }
	
	/**
	 * Conerts a y position into a row.
	 * @param y Y to convert.
	 * @return Row represented by the given y position.
	 */
	public int GetRow(float y) { return (int)(GetLayoutHeight() - 1 - y); }
	
	/**
	 * Converts a column into an x position.
	 * @param column Column to convert.
	 * @return X position represented by the given column.
	 */
	public float GetX(int column) { return (float)column; }
	
	/**
	 * Converts a row into a y position.
	 * @param row Row to convert.
	 * @return Y position represented by the given row.
	 */
	public float GetY(int row) { return (float)(GetLayoutHeight() - 1 - row); }
	
	public String GetName() { return Name; }
	
	public int GetGoldNeeded() { return GoldNeeded; }
	
	public int GetLayoutWidth() { return LayoutWidth; }
	
	public int GetLayoutHeight() { return LayoutHeight; }
}
