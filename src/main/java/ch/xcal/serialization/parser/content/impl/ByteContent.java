package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class ByteContent implements IPrimitiveContent {
	private final byte value;

	private ByteContent(final byte value) {
		this.value = value;
	}

	public static ByteContent create(final byte value) {
		return new ByteContent(value);
	}

	public byte getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ByteContent [value=" + value + "]";
	}
}
