import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBotClient extends BotBase
{
	private final Random RNG = new Random();
	
	private final String[] GreetingTokens = new String[] { "hi", "hey", "hello" };
	private final String[] StatementResponses = new String[] { "ok", "no way!", "uh huh", "mhm", "sure" };
	
	private final HashMap<String, String> WhoResponses = new HashMap<>()
	{
		{
			put("you", "I'm a chat bot. Fairly obvious");
			put(" i", "I'm going to take a wild guess and say...Chris?");
			put(null, "I'm not answering that");
		}
	};
	
	private final HashMap<String, String> WhatResponses = new HashMap<>()
	{
		{
			put("name", "java.exe  |  PID 16079");
			put(" i", "someone testing out this sick chatbot");
			put("time", "<-- It's right there");
			put("you", "I'm a chat bot. Fairly obvious.");
			put("up", "the sky");
			put("weather", "can't complain, CPU's currently sitting at a comfortable 38.2°C");
			put("balamory", "wouldn't *you* like to know?");
			put("life", "42");
			put(null, "...I don't know");
		}
	};
	
	private final HashMap<String, String> WhenResponses = new HashMap<>()
	{
		{
			put("you", "now");
			put(null, "when ever you want it to be");
		}
	};
	
	private final HashMap<String, String> WhereResponses = new HashMap<>()
	{
		{
			put("you", "umm... one of the linux.bath servers by the looks of it");
			put(" i", "all I can say is you're not where you're not");
			put(null, "that's classified, sorry");
		}
	};
	
	private final HashMap<String, String> WhyResponses = new HashMap<>()
	{
		{
			put("here", "because everyone knows to come here for a good time");
			put(null, "...do I really need to explain it to you? Don't bother answering, I'll just ignore it and move on");
		}
	};
	
	private final HashMap<String, String> HowResponses = new HashMap<>()
	{
		{
			put("weather", "can't complain, CPU's currently sitting at a comfortable 38.2°C");
			put("you", "I'm doing well thanks!");
			put("going", "I'm doing well thanks!");
			put(null, "jesus christ how would I know that?");
		}
	};
	
	private final HashMap<String, String> DoResponses = new HashMap<>()
	{
		{
			put(null, "pass");
		}
	};
	
	private final HashMap<String, String> AreResponses = new HashMap<>()
	{
		{
			put(null, "you tell me");
		}
	};
	
	private final HashMap<String, HashMap<String, String>> ResponsesByQuestionToken = new HashMap<>()
	{
		{
			put("who", WhoResponses);
			put("what", WhatResponses);
			put("when", WhenResponses);
			put("where", WhereResponses);
			put("why", WhyResponses);
			put("how", HowResponses);
			put("do", DoResponses);
			put("are", AreResponses);
		}
	};
	
	/**
	 * Constructs a new ChatBotClient.
	 *
	 * @param remoteAddress The address of the server.
	 * @param remotePort    The port to connect to. (Server needs to be actively listening for connections on this port)
	 */
	public ChatBotClient(InetAddress remoteAddress, int remotePort)
	{
		super(remoteAddress, remotePort, Pattern.compile("^!cb\\s*"));
		
		SetFirstResponse("ChatBot");
	}
	
	/**
	 * Logic for when we receive a message.
	 *
	 * @param message The message received.
	 */
	@Override
	public void OnMessageReceived(String message)
	{
		super.OnMessageReceived(message);
		
		message = RemoveDisplayName(message);
		String[] messageArray = message.split("\\s+");
		if (messageArray.length != 0 && IsPrefix(messageArray[0]))
		{
			message = RemoveChatPrefix(message);
			String response = CreateResponse(message);
			SendMessage(response);
		}
	}
	
	/**
	 * Takes in a message and creates a response based on predefined key words.
	 *
	 * @param message Message to create a response to.
	 * @return A response to the given message.
	 */
	private String CreateResponse(String message)
	{
		boolean isGreeting = false;
		boolean isPersonalQuestion = false;
		boolean isGeneralQuestion = false;
		boolean isStatement = false;
		String foundQuestionToken = null;
		
		message = message.toLowerCase();
		
		// Checking for greetings
		for (int i = 0; i < GreetingTokens.length; i++)
		{
			if (message.contains(GreetingTokens[i]))
			{
				isGreeting = true;
				break;
			}
		}
		
		// Checking for questions
		for (String questionToken: ResponsesByQuestionToken.keySet())
		{
			if (message.contains(questionToken))
			{
				foundQuestionToken = questionToken;
				
				// Determining if it's personal or general
				isPersonalQuestion = message.contains("you");
				isGeneralQuestion = !isPersonalQuestion;
				break;
			}
		}
		
		// If none of the above, it's a statement
		isStatement = !isGreeting && !isGeneralQuestion && !isPersonalQuestion;
		
		// Creating a response
		String response = "...";
		if (isGreeting)
		{
			int index = RNG.nextInt(GreetingTokens.length - 1);
			response = GreetingTokens[index];
		}
		else if (isStatement)
		{
			int index = RNG.nextInt(StatementResponses.length - 1);
			response = StatementResponses[index];
		}
		else
		{
			// Dealing with questions
			boolean foundResponse = false;
			HashMap<String, String> responses = ResponsesByQuestionToken.get(foundQuestionToken);
			if (responses != null)
			{
				// Checking for specific key words
				for (String keyWord : responses.keySet())
				{
					if (keyWord != null && message.contains(keyWord))
					{
						response = responses.get(keyWord);
						foundResponse = true;
						break;
					}
				}
				
				if (!foundResponse)
				{
					response = responses.get(null);
				}
			}
		}
		
		return response;
	}
}
