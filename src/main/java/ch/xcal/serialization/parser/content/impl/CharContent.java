package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class CharContent implements IPrimitiveContent {
	private final char value;

	private CharContent(final char value) {
		this.value = value;
	}

	public static CharContent create(final char value) {
		return new CharContent(value);
	}

	public char getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "CharContent [value=" + value + "]";
	}
}
