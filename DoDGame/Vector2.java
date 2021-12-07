package DoDGame;

/**
 * Representation of a 2D vector or point.
 */
public class Vector2
{
	public float X, Y;
	
	/**
	 * Short hand for 'new Vector2(0.0f, 1.0f)'.
	 */
	public static final Vector2 Up = new Vector2(0.0f, 1.0f);
	
	/**
	 * Short hand for 'new Vector2(0.0f, -1.0f)'.
	 */
	public static final Vector2 Down = new Vector2(0.0f, -1.0f);
	
	/**
	 * Short hand for 'new Vector2(1.0f, 0.0f)'.
	 */
	public static final Vector2 Right = new Vector2(1.0f, 0.0f);
	
	/**
	 * Short hand for 'new Vector2(-1.0f, 0.0f)'.
	 */
	public static final Vector2 Left = new Vector2(-1.0f, 0.0f);
	
	/**
	 * Constructs a new 2D vector with given x, y components.
	 * @param x The vectors X component.
	 * @param y The vectors Y component.
	 */
	public Vector2(float x, float y)
	{
		X = x;
		Y = y;
	}
	
	/**
	 * Gets the length of this vector.
	 * @return The length of this vector.
	 */
	public float Magnitude()
	{
		return (float)Math.sqrt(X * X + Y * Y);
	}
	
	/**
	 * Gets a unit vector with the same direction as this vector.
	 * @return A new instance of this vector with a magnitude of one.
	 */
	public Vector2 Normalised()
	{
		float length = Magnitude();
		return new Vector2(X / length, Y / length);
	}
	
	/**
	 * Adds two vectors.
	 * @param v1 A vector to add.
	 * @param v2 A vector to add.
	 * @return A new vector equal to the sum of v1, v2.
	 */
	public static Vector2 Add(Vector2 v1, Vector2 v2) { return new Vector2(v1.X + v2.X, v1.Y + v2.Y); }
	
	/**
	 * Multiplies a vector by a scalar.
	 * @param vector The vector to multiply.
	 * @param scalar The scalar to multiply by.
	 * @return A new vector equal to the product of the given parameters.
	 */
	public static Vector2 Mul(Vector2 vector, int scalar) { return new Vector2(vector.X * scalar, vector.Y * scalar); }
	
	/**
	 * Subtracts one vector from another.
	 * @param v1 The vector to subtract from.
	 * @param v2 The vector to subtract by.
	 * @return A new vector equal to the subtraction of the parameters.
	 */
	public static Vector2 Sub(Vector2 v1, Vector2 v2) { return new Vector2(v1.X - v2.X, v1.Y - v2.Y); }
	
	/**
	 * Checks equality this and one other vector.
	 * @param vector Vector to compare to.
	 * @return Whether this and the given vector are equal.
	 */
	public boolean Equals(Vector2 vector) { return Float.compare(X, vector.X) == 0 && Float.compare(Y, vector.Y) == 0; }
	
	/**
	 * Converts a cardinal direction (N, E, S, W) to a direction vector.
	 * @param cardinalDirection Cardinal direction to convert.
	 * @return A new unit vector in the same direction as the given cardinal vector.
	 */
	public static Vector2 CardinalToVector(String cardinalDirection)
	{
		cardinalDirection = cardinalDirection.toUpperCase();
		Vector2 ret = null;
		
		switch (cardinalDirection)
		{
			case "N":
				ret = Vector2.Up;
				break;
			case "E":
				ret = Vector2.Right;
				break;
			case "S":
				ret = Vector2.Down;
				break;
			case "W":
				ret = Vector2.Left;
				break;
			default:
				break;
		}
		
		return ret;
	}
}
