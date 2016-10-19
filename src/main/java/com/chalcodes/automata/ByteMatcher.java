package com.chalcodes.automata;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

/**
 * TODO javadoc
 *
 * @author Kevin Krumwiede
 */
public interface ByteMatcher {
	void reset();
	int length();
	boolean matches(@Nonnull ByteBuffer buffer);
}
