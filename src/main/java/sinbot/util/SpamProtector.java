package sinbot.util;

import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.message.MessageAuthor;

public final class SpamProtector {

	private final static Map<String, Long> cooldownPerUser = new HashMap<String, Long>();
	private final static int COOLDOWN = 1000;

	public static boolean checkAuthorOkay(MessageAuthor messageAuthor) {
		// Don't listen to bot-written messages
		if (messageAuthor.isBotUser()) {
			return false;
		}
		
		// Prevent spam
		String id = String.valueOf(messageAuthor.getId());
		if (cooldownPerUser.containsKey(id)) {
			if ((System.currentTimeMillis() - cooldownPerUser.get(id) - COOLDOWN) < 0) {
				return false;
			}
		}
		cooldownPerUser.put(id, System.currentTimeMillis());
		return true;
	}
}
