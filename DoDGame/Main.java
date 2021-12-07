package DoDGame;

/**
 * Contains main().
 */
public class Main
{
	/**
	 * The entry point to the program.
	 */
	public static void main(String[] args)
	{
		GameController gameController = new GameController();
		gameController.Start();
		
		while (true)
		{
			gameController.Update();
		}
	}
}
