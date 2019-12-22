package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class FloatElement implements IPrimitiveElement {
	private final float value;

	private FloatElement(final float value) {
		this.value = value;
	}

	public static FloatElement create(final float value) {
		return new FloatElement(value);
	}

	public float getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "FloatContent [value=" + value + "]";
	}
}
