package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class ShortElement implements IPrimitiveElement {
	private final short value;

	private ShortElement(final short value) {
		this.value = value;
	}

	public static ShortElement create(final short value) {
		return new ShortElement(value);
	}

	public short getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ShortContent [value=" + value + "]";
	}
}
