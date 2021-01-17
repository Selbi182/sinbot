package sinbot.main;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import sinbot.util.BotUtil;
import sinbot.util.CachedCounter;
import sinbot.util.Digestor;
import sinbot.util.Gachi;
import sinbot.util.SpamProtector;

public class MessageHandler {
	private Map<String, CachedCounter> counters;

	public MessageHandler() throws IOException {
		BotUtil.COUNTER_FOLDER.mkdirs();

		counters = new HashMap<String, CachedCounter>();
		File[] counterFiles = BotUtil.COUNTER_FOLDER.listFiles();
		for (File f : counterFiles) {
			if (f.isFile() && f.canRead()) {
				CachedCounter loadedCounter = new CachedCounter(f.getName());
				counters.put(loadedCounter.getTrigger(), loadedCounter);
			}
		}
		System.out.println(counterFiles.length + " existing counters loaded!");
	}

	public boolean processMessage(MessageCreateEvent message) {
		if (SpamProtector.checkAuthorOkay(message.getMessageAuthor())) {
			String content = message.getMessageContent();
			if (content.startsWith(BotUtil.PREFIX)) {
				System.out.println("New potential message received: " + content);
				
				EmbedBuilder replyEmbed = new EmbedBuilder();
				Digestor messageDigestor = new Digestor(content.substring(BotUtil.PREFIX.length()));
				String firstWord = messageDigestor.shift();
				switch (firstWord) {
					case "new":
					case "create":
						newCounter(replyEmbed, messageDigestor);
						break;
					case "delete":
					case "remove":
						deleteCounter(replyEmbed, messageDigestor);
						break;
					case "rename":
					case "description":
						renameCounter(replyEmbed, messageDigestor);
						break;
					case "counters":
					case "list":
						listCounters(replyEmbed);
						break;
					case "billy":
					case "gachi":
					case "listbilly":
					case "listgachi":
						listGachiQuotes(replyEmbed);
						break;
					case "newgachi":
					case "newbilly":
					case "addbilly":
					case "addgachi":
					case "addquote":
						newGachiQuote(replyEmbed, messageDigestor);
						break;
					default:
						CachedCounter calledCounter = counters.get(firstWord);
						if (calledCounter == null) {
							return false;
						}
						attemptProcessCounterIncrease(replyEmbed, messageDigestor, calledCounter);
				}
				
				message.getChannel().sendMessage(replyEmbed);
			} else if (content.toLowerCase().matches(".*\\bbilly\\b.*")) {
				System.out.println("New Billy message received: " + content);
				sendBillyMessage(message);
			}
			return true;
		}
		return false;
	}

	private void sendBillyMessage(MessageCreateEvent message) {
		EmbedBuilder gachiEmbed = new EmbedBuilder();
		gachiEmbed.setColor(Color.MAGENTA);
		gachiEmbed.setTitle(Gachi.getRandomQuote());
		message.getChannel().sendMessage(gachiEmbed);
	}
	

	private void newGachiQuote(EmbedBuilder replyEmbed, Digestor digestor) {
		String quote = digestor.peekAll();
		if (quote != null && !quote.isBlank()) {
			Gachi.addQuote(quote);
			replyEmbed.setTitle("Quote Added");		
			replyEmbed.setDescription(Gachi.billyMessage(quote));
			replyEmbed.setColor(Color.GREEN);
		} else {
			replyEmbed.setTitle("Usage");		
			replyEmbed.setDescription("`!newgachi` QUOTE");
			replyEmbed.setColor(Color.GRAY);			
		}
	}
	
	private void listGachiQuotes(EmbedBuilder replyEmbed) {
		List<String> gachiQuotes = Gachi.getQuotes();
		if (!gachiQuotes.isEmpty()) {
			Collections.sort(gachiQuotes);
			StringBuilder sb = new StringBuilder();
			for (String s : gachiQuotes) {
				sb.append(String.format("\"%s\"\n", s));
			}
			sb.setLength(sb.length() - 1);
			
			replyEmbed.setTitle("Gachi Quotes (" + gachiQuotes.size() + ")");
			replyEmbed.setDescription(sb.toString());
			replyEmbed.setColor(Color.CYAN);			
		} else {
			replyEmbed.setTitle("ERROR");		
			replyEmbed.setDescription("No Gachi quotes found!");
			replyEmbed.setColor(Color.RED);
		}
	}

	private void newCounter(EmbedBuilder replyEmbed, Digestor digestor) {
		String regex = "[a-zA-Z0-9_]+ .+";
		if (digestor.peek(2).matches(regex)) {
			String trigger = digestor.shift().toLowerCase();
			if (!counters.containsKey(trigger)) {
				String aliasRaw = digestor.peekAll();
				String alias = aliasRaw.substring(0, 1).toUpperCase() + aliasRaw.substring(1);
				CachedCounter newCounter = new CachedCounter(trigger, alias);
				counters.put(trigger, newCounter);
				replyEmbed.setTitle("Counter Created");		
				replyEmbed.setDescription(String.format("`!%s`\n%s", newCounter.getTrigger(), newCounter.getAliasPure()));
				replyEmbed.setColor(Color.GREEN);
			} else {
				replyEmbed.setTitle("ERROR");		
				replyEmbed.setDescription(String.format("Trigger `!%s` already exists!", trigger));
				replyEmbed.setColor(Color.RED);
			}
		} else {
			replyEmbed.setTitle("Usage");		
			replyEmbed.setDescription("`!new TRIGGER ALIAS`\n(TRIGGER must be one ASCII word, ALIAS can be unicode)");
			replyEmbed.setColor(Color.GRAY);			
		}
	}

	private void deleteCounter(EmbedBuilder replyEmbed, Digestor digestor) {
		String regex = "[a-zA-Z0-9_]+";
		String counterName = digestor.peek();
		if (counterName.matches(regex)) {
			CachedCounter counterToDelete = counters.get(counterName);
			if (counterToDelete != null) {
				replyEmbed.setTitle("Counter Deleted");		
				replyEmbed.setDescription(String.format("`!%s`\n%s\n%s", counterToDelete.getTrigger(), counterToDelete.getAliasPure(), counterToDelete.getCount()));
				replyEmbed.setColor(Color.BLACK);
				counters.remove(counterName);
				counterToDelete.delete();
			} else {
				replyEmbed.setTitle("ERROR");		
				replyEmbed.setDescription(String.format("Counter `!%s` not found!", counterName));
				replyEmbed.setColor(Color.RED);
			}
		} else {
			replyEmbed.setTitle("Usage");		
			replyEmbed.setDescription("`!delete TRIGGER`");
			replyEmbed.setColor(Color.GRAY);
		}
	}


	private void renameCounter(EmbedBuilder replyEmbed, Digestor digestor) {
		String regex = "[a-zA-Z0-9_]+ .+";
		if (digestor.peek(2).matches(regex)) {
			String trigger = digestor.shift().toLowerCase();
			if (counters.containsKey(trigger)) {
				CachedCounter counter = counters.get(trigger);
				
				// Preserve count
				BigDecimal preservedCount = counter.getCountRaw();
				
				// Delete old counter
				counters.remove(trigger);
				counter.delete();
					
				// Create new counter with same count and alias but new description
				String aliasRaw = digestor.peekAll();
				String alias = aliasRaw.substring(0, 1).toUpperCase() + aliasRaw.substring(1);
				CachedCounter newCounter = new CachedCounter(trigger, alias);
				newCounter.setCountRaw(preservedCount);
				counters.put(trigger, newCounter);
				
				replyEmbed.setTitle("Counter Description Updated");		
				replyEmbed.setDescription(String.format("`!%s`\n_%s_\nCount: %s", newCounter.getTrigger(), newCounter.getAliasPure(), newCounter.getCount()));
				replyEmbed.setColor(Color.GREEN);			
			} else {
				replyEmbed.setTitle("ERROR");		
				replyEmbed.setDescription(String.format("Counter `!%s` not found!", trigger));
				replyEmbed.setColor(Color.RED);
			}
		} else {
			replyEmbed.setTitle("Usage");		
			replyEmbed.setDescription("`!rename TRIGGER NEW_DESCRIPTION`");
			replyEmbed.setColor(Color.GRAY);
		}
	}
		
	private void listCounters(EmbedBuilder replyEmbed) {
		List<CachedCounter> cachedCounters = new ArrayList<>(counters.values());
		if (!cachedCounters.isEmpty()) {
			Collections.sort(cachedCounters);
			StringBuilder sb = new StringBuilder();
			for (CachedCounter c : cachedCounters) {
				sb.append(String.format("`!%s` - _%s_ - %s\n", c.getTrigger(), c.getAliasPure(), c.getCount()));
			}
			sb.setLength(sb.length() - 1);
			
			replyEmbed.setTitle("Counters (" + counters.size() + ")");
			replyEmbed.setDescription(sb.toString());
			replyEmbed.setColor(Color.CYAN);			
		} else {
			replyEmbed.setTitle("ERROR");		
			replyEmbed.setDescription("No counters found!");
			replyEmbed.setColor(Color.RED);
		}
	}

	private void attemptProcessCounterIncrease(EmbedBuilder replyEmbed, Digestor digestor, CachedCounter cachedCounter) {
		if (digestor.isEmpty()) {
			cachedCounter.increaseCounter();
		} else if (digestor.peek().equals("count")) {
			// Nothing
		} else {
			String byCount = digestor.shift();
			String decimalRegex = "-?([0-9]*[.])?[0-9]+"; // e.g. 42 or 12.345 or -.09
			if (!byCount.matches(decimalRegex)) {
				replyEmbed.setTitle("Usage");
				replyEmbed.setDescription(String.format("`%1$s%2$s`\n`%1$s%2$s NUMBER`\n`%1$s%2$s count`", BotUtil.PREFIX, digestor.reset().peek()));
				replyEmbed.setColor(Color.GRAY);
				return;
			}
			cachedCounter.increaseCounter(byCount);
		}
		replyEmbed.setTitle(cachedCounter.getAlias());
		replyEmbed.setDescription(cachedCounter.getCount());
		replyEmbed.setColor(Color.MAGENTA);
	}
}
