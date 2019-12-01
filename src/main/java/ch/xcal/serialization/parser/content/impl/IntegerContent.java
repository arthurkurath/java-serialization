package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class IntegerContent implements IPrimitiveContent {
	private final int value;

	private IntegerContent(final int value) {
		this.value = value;
	}

	public static IntegerContent create(final int value) {
		return new IntegerContent(value);
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "IntegerContent [value=" + value + "]";
	}
}
