package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public class Automatons {
	private Automatons() {}

//	public static <S,T> Automaton<S,T> none() {
//		final State<S,T> initial = new State<>();
//		return new Automaton<>(initial);
//	}

	public static <S,T> Automaton<S,T> empty() {
		final State<S,T> initial = new State<>();
		initial.setAccepting(true);
		return new Automaton<>(initial);
	}

	public static <S,T> Automaton<S,T> symbol(@Nonnull final S input) {
		final State<S,T> initial = new State<>();
		final State<S,T> accept = new State<>();
		initial.addTransition(input, accept);
		accept.setAccepting(true);
		return new Automaton<>(initial);
	}

	public static <S,T> Automaton<S,T> sequence(@Nonnull final List<S> symbols) {
		if(symbols.size() == 0) {
			return empty();
		}
		final State<S,T> initial = new State<>();
		State<S,T> tail = initial;
		for(final S symbol : symbols) {
			final State<S,T> next = new State<>();
			tail.addTransition(symbol, next);
			tail = next;
		}
		tail.setAccepting(true);
		return new Automaton<>(initial);
	}

	public static <T> Automaton<Byte, T> sequence(@Nonnull final ByteBuffer buffer) {
		final State<Byte,T> initial = new State<>();
		State<Byte,T> tail = initial;
		while(buffer.hasRemaining()) {
			final State<Byte,T> next = new State<>();
			tail.addTransition(buffer.get(), next);
			tail = next;
		}
		tail.setAccepting(true);
		return new Automaton<>(initial);
	}

	public static <S,T> Automaton<S,T> set(@Nonnull final Collection<S> symbols) {
		if(symbols.size() == 0) {
			return empty();
		}
		final State<S,T> initial = new State<>();
		final State<S,T> accept = new State<>();
		accept.setAccepting(true);
		for(final S symbol : symbols) {
			initial.addTransition(symbol, accept);
		}
		return new Automaton<>(initial);
	}
}
