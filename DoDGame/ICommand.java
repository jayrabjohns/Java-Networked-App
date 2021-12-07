package DoDGame;

/**
 * A command given by a character which translates to some action in-game.
 */
public interface ICommand
{
	/**
	 * The method run when a command is executed.
	 * @param character The character executing this command
	 * @param arg General purpose argument, may be used differently by different commands.
	 * @return A sentence describing the result after execution.
	 */
	String Invoke(CharacterBase character, String arg);
}
