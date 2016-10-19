package com.chalcodes.automata.regex;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.Automatons;

import javax.annotation.Nonnull;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class Regex {
	private Regex() {};

	/* Adapted from http://matt.might.net/articles/parsing-regex-with-recursive-descent */
	// TODO should this produce Automaton<Byte,T> instead?

	@Nonnull public static <T> Automaton<Character, T> parse(@Nonnull final String regex) {
		final RegexIterator iter = new RegexIterator(regex);
		final Automaton<Character, T> parsed = expr(iter);
		/* Extra parentheses bubble up to here. */
		if(iter.hasNext()) {
			throw new ParseException("unexpected character", iter.position());
		}
		return parsed;
	}

	@Nonnull private static <T> Automaton<Character, T> expr(@Nonnull final RegexIterator iter) {
		/* An expression is the union of one or more terms. */
		final Automaton<Character, T> expr = term(iter);
		while(iter.hasNext() && iter.peek() == '|') {
			iter.remove();
			expr.union(Regex.<T>term(iter));
		}
		return expr;
	}

	@Nonnull private static <T> Automaton<Character, T> term(@Nonnull final RegexIterator iter) {
		/* A term is the concatenation of zero or more factors. */
		final Automaton<Character, T> term = Automatons.empty();
		while(iter.hasNext() && iter.peek() != '|' && iter.peek() != ')') {
			term.concat(Regex.<T>factor(iter));
		}
		return term;
	}

	@Nonnull private static <T> Automaton<Character, T> factor(@Nonnull final RegexIterator iter) {
		/* A factor is a base followed by zero or one quantifiers. */
		// TODO zero or more quantifiers?
		final Automaton<Character, T> factor = base(iter);
		/* A quantifier is '*', '+', '?', or "{m,n}". */
		if(iter.hasNext()) {
			switch(iter.peek()) {
				case '*':
					iter.remove();
					factor.star();
					break;
				case '+':
					iter.remove();
					factor.plus();
					break;
				case '?':
					iter.remove();
					factor.optional();
					break;
				// TODO counted
			}
		}
		return factor;
	}

	@Nonnull private static <T> Automaton<Character, T> base(@Nonnull final RegexIterator iter) {
		/* A base is a literal character, an escaped character, a set of
		 * characters, or a parenthesized expression. */
		switch(iter.peek()) {
			case '(':
				iter.remove();
				final Automaton<Character, T> base = expr(iter);
				iter.require(')');
				return base;
			case '\\':
				iter.remove();
				// TODO restrict which characters can be escaped?
				// TODO character classes? (digits, etc.)
				// fall through to default case
			default:
				return Automatons.symbol(iter.remove());
		}
	}
}
