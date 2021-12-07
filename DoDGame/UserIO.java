package DoDGame;

import java.io.*;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Deals with all IO related to the user via the console.
 */
public class UserIO
{
	private BufferedReader InputStream = new BufferedReader(new InputStreamReader(System.in));
	private PrintWriter OutStream = new PrintWriter(System.out);
	
	/**
	 * Changes the in & out streams used.
	 * @param in Stream to read inputs from.
	 * @param out Stream to write outputs to.
	 */
	public void SetInOutStreams(BufferedReader in, PrintWriter out)
	{
		// Not cleaning up streams because they are passed in by some other class, they should be responsible for cleaning them up.
		
		InputStream = in;
		OutStream = out;
	}
	
	/**
	 * Displays a string to the console. Deals with exceptions thrown by printf.
	 * @param format A format string.
	 * @param args Arguments referenced in the format string.
	 */
	public void WriteToUser(String format, Object... args)
	{
		try
		{
			String outString = String.format(format, args);
			OutStream.println(outString);
			OutStream.flush();
		}
		catch (NullPointerException | IllegalFormatException e)
		{
			// Nothing to do
		}
	}
	
	/**
	 * Gets an input from the user via the console.
	 * @param prompt A prompt on what the user should enter.
	 * @return The input given by the user.
	 */
	public String GetInput(String prompt)
	{
		WriteToUser(prompt);
		try
		{
			return InputStream.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Gets an input and treats it like an integer.
	 * @param prompt A prompt on what the user should enter.
	 * @return The number entered by the user. Returns null if a valid number wasn't entered.
	 */
	public Integer GetInputInt(String prompt)
	{
		Integer ret = null;
		
		try
		{
			String inputString = GetInput(prompt).strip();
			ret = Integer.parseInt(inputString);
		}
		catch (NumberFormatException e)
		{
			// Nothing to do
		}
		
		return ret;
	}
	
	/**
	 * Displays all valid maps and gets the user to pick one.
	 * @return The map chosen by the user
	 */
	public Map SelectMap(boolean randomMap)
	{
		String mapsDirPath = Globals.GameSettings.GetMapFolderPath();
		String[] paths = Globals.FileIO.GetAllFilesInDir(mapsDirPath);
		
		// Getting a list of all valid maps
		List<Map> maps = new ArrayList<>(paths.length);
		for (int i = 0; i < paths.length; i++)
		{
			Map map = Globals.ObjectController.CreateMap(paths[i]);
			// Making sure map has at least one space free
			if (map != null && map.GetRandomSpawnPos() != null)
			{
				maps.add(map);
			}
		}
		
		// Dealing with no maps being found
		if (maps.size() == 0)
		{
			Globals.UserIO.WriteToUser("No map files found in %s, stopping the game.", mapsDirPath);
			GameController.ExitGame();
		}
		
		int mapIndex = -1;
		
		// Choosing map
		if (randomMap)
		{
			WriteToUser("Picking random map...");
			mapIndex = Globals.RNG.nextInt(maps.size());
		}
		else
		{
			// Printing map names
			for (int i = 0; i < maps.size(); i++)
			{
				WriteToUser("%s.%s", Integer.toString(i + 1), maps.get(i).GetName());
			}
			
			Integer mapNum = null;
			boolean inputIsValid = false;
			
			// Getting valid map index
			while (!inputIsValid)
			{
				mapNum = GetInputInt("\n\nEnter map number (e.g. 1...): ");
				
				if (mapNum != null)
				{
					mapIndex = mapNum - 1;
					inputIsValid = mapIndex < maps.size() && mapIndex >= 0;
				}
			}
		}
		
		// Destroying un chosen maps
		for (int i = maps.size() - 1; i >= 0; i--)
		{
			if (i != mapIndex)
			{
				Globals.ObjectController.DestroyMap(maps.get(i));
			}
		}
		
		return maps.get(mapIndex);
	}
}
