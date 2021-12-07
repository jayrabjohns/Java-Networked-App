import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.IllegalFormatException;

/**
 * Deals with IO related to the user via the console.
 */
public class ConsoleIO
{
	// Defining ANSI escape codes
	public static final String TextColourReset = "\u001B[0m";
	public static final String TextColourRed = "\u001B[31m";
	public static final String TextColourYellow = "\u001B[33m";
	public static final String TextColourGreen = "\u001B[32m";
	
	private volatile BufferedReaderEx BufferedReaderEx = null;
	
	/**
	 * Waits for reader to be ready, then reads input.
	 * Getting an input via this method allows the thread to be interrupted.
	 *
	 * @return The given input.
	 * @throws InterruptedException If any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
	 * @throws IOException If an I/O error occurs.
	 */
	public String GetInput() throws InterruptedException, IOException
	{
		// Creating new stream reader if previous one was destroyed
		if (BufferedReaderEx == null)
		{
			BufferedReaderEx = new BufferedReaderEx(new InputStreamReader(System.in));
		}
		
		return BufferedReaderEx.ReadLineWhenReady();
	}
	
	/**
	 * Immediately stops any blocking calls waiting for a user input.
	 * To have any affect this must be called from a thread other than the one being blocked.
	 */
	public void StopInputs()
	{
		try
		{
			BufferedReaderEx.close();
		}
		catch (IOException e)
		{
			// Stream closed by other thread
		}
		BufferedReaderEx = null;
	}
	
	/**
	 * Logs a string to the console.
	 *
	 * @param str A format string.
	 * @param args Arguments reference in the format string.
	 */
	public static void Log(String str, Object... args)
	{
		String timeString = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
		
		try
		{
			str = String.format(str, args);
			System.out.printf("[%s] %s%n", timeString, str);
		}
		catch (NullPointerException | IllegalFormatException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Logs an error in the console.
	 *
	 * @param str A format string.
	 * @param args Arguments reference in the format string.
	 */
	public static void LogError(String str, Object... args)
	{
		str = String.format(str, args);
		Log("%s%s%s", TextColourRed, str, TextColourReset);
	}
	
	/**
	 * Logs a string to console in a highlighted colour, making it easier to differentiate from other logs.
	 *
	 * @param str A format string.
	 * @param args Arguments reference in the format string.
	 */
	public static void LogHighlight(String str, Object... args)
	{
		str = String.format(str, args);
		Log("%s%s%s", TextColourGreen, str, TextColourReset);
	}
}
