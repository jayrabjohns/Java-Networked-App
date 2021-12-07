package DoDGame;

import java.io.InputStreamReader;
import java.util.Random;

/**
 * Keeps references needed by all classes.
 */
public abstract class Globals
{
	public final static ObjectController ObjectController = new ObjectController();
	public final static UserIO UserIO = new UserIO();
	public final static GameSettings GameSettings = new GameSettings();
	public final static FileIO FileIO = new FileIO();
	public final static Random RNG = new Random();
}
