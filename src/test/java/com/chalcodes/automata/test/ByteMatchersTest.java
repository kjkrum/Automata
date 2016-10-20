package com.chalcodes.automata.test;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.ByteMatchers;
import com.chalcodes.automata.SingleSemanticsByteMatcher;
import com.chalcodes.automata.regex.Regex;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class ByteMatchersTest {

	@Test
	public void matcher() {
		final Automaton<Byte,String> a = Regex.parse("ABC", StandardCharsets.ISO_8859_1);
		a.addSemanticValue("FOO");
		SingleSemanticsByteMatcher<String> m = ByteMatchers.singleSemantics(a);
		final ByteBuffer input = ByteBuffer.wrap("ABC".getBytes(StandardCharsets.ISO_8859_1));
		assertTrue(m.matches(input));
		assertEquals(3, m.length());
		assertEquals("FOO", m.semanticValue());
	}

}