package sinbot.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;

import com.google.common.io.Files;

public class SinBot {
	public static void main(String[] args) {
		try {
			// Give starting message
			// (For future references: This bot is so small that I honestly don't care about proper logging)
			System.out.println("Connecting SinBot...");
			
			// Discord API login
			String token = readToken();
			DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
			api.updateStatus(UserStatus.ONLINE);
			ramRanchReallyRocks(api);
			
			MessageHandler messageHandler = new MessageHandler();
			
			// Setup message receiver
			api.addMessageCreateListener(event -> {
				String statusChangeRequest = event.getMessage().getContent().toLowerCase();
				if (statusChangeRequest.startsWith("!!!setstatus ")) {
					String newStatus = statusChangeRequest.split(" ")[1].trim();
					UserStatus newUserStatus = UserStatus.fromString(newStatus);
					if (!newUserStatus.equals(UserStatus.OFFLINE)) {
						api.updateStatus(newUserStatus);
						System.out.println("Changed bot status to: " + newUserStatus.toString());						
					}
				} else {
					if (messageHandler.processMessage(event)) {
						ramRanchReallyRocks(api);
					}
				}
			});			
		} catch (Exception e) {
			System.out.println("Failed to start bot! Terminating...");
			e.printStackTrace();
		}
	}

	private static void ramRanchReallyRocks(DiscordApi api) {
		api.updateActivity(ActivityType.LISTENING, "Ram Ranch " + (new Random().nextInt(371)));
	}
	
	private static String readToken() throws IOException {
		File tokenFile = new File("./token.txt");
		if (tokenFile.canRead()) {
			return Files.asCharSource(tokenFile, Charset.defaultCharset()).readFirstLine();
		}
		throw new IOException("Can't read token file!");
	}
}
