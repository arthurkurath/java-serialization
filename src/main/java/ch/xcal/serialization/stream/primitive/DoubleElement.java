package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class DoubleElement implements IPrimitiveElement {
	private final double value;

	private DoubleElement(final double value) {
		this.value = value;
	}

	public static DoubleElement create(final double value) {
		return new DoubleElement(value);
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "DoubleContent [value=" + value + "]";
	}
}
