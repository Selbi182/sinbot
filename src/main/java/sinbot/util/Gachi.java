package sinbot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Gachi {

	private static final String QUOTE_FILE = "./gachi.txt";

	public static List<String> getQuotes() {
		try {
			File f = new File(QUOTE_FILE);
			if (f.canRead()) {
				return Files.readAllLines(f.toPath());
			} else {
				System.out.println("Cannot read quote file");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public static String getRandomQuote() {
		List<String> quotes = getQuotes();
	    int rnd = new Random().nextInt(quotes.size());
	    String randomQuote = quotes.get(rnd);
	    return billyMessage(randomQuote);
	}

	public static void addQuote(String quote) {
		try {
			File f = new File(QUOTE_FILE);
			if (f.canWrite()) {
				Files.writeString(f.toPath(), quote + "\n", StandardOpenOption.APPEND);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String billyMessage(String s) {
		return String.join(" ", BotUtil.GACHI_EMOJI, s, BotUtil.GACHI_EMOJI);
	}

}
