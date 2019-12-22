package ch.xcal.serialization.stream.root;

import java.util.Arrays;
import java.util.Objects;

import ch.xcal.serialization.stream.IRootStreamElement;

public class BlockDataElement implements IRootStreamElement {

	private final byte[] value;

	private BlockDataElement(final byte[] value) {
		this.value = Objects.requireNonNull(value);
	}

	public static BlockDataElement create(final byte[] value) {
		return new BlockDataElement(value);
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "BlockDataContent [value=" + Arrays.toString(value) + "]";
	}
}
