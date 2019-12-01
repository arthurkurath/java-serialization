package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class ShortContent implements IPrimitiveContent {
	private final short value;

	private ShortContent(final short value) {
		this.value = value;
	}

	public static ShortContent create(final short value) {
		return new ShortContent(value);
	}

	public short getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ShortContent [value=" + value + "]";
	}
}
