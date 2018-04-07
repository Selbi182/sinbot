package main;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class SinBot {
	private final static String TOKEN = "MzUxMzczOTEzMDM0NDU3MDg4.DIRrSQ.eli7VNBJT7BwPi1zFocyrBJit7w";
	public final static File COUNTER_FOLDER = new File("." + File.separator + "counters");

	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);
		
		DiscordAPI api = Javacord.getApi(TOKEN, true);
		api.connect(new FutureCallback<DiscordAPI>() {
			public void onSuccess(DiscordAPI api) {
				try {
					api.registerListener(new MessageCreateListener() {
						MessageHandler messageHandler = new MessageHandler();

						public void onMessageCreate(DiscordAPI api, Message message) {
							try {
								messageHandler.processMessage(message);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});
	}
}
