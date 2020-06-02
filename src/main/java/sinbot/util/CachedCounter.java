package sinbot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

public class CachedCounter implements Comparable<CachedCounter>{

	private File file;
	private BigDecimal counter;
	private String trigger;
	private String aliasPure;
	private String alias;

	// To read when booting
	public CachedCounter(String trigger) throws IOException {
		this.trigger = trigger.toLowerCase().replace(".txt", "");
		this.file = new File(BotUtil.COUNTER_FOLDER, this.trigger + ".txt");
		List<String> read = Files.readAllLines(file.toPath());
		this.aliasPure = read.get(0).substring(0, 1).toUpperCase() + read.get(0).substring(1);
		this.alias = (Pattern.matches(".+\\W", aliasPure) ? aliasPure : aliasPure + ":");
		this.counter = new BigDecimal(read.get(1));
	}

	// To create at runtime
	public CachedCounter(String trigger, String alias) {
		this.trigger = trigger.toLowerCase();
		this.file = new File(BotUtil.COUNTER_FOLDER, this.trigger + ".txt");
		this.aliasPure = alias.substring(0, 1).toUpperCase() + alias.substring(1);
		this.alias = (Pattern.matches(".+\\W", aliasPure) ? aliasPure : aliasPure + ":");
		this.counter = new BigDecimal(0);
		rewriteCounterFile();
	}

	public void increaseCounter() {
		increaseCounter(BigDecimal.ONE);
	}

	public void increaseCounter(String byCount) {
		increaseCounter(new BigDecimal(byCount));
	}

	public void increaseCounter(BigDecimal byCount) {
		counter = counter.add(byCount);
		rewriteCounterFile();
	}


	private void rewriteCounterFile() {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			writer.write(aliasPure);
			writer.write('\n');
			writer.write(counter.toString());			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	public void delete() {
		file.delete();
	}
	
	//////////////////

	public String getTrigger() {
		return trigger;
	}

	public String getAliasPure() {
		return aliasPure;
	}

	public String getAlias() {
		return alias;
	}

	public String getCount() {
		return counter.stripTrailingZeros().toPlainString();
	}
	
	public BigDecimal getCountRaw() {
		return counter;
	}
	
	public void setCountRaw(BigDecimal count) {
		this.counter = count;
	}

	@Override
	public int compareTo(CachedCounter o) {
		return this.getTrigger().compareTo(o.getTrigger());
	}
}
