package com.chalcodes.automata.regex;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.Automatons;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Parses regular expressions.
 *
 * @author Kevin Krumwiede
 */
public class Regex {
	private Regex() {}

	@Nonnull public static <T> Automaton<Byte, T> parse(@Nonnull final CharSequence regex, @Nonnull final Charset charset) {
		final RegexIterator iter = new RegexIterator(regex);
		final CharsetEncoder encoder = charset.newEncoder();
		final CharBuffer input = CharBuffer.allocate(2);
		final ByteBuffer output = ByteBuffer.allocate((int) Math.ceil(encoder.maxBytesPerChar()));
		final Automaton<Byte, T> parsed = expr(iter, encoder, input, output);
		if(iter.hasNext()) {
			throw new ParseException("unexpected character", input.position());
		}
		return parsed;
	}

	@Nonnull private static <T> Automaton<Byte,T> expr(final RegexIterator iter,
													   final CharsetEncoder encoder,
													   final CharBuffer input,
													   final ByteBuffer output) {
		/* An expression is the union of one or more terms. */
		final Automaton<Byte, T> expr = term(iter, encoder, input, output);
		while(iter.hasNext() && iter.peek() == '|') {
			input.get();
			expr.union(Regex.<T>term(iter, encoder, input, output));
		}
		return expr;
	}

	@Nonnull private static <T> Automaton<Byte,T> term(final RegexIterator iter,
													   final CharsetEncoder encoder,
													   final CharBuffer input,
													   final ByteBuffer output) {
		/* A term is the concatenation of zero or more factors. */
		final Automaton<Byte, T> term = Automatons.empty();
		while(iter.hasNext()) {
			final char next = iter.peek();
			if(next == '|' || next == ')') {
				break;
			}
			term.concat(Regex.<T>factor(iter, encoder, input, output));
		}
		return term;
	}

	@Nonnull private static <T> Automaton<Byte,T> factor(final RegexIterator iter,
														 final CharsetEncoder encoder,
														 final CharBuffer input,
														 final ByteBuffer output) {
		/* A factor is a base followed by zero or one quantifiers. */
		// TODO zero or more quantifiers?
		final Automaton<Byte, T> factor = base(iter, encoder, input, output);
		/* A quantifier is '*', '+', '?', or "{m,n}". */
		if(iter.hasNext()) {
			switch(iter.peek()) {
				case '*':
					iter.skip();
					factor.star();
					break;
				case '+':
					iter.skip();
					factor.plus();
					break;
				case '?':
					iter.skip();
					factor.optional();
					break;
				// TODO counted
			}
		}
		return factor;
	}

	@Nonnull private static <T> Automaton<Byte,T> base(final RegexIterator iter,
													   final CharsetEncoder encoder,
													   final CharBuffer input,
													   final ByteBuffer output) {
		/* A base is a literal character, an escaped character, a set of
		 * characters, or a parenthesized expression. */
		switch(iter.peek()) {
			case '(':
				iter.skip();
				final Automaton<Byte, T> base = expr(iter, encoder, input, output);
				iter.require(')');
				return base;
			case '\\':
				iter.skip();
				switch(iter.peek()) {
					case '(':
					case ')':
					case '{':
					case '}':
					case '[':
					case ']':
					case '|':
					case '\\':
						// fall through to default case of outer switch
						break;
					// TODO recognize character classes? (digits, etc.)
					default:
						throw new ParseException("unexpected character", iter.position());
				}
			default:
				input.clear();
				iter.next(input);
				input.flip();
				output.clear();
				encoder.reset();
				encoder.encode(input, output, true);
				encoder.flush(output);
				output.flip();
				return Automatons.sequence(output);
		}
	}
}
