package com.chalcodes.automata.test;

import com.chalcodes.automata.Automaton;
import com.chalcodes.automata.Automatons;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class AutomatonTest {

	private static final List<String> FOO_ETC;

	static {
		final List<String> strings = new LinkedList<>();
		strings.add("foo");
		strings.add("bar");
		strings.add("baz");
		FOO_ETC = Collections.unmodifiableList(strings);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void copyStatesAreDisjoint() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Automaton<String, Void> a = Automatons.sequence(FOO_ETC);
		Automaton<String, Void> b = a.copy();
		Field mInitial = Automaton.class.getDeclaredField("mInitial");
		mInitial.setAccessible(true);
		final Object aInitial = mInitial.get(a);
		final Object bInitial = mInitial.get(b);
		Method findReachable = aInitial.getClass().getDeclaredMethod("findReachable");
		findReachable.setAccessible(true);
		Set<Object> aReachable = (Set<Object>) findReachable.invoke(aInitial);
		Set<Object> bReachable = (Set<Object>) findReachable.invoke(bInitial);
		assertTrue(Collections.disjoint(aReachable, bReachable));
	}

//	@Test
//	public void graphViz() {
//		Automaton<String, Void> a = Automatons.sequence(FOO_ETC);
//		System.out.println(a.toGraphViz("test", new TransitionLabeler<String>() {
//			@Override
//			public String getLabel(@Nonnull final String input) {
//				return input;
//			}
//		}));
//	}

}