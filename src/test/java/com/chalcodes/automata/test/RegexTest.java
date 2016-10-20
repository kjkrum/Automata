package com.chalcodes.automata.test;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.TransitionLabeler;
import com.chalcodes.automata.regex.Regex;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class RegexTest {
	private static final TransitionLabeler<Byte> gLabeler = new TransitionLabeler<Byte>() {
		@Override
		public String getLabel(@Nonnull final Byte input) {
			final byte b = input;
			if(b > 32 && b < 127) {
				return '\'' + Character.toString((char) b) + '\'';
			}
			else {
				return "0x" + Integer.toHexString(b & 0xFF);
			}
		}
	};

	@Test
	public void parse() throws Exception {
		final Automaton<Byte, String> a = Regex.parse("(AB)+", StandardCharsets.ISO_8859_1);
		a.addSemanticValue("FOO");
		final Automaton<Byte, String> b = Regex.parse("(AB)+C", StandardCharsets.ISO_8859_1);
		b.addSemanticValue("BAR");
		a.union(b);
		a.determinize();
		System.out.println(a.toGraphViz("test", gLabeler));
	}
}