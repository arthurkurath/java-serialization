package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class CharElement implements IPrimitiveElement {
	private final char value;

	private CharElement(final char value) {
		this.value = value;
	}

	public static CharElement create(final char value) {
		return new CharElement(value);
	}

	public char getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "CharContent [value=" + value + "]";
	}
}
