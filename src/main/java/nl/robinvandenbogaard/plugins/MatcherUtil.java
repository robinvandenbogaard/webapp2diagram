package nl.robinvandenbogaard.plugins;

import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Created by robin on 8/13/2017.
 */
public class MatcherUtil {
	public static boolean matches(List<String> wildcardStatements, String name) {
		for (String statement : wildcardStatements) {
			if (FilenameUtils.wildcardMatch(name, statement)) {
				return true;
			}
		}
		return wildcardStatements.size() == 0;

	}
}
