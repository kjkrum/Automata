package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A mutable automaton that operates on arbitrary input symbols.  May be
 * deterministic or non-deterministic.
 *
 * @param <S> the type of symbol received as input
 * @param <T> the type of semantic value associated with accepting states
 * @author Kevin Krumwiede
 */
public class Automaton<S,T> {
	private State<S,T> mInitial;
	private final Set<State<S,T>> mAccepting = new HashSet<>();
	private boolean mCannibalized;
	private boolean mDeterminized;

	Automaton(@Nonnull final State<S, T> initial) {
		mInitial = initial;
		findAccepting();
	}

	private void findAccepting() {
		mAccepting.clear();
		for(final State<S,T> state : mInitial.findReachable()) {
			if(state.isAccepting()) {
				mAccepting.add(state);
			}
		}
	}

	public boolean isCannibalized() {
		return mCannibalized;
	}

	void checkCannibalized() {
		if(mCannibalized) {
			throw new IllegalStateException(this + " has been cannibalized");
		}
	}

	private void cannibalize() {
		checkCannibalized();
		mCannibalized = true;
	}

	public Automaton<S,T> copy() {
		checkCannibalized();
		final Map<State<S,T>, State<S,T>> map = mInitial.copy();
		final Automaton<S,T> copy = new Automaton<>(map.get(mInitial));
		copy.mDeterminized = mDeterminized;
		return copy;
	}

	/**
	 * Modifies this automaton so it accepts zero or one of its language.
	 *
	 * @return this automaton
	 */
	public Automaton<S,T> optional() {
		checkCannibalized();
		mInitial.addEpsilons(mAccepting);
		mDeterminized = false;
		return this;
	}

	/**
	 * Modifies this automaton so it accepts one or more of its language.
	 *
	 * @return this automaton
	 */
	public Automaton<S,T> plus() {
		checkCannibalized();
		for(final State<S,T> state : mAccepting) {
			state.addEpsilon(mInitial);
		}
		mDeterminized = false;
		return this;
	}

	/**
	 * Modifies this automaton so it accepts zero or more of its language.
	 *
	 * @return this automaton
	 */
	public Automaton<S,T> star() {
		checkCannibalized();
		for(final State<S,T> accept : mAccepting) {
			accept.addEpsilon(mInitial);
			mInitial.addEpsilon(accept);
		}
		mDeterminized = false;
		return this;
	}

	// TODO counted (count)
	// TODO counted (min, max)

	/**
	 * Modifies this automaton so it accepts the concatenation of its language
	 * with that of another automaton.  The other automaton will be
	 * cannibalized.
	 *
	 * @param other the other automaton
	 * @return this automaton
	 */
	public Automaton<S,T> concat(@Nonnull final Automaton<S,T> other) {
		if(other == this) {
			throw new IllegalArgumentException("other == this");
		}
		checkCannibalized();
		other.cannibalize();
		for(final State<S,T> state : mAccepting) {
			state.setAccepting(false);
			state.addEpsilon(other.mInitial);
		}
		mAccepting.clear();
		mAccepting.addAll(other.mAccepting);
		mDeterminized = false;
		return this;
	}

	/**
	 * Modifies this automaton so it accepts the union (or alternation) of its
	 * language with that of another automaton.  The other automaton will be
	 * cannibalized.
	 *
	 * @param other the other automaton
	 * @return this automaton
	 */
	public Automaton<S,T> union(@Nonnull final Automaton<S,T> other) {
		if(other == this) {
			throw new IllegalArgumentException("other == this");
		}
		checkCannibalized();
		other.cannibalize();
		final State<S,T> initial = new State<>();
		initial.addEpsilon(mInitial);
		initial.addEpsilon(other.mInitial);
		mInitial = initial;
		mAccepting.addAll(other.mAccepting);
		mDeterminized = false;
		return this;
	}

	/**
	 * Adds a semantic value to all accepting states of this automaton.
	 *
	 * @param semanticValue the semantic value
	 * @return this automaton
	 */
	public Automaton<S,T> addSemanticValue(@Nonnull T semanticValue) {
		checkCannibalized();
		//noinspection ConstantConditions - public API
		if(semanticValue == null) {
			throw new NullPointerException();
		}
		for(final State<S,T> state : mAccepting) {
			state.addSemanticValue(semanticValue);
		}
		return this;
	}

	/**
	 * Tests whether this automaton has single semantics.  This automaton will
	 * be determinized.
	 *
	 * @return this automaton
	 */
	public boolean hasSingleSemantics() {
		checkCannibalized();
		determinize();
		for(final State<S,T> state : mAccepting) {
			if(state.hasMultipleSemantics()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Maps all reachable states to unique IDs.  Mappings may not be stable
	 * from one invocation to the next.
	 *
	 * @return the map of states to IDs
	 */
	Map<State<S,T>, Integer> getStateIds() {
		final Map<State<S,T>, Integer> stateIds = new HashMap<>();
		int id = 0;
		for(final State<S,T> state : mInitial.findReachable()) {
			stateIds.put(state, id++);
		}
		return stateIds;
	}

	public Automaton<S,T> determinize() {
		checkCannibalized();
		if(!mDeterminized) {
			/* See https://www.youtube.com/watch?v=taClnxU-nao */
			final Set<State<S,T>> nfaStates = mInitial.findReachable();
			/* Cache the epsilon closures of the original NFA states. */
			final Map<State<S,T>, Set<State<S,T>>> epsilonClosures = new HashMap<>();
			for(final State<S,T> state : nfaStates) {
				epsilonClosures.put(state, state.getEpsilonClosure());
			}
			/* Create a map of power sets to intermediate temp states. */
			final Map<Set<State<S,T>>, State<S,T>> powerSetsToTempStates = new HashMap<>();
			/* Queue of power sets that need to have their temp states populated. */
			final Queue<Set<State<S,T>>> queue = new LinkedList<>();
			/* Start things off with the epsilon closure of the initial state. */
			final Set<State<S,T>> init = epsilonClosures.get(mInitial);
			powerSetsToTempStates.put(init, new State<S,T>());
			queue.add(init);
			/* Process the queue. */
			final Set<S> inputs = new HashSet<>();
			final Set<State<S,T>> transitions = new HashSet<>();
			while(!queue.isEmpty()) {
				final Set<State<S,T>> powerSet = queue.remove();
				State<S,T> tempState = powerSetsToTempStates.get(powerSet);
				assert tempState != null;
				/* For each state in the power set... */
				for(final State<S,T> nfaState : powerSet) {
					/* For each input... */
					nfaState.getInputs(inputs);
					if(!inputs.isEmpty()) {
						for(final S input : inputs) {
							/* For each transition for that input... */
							nfaState.getTransitions(input, transitions);
							assert !transitions.isEmpty();
							for(final State<S,T> transition : transitions) {
								/* Add the transition's epsilon closure to the
								 * temp state's transition set for the input. */
								tempState.addTransitions(input, epsilonClosures.get(transition));
							}
							transitions.clear();
						}
						inputs.clear();
					}
					if(nfaState.isAccepting()) {
						tempState.setAccepting(true);
						tempState.addSemanticValues(nfaState);
					}
				}
				/* The transition set for each input to tempState should be a
				 * key in powerSetsToTempStates.  Add any that are not already
				 * present. */
				tempState.getInputs(inputs);
				if(!inputs.isEmpty()) {
					for(final S input : inputs) {
						tempState.getTransitions(input, transitions);
						assert !transitions.isEmpty();
						if(!powerSetsToTempStates.containsKey(transitions)) {
							final Set<State<S,T>> copy = new HashSet<>(transitions);
							powerSetsToTempStates.put(copy, new State<S,T>());
							queue.add(copy);
						}
						transitions.clear();
					}
					inputs.clear();
				}
			}
			/* Each transition set for each temp state in
			 * powerSetsToTempStates is also a key in powerSetsToTempStates.
			 * Each key maps to a state in the final DFA. */
			final Map<Set<State<S,T>>, State<S,T>> powerSetsToDfaStates = new HashMap<>();
			/* For simplicity, first create all the entries. */
			for(final Set<State<S,T>> powerSet : powerSetsToTempStates.keySet()) {
				powerSetsToDfaStates.put(powerSet, new State<S,T>());
			}
			for(final Set<State<S,T>> powerSet : powerSetsToTempStates.keySet()) {
				final State<S,T> tempState = powerSetsToTempStates.get(powerSet);
				final State<S,T> dfaState = powerSetsToDfaStates.get(powerSet);
				tempState.getInputs(inputs);
				if(!inputs.isEmpty()) {
					for(final S input : inputs) {
						tempState.getTransitions(input, transitions);
						assert !transitions.isEmpty();
						assert powerSetsToDfaStates.get(transitions) != null;
						dfaState.addTransition(input, powerSetsToDfaStates.get(transitions));
						transitions.clear();
					}
					inputs.clear();
				}
				if(tempState.isAccepting()) {
					dfaState.setAccepting(true);
					dfaState.addSemanticValues(tempState);
				}
			}
			/* Rebuild the pattern. */
			mInitial = powerSetsToDfaStates.get(init);
			findAccepting();
			mDeterminized = true;
		}
		return this;
	}

	public String toGraphViz(@Nonnull final String name, @Nonnull final TransitionLabeler<S> labeler) {
		final Map<State<S,T>, Integer> stateIds = getStateIds();
		StringBuilder sb = new StringBuilder();
		sb.append("digraph ").append(name).append(" {\n\trankdir=LR\n");
		sb.append("\tnode [shape=circle fixedsize=shape]\n");
		/* Declare nodes. */
		Set<T> semanticValues = new HashSet<T>();
		for(final State<S,T> state : stateIds.keySet()) {
			sb.append('\t').append(stateIds.get(state));
			sb.append(" [label=\"").append(stateIds.get(state));
			if(state.isAccepting()) {
				semanticValues.clear();
				state.getSemanticValues(semanticValues);
				if(!semanticValues.isEmpty()) {
					sb.append("\\n");
					for(T info : semanticValues) {
						sb.append(info).append(", ");
					}
					sb.setLength(sb.length() - 2);
					semanticValues.clear();
				}
				sb.append("\" shape=doublecircle");
			}
			else {
				sb.append('"'); // just close the label
			}
			sb.append("];\n");
		}
		/* Declare edges. */
		final Set<S> inputs = new HashSet<>();
		final Set<State<S,T>> edges = new HashSet<>();
		for(final State<S,T> state : stateIds.keySet()) {
			/* Epsilon transitions... */
			state.getEpsilons(edges);
			if(!edges.isEmpty()) {
				sb.append('\t').append(stateIds.get(state)).append(" -> { ");
				for(final State<S,T> e : edges) {
					if(e != state) { // omit self-epsilons
						sb.append(stateIds.get(e)).append(' ');
					}
				}
				sb.append("} [label=\"\u03b5\"];\n");
				edges.clear();
			}
			/* Input transitions... */
			state.getInputs(inputs);
			if(!inputs.isEmpty()) {
				for(final S input : inputs) {
					sb.append('\t').append(stateIds.get(state)).append(" -> { ");
					state.getTransitions(input, edges);
					if(!edges.isEmpty()) {
						for(final State<S,T> transition : edges) {
							sb.append(stateIds.get(transition)).append(' ');
						}
						sb.append("} [label=\"").append(labeler.getLabel(input)).append("\"];\n");
						edges.clear();
					}
				}
				inputs.clear();
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
