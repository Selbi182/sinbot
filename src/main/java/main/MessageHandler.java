package main;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.btobastian.javacord.entities.message.Message;
import util.CachedCounter;

public class MessageHandler {

	private final static String PREFIX = "!";
	private final static int COOLDOWN = 3000;
	private Logger log = Logger.getGlobal();
	private Map<String, CachedCounter> counters;
	private Map<String, Long> cooldownPerUser;
	
	public MessageHandler() throws IOException {
		if (!SinBot.COUNTER_FOLDER.isDirectory()) {
			SinBot.COUNTER_FOLDER.mkdir();
		}

		counters = new HashMap<String, CachedCounter>();
		for (File f : SinBot.COUNTER_FOLDER.listFiles()) {
			if (f.isFile() && f.canRead()) {
				CachedCounter loadedCounter = new CachedCounter(f.getName());
				counters.put(loadedCounter.getTrigger(), loadedCounter);
				log.info("Loaded counter: " + loadedCounter);
			}
		}
		
		cooldownPerUser = new HashMap<String, Long>();
	}

	public void processMessage(Message message) throws IOException {
		String id = message.getAuthor().getId();
		if (cooldownPerUser.containsKey(id)) {
			if ((System.currentTimeMillis() - cooldownPerUser.get(id) - COOLDOWN) < 0) {
				return;
			}
		}
		cooldownPerUser.put(id, System.currentTimeMillis());
		
		String trimmedMsg = message.getContent().trim().replaceAll("[ ]+", " ");
		if (trimmedMsg.startsWith(PREFIX)) {
			log.info("New potential message received: " + trimmedMsg);
			
			for (Map.Entry<String, CachedCounter> entry : counters.entrySet()) {
				String replyMessage = attemptProcess(trimmedMsg, entry.getValue());
				if (replyMessage != null) {
					message.reply(replyMessage);
					return;
				}
			}

			String replyMessageNewCounter = attemptRegisterNewCounter(trimmedMsg);
			if (replyMessageNewCounter != null) {
				message.reply(replyMessageNewCounter);
				return;
			}
		}
	}

	private String attemptProcess(String trimmedMsg, CachedCounter cachedCounter) {
		String prefix = PREFIX + cachedCounter.getTrigger().toLowerCase();
		if (trimmedMsg.startsWith(prefix)) {
			String regex = prefix + " -?([0-9]*[.])?[0-9]+";
			if (trimmedMsg.matches(regex)) {
				String extract = trimmedMsg.split(" ")[1];
				BigDecimal newSins = new BigDecimal(extract);
				cachedCounter.increaseCounter(newSins);
			} else if (trimmedMsg.equals(prefix)) {
				cachedCounter.increaseCounter();
			} else if (trimmedMsg.equals(prefix + " count")) {
				// Nothing
			} else {
				return String.format("Usage: _%1$s_ or _%1$s NUMBER_ or _%1$s count_", prefix);
			}

			return "_" + cachedCounter.getAlias() + ": " + cachedCounter.getPrettyString() + "_";
		}

		return null;
	}

	private String attemptRegisterNewCounter(String trimmedMsg) throws IOException {
		String prefix = PREFIX + "new";
		if (trimmedMsg.startsWith(prefix)) {
			String regex = prefix + " [a-zA-Z0-9_]+ .+";
			if (trimmedMsg.matches(regex)) {
				String trigger = trimmedMsg.split(" ", 3)[1];
				String aliasRaw = trimmedMsg.split(" ", 3)[2];
				String alias = aliasRaw.substring(0, 1).toUpperCase() + aliasRaw.substring(1);
				CachedCounter newCounter = new CachedCounter(trigger, alias);
				counters.put(trigger, newCounter);
				return String.format("New counter created: !%s (%s)", newCounter.getTrigger(), newCounter.getAlias());
			} else {
				return "Usage: !new TRIGGER NAME (trigger must be one latin word, name can be anything)";
			}
		}
		return null;
	}
}
