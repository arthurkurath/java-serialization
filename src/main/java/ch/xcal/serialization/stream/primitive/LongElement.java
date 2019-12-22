package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class LongElement implements IPrimitiveElement {
	private final long value;

	private LongElement(final long value) {
		this.value = value;
	}

	public static LongElement create(final long value) {
		return new LongElement(value);
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "LongContent [value=" + value + "]";
	}
}
