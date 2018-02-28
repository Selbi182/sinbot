package bot;

import java.math.BigDecimal;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.*;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class SinBot {
	public static void main(String[] args) {
		// Bot User token taken from
		String token = "MzUxMzczOTEzMDM0NDU3MDg4.DIRrSQ.eli7VNBJT7BwPi1zFocyrBJit7w";

		// Connect token to Discord API and tell it that it's a bot user
		DiscordAPI api = Javacord.getApi(token, true);

		// Set up the connection hook with callback and response handler
		api.connect(new FutureCallback<DiscordAPI>() {
			/**
			 * This gets executed when everything worked properly
			 */
			public void onSuccess(DiscordAPI api) {

				// Register a message listener
				api.registerListener(new MessageCreateListener() {

					// Initialize the sin counter
					private SinCounter sc = new SinCounter();

					/**
					 * This gets executed whenever we write a message
					 */
					public void onMessageCreate(DiscordAPI api, Message message) {
						
						// Get the message's content and trim it + remove all extra spaces
						// "  !sin    123  " -> "!sin 123"
						String msg = message.getContent().trim().replaceAll("[ ]+", " ");
						
						// Check if the message starts with !sin
						// Otherwise, ignore it
						if (msg.startsWith("!sin")) {
							
							// Setup the regex check for "!sin NUMBER"
							// This includes a check for floating point numbers
							String regex = "!sin -?([0-9]*[.])?[0-9]+";
							if (msg.matches(regex)) {
								// Case 1: !sin with Number
								// Extract the second part of the message (the number)
								String extract = msg.split(" ")[1];
								
								// Add that number to the number of sins
								BigDecimal newSins = new BigDecimal(extract);
								sc.increaseSins(newSins);
							} else if (msg.equals("!sin")) {
								// Case 2: !sin WITHOUT ANYTHING ELSE
								
								// Just increase sins by 1
								sc.increaseSins();
							} else {
								// Case 3: !sin with random crap that isn't a number
								
								// Return an instruction
								message.reply("Usage: _!sin_ or _!sin NUMBER_");
								
								// Return early to not display the current sin counter
								return;
							}
							
							// Reply with the current sin counter
							message.reply("_Sin Counter: " + sc.getPrettyString() + "_");
						}
					}
				});
			}

			/**
			 * This gets executed when an error occurred
			 */
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});

	}
}
