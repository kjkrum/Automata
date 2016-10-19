package com.chalcodes.automata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * TODO javadoc
 *
 * @param <S> the type of symbol received as input
 * @param <T> the type of semantic value associated with accepting states
 * @author Kevin Krumwiede
 */
public class State<S,T> {
	@Nullable private Map<S, Set<State<S,T>>> mTransitions;
	@Nullable private Set<State<S,T>> mEpsilons;
	private boolean mAccepting;
	@Nullable private Set<T> mSemanticValues;

	void addEpsilon(@Nonnull final State<S,T> epsilon) {
		if(mEpsilons == null) {
			mEpsilons = new HashSet<>();
		}
		mEpsilons.add(epsilon);
	}

	void addEpsilons(@Nonnull final Collection<State<S,T>> epsilons) {
		if(!epsilons.isEmpty()) {
			if(mEpsilons == null) {
				mEpsilons = new HashSet<>();
			}
			mEpsilons.addAll(epsilons);
		}
	}

	void getEpsilons(@Nonnull final Set<State<S,T>> result) {
		if(mEpsilons != null) {
			result.addAll(mEpsilons);
			result.add(this);
		}
	}

	Set<State<S,T>> getEpsilonClosure() {
		final Set<State<S,T>> result = new HashSet<>();
		getEpsilonClosure(result);
		return result;
	}

	private void getEpsilonClosure(@Nonnull final Collection<State<S,T>> result) {
		result.add(this);
		if(mEpsilons != null) {
			for(State<S,T> state : mEpsilons) {
				if(result.add(state)) {
					state.getEpsilonClosure(result);
				}
			}
		}
	}

	Set<State<S,T>> findReachable() {
		final Set<State<S,T>> reachable = new LinkedHashSet<>();
		final Queue<State<S,T>> queue = new LinkedList<>();
		reachable.add(this);
		queue.add(this);
		while(!queue.isEmpty()) {
			final State<S,T> state = queue.remove();
			if(state.mEpsilons != null) {
				for(final State<S,T> epsilon : state.mEpsilons) {
					if(reachable.add(epsilon)) {
						queue.add(epsilon);
					}
				}
			}
			final Set<State<S,T>> transitions = new HashSet<>();
			if(state.mTransitions != null) {
				for(S input : state.mTransitions.keySet()) {
					transitions.clear();
					state.getTransitions(input, transitions);
					for(State<S,T> transition : transitions) {
						if(reachable.add(transition)) {
							queue.add(transition);
						}
					}
				}
			}
		}
		return reachable;
	}

	boolean isAccepting() {
		return mAccepting;
	}

	void setAccepting(boolean accepting) {
		mAccepting = accepting;
		if(!mAccepting) {
			mSemanticValues = null;
		}
	}

	void addSemanticValue(@Nonnull final T semanticValue) {
		if(!mAccepting) {
			throw new IllegalStateException("not accepting");
		}
		if(mSemanticValues == null) {
			mSemanticValues = new HashSet<>();
		}
		mSemanticValues.add(semanticValue);
	}

	void addSemanticValues(@Nonnull State<S,T> other) {
		if(!mAccepting) {
			throw new IllegalStateException("not accepting");
		}
		if(other.mSemanticValues != null) {
			if(mSemanticValues == null) {
				mSemanticValues = new HashSet<>();
			}
			mSemanticValues.addAll(other.mSemanticValues);
		}
	}

	boolean hasSemanticValues() {
		return mSemanticValues != null;
	}

	boolean hasMultipleSemantics() {
		return mSemanticValues != null && mSemanticValues.size() > 1;
	}

	void getSemanticValues(@Nonnull final Set<T> result) {
		if(mSemanticValues != null) {
			result.addAll(mSemanticValues);
		}
	}

	Set<T> getSemanticValues() {
		return mSemanticValues == null ?
				Collections.<T>emptySet() :
				Collections.unmodifiableSet(new HashSet<>(mSemanticValues));
	}

	@Nonnull Map<State<S,T>, State<S,T>> copy() {
		final Map<State<S,T>, State<S,T>> map = new HashMap<>();
		copy(map);
		return map;
	}

	@Nonnull private State<S,T> copy(@Nonnull final Map<State<S,T>, State<S,T>> map) {
		if(map.containsKey(this)) {
			return map.get(this);
		}
		else {
			final State<S,T> copy = new State<>();
			map.put(this, copy);
			if(mEpsilons != null) {
				for(final State<S,T> epsilon : mEpsilons) {
					copy.addEpsilon(epsilon.copy(map));
				}
			}
			if(mTransitions != null) {
				final Set<State<S,T>> transitions = new HashSet<>();
				for(S input : mTransitions.keySet()) {
					transitions.clear();
					getTransitions(input, transitions);
					for(final State<S,T> transition : transitions) {
						copy.addTransition(input, transition.copy(map));
					}
				}
			}
			copy.mAccepting = mAccepting;
			if(mSemanticValues != null) {
				copy.mSemanticValues = new HashSet<>();
				copy.mSemanticValues.addAll(mSemanticValues);
			}
			return copy;
		}
	}

	boolean hasTransitions() {
		return mTransitions != null;
	}

	void addTransition(@Nonnull final S input, @Nonnull final State<S,T> transition) {
		//noinspection ConstantConditions
		assert input != null;
		//noinspection ConstantConditions
		assert transition != null;
		getOrCreateTransitions(input).add(transition);
	}

	void addTransitions(@Nonnull final S input, @Nonnull final Set<State<S,T>> transitions) {
		getOrCreateTransitions(input).addAll(transitions);
	}

	private Set<State<S,T>> getOrCreateTransitions(@Nonnull final S input) {
		if(mTransitions == null) {
			mTransitions = new HashMap<>();
		}
		Set<State<S,T>> transitions = mTransitions.get(input);
		if(transitions == null) {
			transitions = new HashSet<>();
			mTransitions.put(input, transitions);
		}
		return transitions;
	}

	void getInputs(@Nonnull final Set<S> result) {
		if(mTransitions != null) {
			result.addAll(mTransitions.keySet());
		}
	}

	void getTransitions(@Nonnull final S input, @Nonnull final Set<State<S,T>> result) {
		if(mTransitions != null && mTransitions.containsKey(input)) {
			result.addAll(mTransitions.get(input));
		}
	}
}
