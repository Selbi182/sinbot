package sinbot.main;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import lombok.NonNull;
import lombok.extern.java.Log;
import sinbot.util.BotUtil;
import sinbot.util.CachedCounter;
import sinbot.util.Digestor;
import sinbot.util.SpamProtector;

@Log
public class MessageHandler {
	private Map<String, CachedCounter> counters;

	public MessageHandler() {
		BotUtil.COUNTER_FOLDER.mkdirs();

		counters = new HashMap<String, CachedCounter>();
		File[] counterFiles = BotUtil.COUNTER_FOLDER.listFiles();
		for (File f : counterFiles) {
			if (f.isFile() && f.canRead()) {
				CachedCounter loadedCounter = new CachedCounter(f.getName());
				counters.put(loadedCounter.getTrigger(), loadedCounter);
			}
		}
		log.info(counterFiles.length + " existing counters loaded!");
	}

	public void processMessage(@NonNull Message message) {
		if (SpamProtector.checkAuthorOkay(message.getAuthor())) {
			String content = message.getContent();
			if (content.startsWith(BotUtil.PREFIX)) {
				log.info("New potential message received: " + content);
				
				EmbedBuilder replyEmbed = new EmbedBuilder();
				Digestor messageDigestor = new Digestor(content.substring(BotUtil.PREFIX.length()));
				String firstWord = messageDigestor.shift();
				switch (firstWord) {
					case "new":
						newCounter(replyEmbed, messageDigestor);
						break;
					case "counters":
						listCounters(replyEmbed);
						break;
					default:
						CachedCounter calledCounter = counters.get(firstWord);
						if (calledCounter == null) {
							return;
						}
						attemptProcessCounterIncrease(replyEmbed, messageDigestor, calledCounter);
				}
				
				message.reply(null, replyEmbed);
			}
		}
	}

	private void newCounter(EmbedBuilder replyEmbed, @NonNull Digestor digestor) {
		String regex = "[a-zA-Z0-9_]+ .+";
		if (digestor.peek(2).matches(regex)) {
			String trigger = digestor.shift();
			String aliasRaw = digestor.shift();
			String alias = aliasRaw.substring(0, 1).toUpperCase() + aliasRaw.substring(1);
			CachedCounter newCounter = new CachedCounter(trigger, alias);
			counters.put(trigger, newCounter);
			replyEmbed.setTitle("New counter created:");		
			replyEmbed.setDescription(String.format("`!%s`\n%s", newCounter.getTrigger(), newCounter.getAliasPure()));
			replyEmbed.setColor(Color.GREEN);
		} else {
			replyEmbed.setTitle("ERROR");		
			replyEmbed.setDescription("Usage:\n`!new TRIGGER ALIAS`\n(TRIGGER must be one ASCII word, ALIAS can be unicode)");
			replyEmbed.setColor(Color.RED);
		}
	}

	private void listCounters(EmbedBuilder replyEmbed) {
		replyEmbed.setTitle("List of all current counters:");
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, CachedCounter> entry : counters.entrySet()) {
			CachedCounter c = entry.getValue();
			sb.append(String.format("`!%s` - _%s_ - %s\n\n", c.getTrigger(), c.getAliasPure(), c.getCount()));
		}
		sb.setLength(sb.length() - 1);
		replyEmbed.setDescription(sb.toString());
		replyEmbed.setColor(Color.CYAN);
	}

	private void attemptProcessCounterIncrease(EmbedBuilder replyEmbed, @NonNull Digestor digestor, @NonNull CachedCounter cachedCounter) {
		if (digestor.isEmpty()) {
			cachedCounter.increaseCounter();
		} else if (digestor.peek().equals("count")) {
			// Nothing
		} else {
			String byCount = digestor.shift();
			String decimalRegex = "-?([0-9]*[.])?[0-9]+"; // e.g. 42 or 12.345 or -.09
			if (!byCount.matches(decimalRegex)) {
				replyEmbed.setTitle("ERROR");
				replyEmbed.setDescription(String.format("Usage:\n`%1$s%2$s`\n`%1$s%2$s NUMBER`\n`%1$s%2$s count`", BotUtil.PREFIX, digestor.reset().peek()));
				replyEmbed.setColor(Color.RED);
				return;
			}
			cachedCounter.increaseCounter(byCount);
		}
		replyEmbed.setTitle(cachedCounter.getAlias());
		replyEmbed.setDescription(cachedCounter.getCount());
		replyEmbed.setColor(Color.MAGENTA);
	}
}
