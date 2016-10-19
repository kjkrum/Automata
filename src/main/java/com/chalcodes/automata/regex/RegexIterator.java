package com.chalcodes.automata.regex;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
class RegexIterator {
	private final String mRegex;
	private int mNext = 0;

	RegexIterator(@Nonnull final String regex) {
		mRegex = regex;
	}

	/**
	 * Gets the index of the next character.
	 *
	 * @return the intext of the next character
	 */
	int position() {
		return mNext;
	}

	/**
	 * Tests whether this iterator has more characters.
	 *
	 * @return true if this iterator has more characters; otherwise false
	 */
	boolean hasNext() {
		return mNext < mRegex.length();
	}

	/**
	 * Gets the next character without removing it.
	 *
	 * @return the next character
	 */
	char peek() {
		if(hasNext()) {
			return mRegex.charAt(mNext);
		}
		else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Removes the next character.
	 *
	 * @return the next character
	 */
	char remove() {
		if(hasNext()) {
			return mRegex.charAt(mNext++);
		}
		else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Asserts that the next character is equal to the required character.  If
	 * so, the character is removed.
	 *
	 * @param required the next character
	 * @throws ParseException if the next character is not equal to the
	 * required character
	 */
	void require(final char required) {
		if(hasNext() && peek() == required) {
			remove();
		}
		else {
			throw new ParseException("'" + required + "' expected", mNext);
		}
	}
}
