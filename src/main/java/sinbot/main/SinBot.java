package sinbot.main;

import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import sinbot.util.BotUtil;

@Log
public class SinBot {
	public static void main(String[] args) {
		// Reduce proprietary logger of the API to warnings only
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);

		// Give starting message
		log.info("Connecting SinBot...");
		
		// Discord API message receiver
		DiscordAPI api = Javacord.getApi(BotUtil.TOKEN, true);
		api.connect(new FutureCallback<DiscordAPI>() {
			@SneakyThrows
			public void onSuccess(DiscordAPI api) {
				log.info("Connection successful!");
				api.registerListener(new MessageCreateListener() {
					MessageHandler messageHandler = new MessageHandler();

					@SneakyThrows
					public void onMessageCreate(DiscordAPI api, Message message) {
						messageHandler.processMessage(message);
					}
				});
			}

			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});
	}
}
