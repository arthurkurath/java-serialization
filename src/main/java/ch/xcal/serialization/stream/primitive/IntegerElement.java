package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class IntegerElement implements IPrimitiveElement {
	private final int value;

	private IntegerElement(final int value) {
		this.value = value;
	}

	public static IntegerElement create(final int value) {
		return new IntegerElement(value);
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "IntegerContent [value=" + value + "]";
	}
}
