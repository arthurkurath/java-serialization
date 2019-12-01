package ch.xcal.serialization.parser.content.impl;

import java.util.Arrays;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IPrimitiveContent;

public class BlockDataContent implements IPrimitiveContent {

	private final byte[] value;

	private BlockDataContent(final byte[] value) {
		this.value = Objects.requireNonNull(value);
	}

	public static BlockDataContent create(final byte[] value) {
		return new BlockDataContent(value);
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "BlockDataContent [value=" + Arrays.toString(value) + "]";
	}
}
