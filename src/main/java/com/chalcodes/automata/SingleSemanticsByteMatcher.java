package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.List;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class SingleSemanticsByteMatcher<T> extends AbstractByteMatcher implements SingleSemantics<T> {
	private final List<T> mSemanticValues;

	SingleSemanticsByteMatcher(@Nonnull final int[][] transitions,
							   @Nonnull final BitSet accepting,
							   @Nonnull final List<T> semanticValues) {
		super(transitions, accepting);
		mSemanticValues = semanticValues;
	}

	private SingleSemanticsByteMatcher(@Nonnull final SingleSemanticsByteMatcher<T> other) {
		super(other);
		mSemanticValues = other.mSemanticValues;
	}

	@Override
	public T semanticValue() {
		return mSemanticValues.get(matchState());
	}

	public SingleSemanticsByteMatcher<T> copy() {
		return new SingleSemanticsByteMatcher<>(this);
	}
}
