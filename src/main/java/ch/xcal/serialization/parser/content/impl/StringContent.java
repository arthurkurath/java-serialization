package ch.xcal.serialization.parser.content.impl;

import java.util.Objects;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class StringContent implements IPrimitiveContent {

	private final String value;

	private StringContent(final String value) {
		this.value = Objects.requireNonNull(value);
	}

	public static StringContent create(final String value) {
		return new StringContent(value);
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "StringContent [value=" + value + "]";
	}
}
