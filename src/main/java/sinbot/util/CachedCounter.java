package sinbot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@ToString
public class CachedCounter {

	private File file;
	private BigDecimal counter;
	@Getter	private String trigger;
	@Getter	private String aliasPure;
	@Getter	private String alias;

	// To read when booting
	@SneakyThrows
	public CachedCounter(String trigger) {
		this.trigger = trigger.toLowerCase().replace(".txt", "");
		this.file = new File(BotUtil.COUNTER_FOLDER, this.trigger + ".txt");
		List<String> read = Files.readAllLines(file.toPath());
		this.aliasPure = read.get(0).substring(0, 1).toUpperCase() + read.get(0).substring(1);
		this.alias = (Pattern.matches(".+\\W", aliasPure) ? aliasPure : aliasPure + ":");
		this.counter = new BigDecimal(read.get(1));
	}

	// To create at runtime
	@SneakyThrows
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

	@SneakyThrows
	private void rewriteCounterFile() {
		@Cleanup OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		writer.write(aliasPure);
		writer.write('\n');
		writer.write(counter.toString());
}

	public String getCount() {
		return counter.stripTrailingZeros().toPlainString();
	}
}
