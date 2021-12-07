import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BotBase extends Client
{
	/**
	 * Pattern for differentiating display names from messages.
	 */
	private final Pattern DisplayNamePattern = Pattern.compile("^<.*>\\s*");
	
	/**
	 * Pattern for recognising if the message was intended for this bot.
	 */
	private final Pattern PrefixPattern;
	
	/**
	 * A collection of ANSI escape codes we excpect to receive from the server.
	 */
	private String[] ANSIEscapeCodes = { "\u001B[0m", "\u001B[31m", "\u001B[33m", "\u001B[32m" };
	
	/**
	 * Extends the Client constructor, adding the prefix this bot expects before messages.
	 *
	 * @param remoteAddress The address of the server.
	 * @param remotePort    The port to connect to. (Server needs to be actively listening for connections on this port)
	 * @param prefixPattern The prefix which should be used when communicating with this bot.
	 */
	public BotBase(InetAddress remoteAddress, int remotePort, Pattern prefixPattern)
	{
		super(remoteAddress, remotePort);
		PrefixPattern = prefixPattern;
	}
	
	/**
	 * Removes the display name attached to the front of a given string, if one exists.
	 *
	 * @param contents The string to remove the name from.
	 * @return A copy of contents with the display name removed.
	 */
	public String RemoveDisplayName(String contents)
	{
		Matcher nameMatcher = DisplayNamePattern.matcher(contents);
		
		// Only removing display name if it appears at the beginning of the string
		if (nameMatcher.find())
		{
			contents = nameMatcher.replaceFirst("");
		}
		
		return contents;
	}
	
	/**
	 * Gets the first ocuring display name from a given string.
	 *
	 * @param contents String to check.
	 * @return The first ocuring display name.
	 */
	public String GetDisplayName(String contents)
	{
		Matcher nameMatcher = DisplayNamePattern.matcher(contents);
		if (nameMatcher.find())
		{
			String match = nameMatcher.group();
			
			// Removing surrounding characters
			return match.replaceAll("[<>\\s]", "");
		}
		
		return null;
	}
	
	/**
	 * Removes the prefix from a given string, if it is present.
	 *
	 * @param contents String to remove the prefix from.
	 * @return A copy of contents without the prefix at the start.
	 */
	public String RemoveChatPrefix(String contents)
	{
		Matcher prefixMatcher = PrefixPattern.matcher(contents);
		
		// Only removing prefix if it appears at the beginning of the string
		if (prefixMatcher.find())
		{
			contents = prefixMatcher.replaceFirst("");
		}
		
		return contents;
	}
	
	/**
	 * Checks if a given string matches the prefix pattern.
	 *
	 * @param str The string to check.
	 * @return Whether the given string is the prefix.
	 */
	public boolean IsPrefix(String str)
	{
		Matcher prefixMatcher = PrefixPattern.matcher(str);
		return prefixMatcher.matches();
	}
	
	/**
	 * Removes ANSI escape codes from a given message.
	 *
	 * @param message The message to remove the codes from.
	 * @return A copy of message without any escape codes.
	 */
	public String RemoveANSIEscapeCodes(String message)
	{
		for (int i = 0; i < ANSIEscapeCodes.length; i++)
		{
			message = message.replace(ANSIEscapeCodes[i], "");
		}
		
		return message;
	}
}
