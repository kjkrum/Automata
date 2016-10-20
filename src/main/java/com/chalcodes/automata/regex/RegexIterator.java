package com.chalcodes.automata.regex;

import javax.annotation.Nonnull;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;

/**
 * Iterates over a regular expression.
 *
 * @author Kevin Krumwiede
 */
class RegexIterator {
	private final CharBuffer mBuffer;

	RegexIterator(@Nonnull final CharSequence regex) {
		mBuffer = CharBuffer.wrap(regex);
	}

	/**
	 * Gets the index of the next character.
	 *
	 * @return the index of the next character.
	 */
	int position() {
		return mBuffer.position();
	}

	/**
	 * Tests whether this iterator has more characters.
	 *
	 * @return true if this iterator has more characters; otherwise false
	 */
	boolean hasNext() {
		return mBuffer.hasRemaining();
	}

	/**
	 * Advances the iterator position by one character.
	 */
	void skip() {
		mBuffer.position(mBuffer.position() + 1);
	}

	/**
	 * Copies the next character, or two if the next character is the first of
	 * a surrogate pair.
	 *
	 * @param buffer the buffer to receive the characters
	 */
	void next(@Nonnull final CharBuffer buffer) {
		final char c = peek();
		if(Character.isHighSurrogate(c)) {
			if(mBuffer.remaining() < 2) {
				throw new ParseException("incomplete surrogate pair", mBuffer.position());
			}
			if(buffer.remaining() < 2) {
				throw new BufferOverflowException();
			}
			buffer.put(mBuffer.get());
			buffer.put(mBuffer.get());
		}
		else {
			buffer.put(mBuffer.get());
		}
	}

	/**
	 * Gets the next character without removing it.
	 *
	 * @return the next character
	 */
	char peek() {
		return mBuffer.get(mBuffer.position());
	}

	/**
	 * Asserts that the next character is equal to the required character.  If
	 * so, the character is skipped.
	 *
	 * @param c the required character
	 * @throws ParseException if the next character is not equal to the
	 * required character
	 */
	void require(final char c) {
		if(peek() == c) {
			skip();
		}
		else {
			throw new ParseException("'" + c + "' expected", mBuffer.position());
		}
	}
}
