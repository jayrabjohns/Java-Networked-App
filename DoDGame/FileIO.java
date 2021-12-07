package DoDGame;

import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * Deals with all IO related to files.
 */
public class FileIO
{
	/**
	 * Tries to read contents from a given text file.
	 * @param path Path of file.
	 * @param fileContents List to copy file contents to.
	 * @return Whether it successfully read from the file.
	 */
	public boolean TryGetTxtFileContents(String path, List<String> fileContents)
	{
		if (fileContents == null || !Objects.equals(GetFileExtension(path),  ".txt") || Objects.equals(GetFileName(path), "README"))
		{
			return false;
		}
		
		boolean success = false;
		try
		{
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			// Reading each line of the file as a string and adding it to the list
			String line = "";
			while ((line = bufferedReader.readLine()) != null)
			{
				fileContents.add(line);
			}
			
			bufferedReader.close();
			success = true;
		}
		catch (FileNotFoundException e)
		{
			Globals.UserIO.WriteToUser("\n\n########################\nCannot find file at %s\n########################\n\n", path);
		}
		catch (IOException e)
		{
			Globals.UserIO.WriteToUser("\n\n########################\nCannot access file at %s\n########################\n\n", path);
		}
		
		return success;
	}
	
	/**
	 * Gets paths to all files in given directory.
	 * @param path Path of the containing directory.
	 * @return Array of all file paths in the directory. Returns null if path is null.
	 */
	public String[] GetAllFilesInDir(String path)
	{
		if (path == null)
		{
			return null;
		}
		
		// Getting each file in the directory
		File mapsDir = new File(path);
		File[] files = mapsDir.listFiles();
		String[] paths = files != null ? new String[files.length] : new String[0];
		
		// Getting absolute path to each file in directory
		for (int i = 0; i < paths.length; i++)
		{
			paths[i] = files[i].getAbsolutePath();
		}
		
		return paths;
	}
	
	/**
	 * Gets the file extension of a file, e.g. '.txt'.
	 * @param path Path of the file.
	 * @return File extension of the file.
	 */
	public String GetFileExtension(String path)
	{
		int extIndex = path != null ? path.lastIndexOf(".") : -1;
		if (extIndex != -1)
		{
			return  path.substring(extIndex);
		}
		
		return "";
	}
	
	/**
	 * Gets the name of a file at a specified location.
	 * @param path Path of file.
	 * @return The name of the file.
	 */
	public String GetFileName(String path)
	{
		return new File(path).getName();
	}
}
