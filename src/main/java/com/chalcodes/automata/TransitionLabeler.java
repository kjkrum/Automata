package com.chalcodes.automata;

import javax.annotation.Nonnull;

/**
 * Labels transitions in the GraphViz representation of an {@code Automaton}.
 *
 * @author Kevin Krumwiede
 * @see Automaton#toGraphViz(String, TransitionLabeler)
 */
public interface TransitionLabeler<S> {
	String getLabel(@Nonnull S input);
}
