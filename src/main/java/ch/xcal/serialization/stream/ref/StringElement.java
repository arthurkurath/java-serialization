package ch.xcal.serialization.stream.ref;

import java.util.Objects;

import ch.xcal.serialization.stream.IReferencedStreamElement;

public class StringElement implements IReferencedStreamElement {

	private final String value;

	private StringElement(final String value) {
		this.value = Objects.requireNonNull(value);
	}

	public static StringElement create(final String value) {
		return new StringElement(value);
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "StringContent [value=" + value + "]";
	}
}
