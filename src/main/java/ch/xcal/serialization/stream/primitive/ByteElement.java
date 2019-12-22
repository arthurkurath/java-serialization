package ch.xcal.serialization.stream.primitive;

import ch.xcal.serialization.stream.IPrimitiveElement;

public class ByteElement implements IPrimitiveElement {
	private final byte value;

	private ByteElement(final byte value) {
		this.value = value;
	}

	public static ByteElement create(final byte value) {
		return new ByteElement(value);
	}

	public byte getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "ByteContent [value=" + value + "]";
	}
}
