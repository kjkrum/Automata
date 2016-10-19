package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class MultipleSemanticsByteMatcher<T> extends AbstractByteMatcher implements MultipleSemantics<T> {
	private final List<Set<T>> mSemanticValues;

	MultipleSemanticsByteMatcher(@Nonnull final int[][] transitions,
								 @Nonnull final BitSet accepting,
								 @Nonnull final List<Set<T>> semanticValues) {
		super(transitions, accepting);
		mSemanticValues = semanticValues;
	}

	private MultipleSemanticsByteMatcher(@Nonnull final MultipleSemanticsByteMatcher<T> other) {
		super(other);
		mSemanticValues = other.mSemanticValues;
	}

	@Override
	@Nonnull public Set<T> semanticValues() {
		/* Sets in list are unmodifiable, and sets for accepting states are
		 * guaranteed non-null. */
		return mSemanticValues.get(matchState());
	}

	public MultipleSemanticsByteMatcher<T> copy() {
		return new MultipleSemanticsByteMatcher<>(this);
	}
}
