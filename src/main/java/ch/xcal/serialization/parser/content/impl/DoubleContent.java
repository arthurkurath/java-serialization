package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class DoubleContent implements IPrimitiveContent {
	private final double value;

	private DoubleContent(final double value) {
		this.value = value;
	}

	public static DoubleContent create(final double value) {
		return new DoubleContent(value);
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "DoubleContent [value=" + value + "]";
	}
}
