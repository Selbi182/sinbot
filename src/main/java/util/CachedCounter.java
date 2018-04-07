package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import main.SinBot;

public class CachedCounter {

	private BigDecimal counter;
	private File file;
	private String trigger;
	private String alias;

	// To read
	public CachedCounter(String trigger) throws IOException {
		this.trigger = trigger.toLowerCase().replace(".txt", "");
		this.file = new File(SinBot.COUNTER_FOLDER, this.trigger + ".txt");
		List<String> read = Files.readAllLines(file.toPath());
		this.alias = read.get(0).substring(0, 1).toUpperCase() + read.get(0).substring(1);
		this.counter = new BigDecimal(read.get(1));
	}
	
	// To create
	public CachedCounter(String trigger, String alias) throws IOException {
		this.trigger = trigger.toLowerCase();
		this.file = new File(SinBot.COUNTER_FOLDER, this.trigger + ".txt");
		this.alias = alias.substring(0, 1).toUpperCase() + alias.substring(1);
		this.counter = new BigDecimal(0);
		rewriteCounterFile();
	}

	public void increaseCounter() {
		increaseCounter(BigDecimal.ONE);
	}

	public void increaseCounter(BigDecimal byCount) {
		counter = counter.add(byCount);
		rewriteCounterFile();
	}

	private void rewriteCounterFile() {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			writer.write(alias);
			writer.write('\n');
			writer.write(counter.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPrettyString() {
		return counter.stripTrailingZeros().toPlainString();
	}

	public String getTrigger() {
		return trigger;
	}

	public String getAlias() {
		return alias;
	}
	
	@Override
	public String toString() {
		return String.format("!%s (%s): %s", trigger, alias, counter.toString());
	}
}
