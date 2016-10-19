package com.chalcodes.automata.test;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.TransitionLabeler;
import com.chalcodes.automata.regex.Regex;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class RegexTest {
	private static final TransitionLabeler<Character> gLabeler = new TransitionLabeler<Character>() {
		@Override
		public String getLabel(@Nonnull final Character input) {
			return input.toString();
		}
	};

	@Test
	public void parse() throws Exception {
		final Automaton<Character, String> a = Regex.parse("(ABC");
		a.determinize();
		System.out.println(a.toGraphViz("test", gLabeler));
	}
}