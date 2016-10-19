package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public interface MultipleSemantics<T> {
	@Nonnull
	Set<T> semanticValues();
}
