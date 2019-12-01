package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class FloatContent implements IPrimitiveContent {
	private final float value;

	private FloatContent(final float value) {
		this.value = value;
	}

	public static FloatContent create(final float value) {
		return new FloatContent(value);
	}

	public float getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "FloatContent [value=" + value + "]";
	}
}
