package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Abstract superclass of byte matchers.
 *
 * @author Kevin Krumwiede
 */
abstract class AbstractByteMatcher implements ByteMatcher {
	private static final int INITIAL = 0;
	private static final int NO_MATCH = -1;
	static final int NO_TRANSITION = -1;
	private final int[][] mTransitions;
	private final BitSet mAccepting;

	AbstractByteMatcher(@Nonnull final int[][] states, @Nonnull final BitSet accepting) {
		mTransitions = states;
		mAccepting = accepting;
	}

	AbstractByteMatcher(@Nonnull final AbstractByteMatcher other) {
		mTransitions = other.mTransitions;
		mAccepting = other.mAccepting;
	}

	private int mState = INITIAL;
	private int mMatch = NO_MATCH;
	private int mLength;

	@Override
	public void reset() {
		mState = INITIAL;
		mMatch = NO_MATCH;
	}

	/**
	 * Gets the length of the longest match.
	 *
	 * @return the length of the longest match
	 * @throws IllegalStateException if there is no match
	 */
	@Override
	public int length() {
		if(mMatch == NO_MATCH) {
			throw new IllegalStateException("no match");
		}
		return mLength;
	}

	/**
	 * Gets the state number of the longest match.
	 *
	 * @return the state number of the longest match
	 * @throws IllegalStateException if there is no match
	 */
	int matchState() {
		if(mMatch == NO_MATCH) {
			throw new IllegalStateException("no match");
		}
		return mMatch;
	}

	@Override
	public boolean matches(@Nonnull final ByteBuffer buffer) {
		reset();
		int consumed = 0;
		checkMatch(consumed);
		final int pos = buffer.position();
		final int remaining = buffer.remaining();
		while(consumed < remaining) {
			final int input = buffer.get(pos + consumed) & 0xFF;
			if(step(input)) {
				++consumed;
				checkMatch(consumed);
			}
			else {
				break;
			}
		}
		if(consumed == remaining && hasTransitions()) {
			throw new BufferUnderflowException();
		}
		return mMatch != NO_MATCH;
	}

	private boolean step(int input) {
		final int transition = mTransitions[mState][input];
		if(transition == NO_TRANSITION) {
			return false;
		}
		else {
			mState = transition;
			return true;
		}
	}

	private void checkMatch(final int len) {
		if(mAccepting.get(mState)) {
			mMatch = mState;
			mLength = len;
		}
	}

	private boolean hasTransitions() {
		for(final int t : mTransitions[mState]) {
			if(t != NO_TRANSITION) {
				return true;
			}
		}
		return false;
	}

}
