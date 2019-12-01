package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class LongContent implements IPrimitiveContent {
	private final long value;

	private LongContent(final long value) {
		this.value = value;
	}

	public static LongContent create(final long value) {
		return new LongContent(value);
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "LongContent [value=" + value + "]";
	}
}
