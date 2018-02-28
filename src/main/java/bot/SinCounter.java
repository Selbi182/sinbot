package bot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;

public class SinCounter {

	/**
	 * Local variable to store the sins is a BigDecimal to avoid floating point
	 * arithmetic problems with doubles and such
	 */
	private BigDecimal sins;

	private File counterFile;

	/**
	 * Filename of the local file to store the counter
	 */
	private final static String COUNTER_FILE = "counter.txt";

	/**
	 * Create a new sin counter and set it to 0 sins, or check if a file already
	 * exists
	 */
	public SinCounter() {
		try {
			counterFile = new File("." + File.separator + COUNTER_FILE);
			if (counterFile.exists() && counterFile.isFile() && counterFile.canRead()) {
				String read = Files.readAllLines(counterFile.toPath()).get(0);
				sins = new BigDecimal(read);
			} else {
				sins = new BigDecimal(0);
				rewriteCounterFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Increase number of sins by 1
	 */
	public void increaseSins() {
		increaseSins(BigDecimal.ONE);
	}

	/**
	 * Increase number of sins by the given number (can also be negative)
	 * 
	 * @param bySins
	 *            number of sins
	 */
	public void increaseSins(BigDecimal bySins) {
		sins = sins.add(bySins);
		rewriteCounterFile();
	}
	
	/**
	 * Rewrite the counter file.
	 */
	private void rewriteCounterFile() {
		try {
			PrintWriter writer = new PrintWriter(counterFile);
			writer.print(sins.toString());
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();	
		}
	}
	

	/**
	 * Return a properly formatted string of the current sins with all trailing
	 * zeroes omitted
	 * 
	 * @return
	 */
	public String getPrettyString() {
		return sins.stripTrailingZeros().toPlainString();
	}
}
