package sinbot.main;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.user.UserStatus;

import sinbot.util.BotUtil;

public class SinBot {
	public static void main(String[] args) {
		try {
			// Give starting message
			// (For future references: This bot is so small that I honestly don't care about proper logging)
			System.out.println("Connecting SinBot...");
			
			// Discord API login
			DiscordApi api = new DiscordApiBuilder().setToken(BotUtil.TOKEN).login().join();
			api.updateStatus(UserStatus.INVISIBLE);
			
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
					messageHandler.processMessage(event);					
				}
			});			
		} catch (Exception e) {
			System.out.println("Failed to start bot! Terminating...");
			e.printStackTrace();
		}
	}
}
