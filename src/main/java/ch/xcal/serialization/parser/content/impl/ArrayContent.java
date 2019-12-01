package ch.xcal.serialization.parser.content.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.TypeDesc.ArrayTypeDesc;

public class ArrayContent implements IContent {

	private final ArrayTypeDesc typeDesc;
	private final List<IContent> elements;

	private ArrayContent(final ArrayTypeDesc typeDesc, final List<IContent> elements) {
		this.typeDesc = Objects.requireNonNull(typeDesc);
		this.elements = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(elements)));
	}

	public static ArrayContent create(final ArrayTypeDesc typeDesc, final List<IContent> elements) {
		return new ArrayContent(typeDesc, elements);
	}

	public ArrayTypeDesc getTypeDesc() {
		return typeDesc;
	}

	public List<IContent> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return "ArrayContent [typeDesc=" + typeDesc + ", elements=" + elements + "]";
	}
}
