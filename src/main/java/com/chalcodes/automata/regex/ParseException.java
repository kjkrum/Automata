package com.chalcodes.automata.regex;

import javax.annotation.Nonnull;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class ParseException extends RuntimeException {
	final int mPosition;

	ParseException(@Nonnull final String message, final int position) {
		super(message);
		mPosition = position;
	}

	public int getPosition() {
		return mPosition;
	}

}
