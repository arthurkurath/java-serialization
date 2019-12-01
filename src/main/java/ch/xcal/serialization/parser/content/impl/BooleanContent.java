package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class BooleanContent implements IPrimitiveContent {
	private final boolean value;

	private BooleanContent(final boolean value) {
		this.value = value;
	}

	public static BooleanContent create(final boolean value) {
		return new BooleanContent(value);
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "BooleanContent [value=" + value + "]";
	}
}
