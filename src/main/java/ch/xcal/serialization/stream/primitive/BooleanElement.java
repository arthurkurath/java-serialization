package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class BooleanElement implements IPrimitiveElement {
	private final boolean value;

	private BooleanElement(final boolean value) {
		this.value = value;
	}

	public static BooleanElement create(final boolean value) {
		return new BooleanElement(value);
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "BooleanContent [value=" + value + "]";
	}
}
