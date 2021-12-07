# Java Networked Application
This was my submission for the first coursework of 'CM10228 Principles of Programming 2' in my first year at the University of Bath.

If you're wondering about the lack of folder structure, this is how it was asked to be submitted and I haven't gotten around to re-introducing one :) 

We had to create a multithreaded, networked application which consists of:
* A chat client & server - you can connect and talk to others connected to the server.
* A rudementary chat bot - you can connect and talk to a bot on the server
* A game you could play on the server - In one of our previous courseworks we created a console based game called Dungeons of Doom (DoD), this is what you can play.

Documentation is included in `.\JavaDoc\`

The original specifications and mark scheme can be found in `Specification.pdf`

# Bot Implementations
I designed both bots to work similarly to how they work on discord.

You need to prepend your message with a prefix for it to be recognised by the bot.
This allows for users to keep chatting in the background, without affecting the bots.

Each bot will only respond to messages if their respective prefix is used.
E.g. '!cb hello' is how you'd correctly communicate with the chat bot.
Or '!dod look' is how you'd correctly play dod.

## Chat bot
To run the chat bot client, run ChatClient and pass in the parameter -cb

Functionality:
- Takes in a message from the server
- The chat bot will ignore any messages which don't have the prefix '!cb'
- Removes any un-needed information (such as display names)
- Checks the message for predefined key words
- Depending on the keywords found, it is categorised as a greeting, personal question, or general question
- Each category has another set of key words, each corresponding to a predetermined response

For example, the message 'who are you' is received.
The words 'who' and 'you' are found in the message, which are keywords for a personal question, so it's classified as a personal question.
It then uses a lookup table to look for more specific key words. If one is found then it uses that response, if not a more general response for personal questions is given.

- If no keywords are found, it is assumed to be a statement so a response will be randomly chosen from a pool of generic answers.
For example, 'I'm sitting on a chair' is a statement.

- After it forms a response, it sends it back to the server

## DoD Game
To run the dod client, run ChatClient and pass in the parameter -dod

Functionality:
- Takes in a messages from the server
- The dod client will ignore any messages which don't have the prefix '!dod'
- The only exception to this is a message sent by the server
- After a dod client has connected to the server, players can join the DOD game by typing '!dod join'
- The dod client will keep track of who's turn it is, a players commands will only be parsed when it is their go.
- Players can join at any time
- Players can stop playing by prematurely calling the 'quit' command.
- Once every player has had their go, the bot will have their go.
- The game ends when either when there are no players left, or if a player successfully exits with the correct amount of gold.

## DoD Controls
To play DoD you may use these commands:

* JOIN 	 		 - Joins the current game. Starts a game if one isn't already started.
* HELLO 			 - Displays total amount of gold required for the human player to be eligible to win.
* GOLD 			 - Displays the current gold owned.
* MOVE <direction> - Moves a player one square in the given cardinal direction. (N, E, S, W)
* PICKUP			 - Picks up the gold on the player's current location.
* LOOK			 - Shows a 5x5 area of the map around a player.
* QUIT			 - Quits the game if the player is standing at an exit and has enough gold to leave. Loses otherwise.
