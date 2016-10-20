package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.chalcodes.automata.AbstractByteMatcher.NO_TRANSITION;

/**
 * Produces table-based byte matchers.
 *
 * @author Kevin Krumwiede
 */
public class ByteMatchers {

	public static <T> SingleSemanticsByteMatcher<T> singleSemantics(@Nonnull final Automaton<Byte,T> automaton) {
		automaton.checkCannibalized();
		automaton.determinize();
		final Map<State<Byte,T>, Integer> stateIds = automaton.getStateIds();
		final int[][] transitions = getTransitionTable(stateIds);
		final BitSet accepting = getAccepting(stateIds);
		final List<T> semanticValues = getSingleSemanticValues(stateIds);
		return new SingleSemanticsByteMatcher<>(transitions, accepting, semanticValues);
	}

	public static <T> MultipleSemanticsByteMatcher<T> multipleSemantics(@Nonnull final Automaton<Byte,T> automaton) {
		automaton.checkCannibalized();
		automaton.determinize();
		final Map<State<Byte,T>, Integer> stateIds = automaton.getStateIds();
		final int[][] transitions = getTransitionTable(stateIds);
		final BitSet accepting = getAccepting(stateIds);
		final List<Set<T>> semanticValues = getMultipleSemanticValues(stateIds);
		return new MultipleSemanticsByteMatcher<>(transitions, accepting, semanticValues);
	}

	private static <T> int[][] getTransitionTable(@Nonnull final Map<State<Byte,T>, Integer> stateIds) {
		final int[][] table = new int[stateIds.size()][];
		final Set<Byte> inputs = new HashSet<>();
		final Set<State<Byte,T>> transitions = new HashSet<>();
		for(final State<Byte,T> state : stateIds.keySet()) {
			final int[] row = new int[256];
			Arrays.fill(row, NO_TRANSITION);
			state.getInputs(inputs);
			for(final Byte b : inputs) {
				state.getTransitions(b, transitions);
				if(!transitions.isEmpty()) {
					final State<Byte,T> transition = transitions.iterator().next();
					final int i = 0xFF & b;
					row[i] = stateIds.get(transition);
					transitions.clear();
				}
			}
			inputs.clear();
			table[stateIds.get(state)] = row;
		}
		return table;
	}

	private static <T> BitSet getAccepting(@Nonnull final Map<State<Byte,T>, Integer> stateIds) {
		final BitSet accepting = new BitSet(stateIds.size());
		for(final State<Byte,T> state : stateIds.keySet()) {
			if(state.isAccepting()) {
				accepting.set(stateIds.get(state));
			}
		}
		return accepting;
	}

	private static <T> List<T> getSingleSemanticValues(@Nonnull final Map<State<Byte,T>, Integer> stateIds) {
		final List<T> semanticValues = new ArrayList<>(Collections.nCopies(stateIds.size(), (T) null));
		final Set<T> set = new HashSet<>();
		for(final State<Byte,T> state : stateIds.keySet()) {
			if(state.isAccepting()) {
				set.clear();
				state.getSemanticValues(set);
				if(set.size() == 1) {
					semanticValues.set(stateIds.get(state), set.iterator().next());
				}
				else if(set.size() > 1) {
					throw new IllegalArgumentException("multiple semantics");
				}
			}
		}
		return semanticValues;
	}

	private static <T> List<Set<T>> getMultipleSemanticValues(@Nonnull final Map<State<Byte,T>, Integer> stateIds) {
		final List<Set<T>> semanticValues = new ArrayList<>(Collections.nCopies(stateIds.size(), (Set<T>) null));
		for(final State<Byte,T> state : stateIds.keySet()) {
			if(state.isAccepting()) {
				semanticValues.set(stateIds.get(state), state.getSemanticValues());
			}
		}
		return semanticValues;
	}
}
