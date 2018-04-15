package sinbot.util;

import java.util.HashMap;
import java.util.Map;

import de.btobastian.javacord.entities.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class SpamProtector {

	private final static Map<String, Long> cooldownPerUser = new HashMap<String, Long>();
	private final static int COOLDOWN = 1000;

	public static boolean checkAuthorOkay(User author) {
		String id = author.getId();
		if (cooldownPerUser.containsKey(id)) {
			if ((System.currentTimeMillis() - cooldownPerUser.get(id) - COOLDOWN) < 0) {
				return false;
			}
		}
		cooldownPerUser.put(id, System.currentTimeMillis());
		return true;
	}
}
