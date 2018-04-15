package sinbot.util;

import java.util.Arrays;
import java.util.LinkedList;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Digestor {

	private @Getter String trimmed;
	private LinkedList<String> wordList;
	private LinkedList<String> wordListOriginal;

	@SuppressWarnings("unchecked")
	public Digestor(String rawString) {
		trimmed = rawString.trim().toLowerCase().replaceAll("[ ]+", " ");
		wordList = new LinkedList<String>(Arrays.asList(trimmed.split(" ")));
		wordListOriginal = (LinkedList<String>) wordList.clone();
	}

	public String shift() {
		return wordList.pollFirst();
	}
	
	public boolean isEmpty() {
		return wordList.isEmpty();
	}

	public String peekAll() {
		return peek(wordList.size());
	}

	public String peek() {
		return peek(1);
	}

	public String peek(int length) {
		int realLength = length > wordList.size() ? wordList.size() : length;
		return String.join(" ", wordList.subList(0, realLength));
	}
	
	public Digestor reset() {
		wordList = wordListOriginal;
		return this;
	}

}