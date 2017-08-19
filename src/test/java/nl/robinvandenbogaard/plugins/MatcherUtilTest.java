package nl.robinvandenbogaard.plugins;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by robin on 8/13/2017.
 */
public class MatcherUtilTest {
	private static final String JARFILE = "webapp2diagram-maven-plugin-1.0.0-SNAPSHOT";
	private static List<String> statements;

	@Before
	public void setup() {
		statements = new ArrayList<>();
	}

	@Test
	public void matchesWildcardFront() throws Exception {
		statements.add("*"+JARFILE);
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}
	@Test
	public void matchesWildcardBefore() throws Exception {
		statements.add("*gin-1.0.0-SNAPSHOT");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}
	@Test
	public void matchesWildcardAfter() throws Exception {
		statements.add("webapp2diagram-maven-plu*");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}
	@Test
	public void matchesWildcardEnd() throws Exception {
		statements.add(JARFILE+"*");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}

	@Test
	public void matchesWildcardBetween() throws Exception {
		statements.add("webapp2diagram-maven-*-1.0.0-SNAPSHOT");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}

	@Test
	public void matchesWildcardMultiple() throws Exception {
		statements.add("webapp2diagram-maven-*-*-SNAPSHOT");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}

	@Test
	public void noWildcardNoMatch() throws Exception {
		statements.add("xxxx");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(false));
	}

	@Test
	public void noWildcardExactMatch() throws Exception {
		statements.add(JARFILE);
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}

	@Test
	public void noMatch() throws Exception {
		statements.add("xx*xx");
		assertThat(MatcherUtil.matches(statements, JARFILE), is(false));
	}

	@Test
	public void matchIfNoStatements() throws Exception {
		assertThat(MatcherUtil.matches(statements, JARFILE), is(true));
	}

}