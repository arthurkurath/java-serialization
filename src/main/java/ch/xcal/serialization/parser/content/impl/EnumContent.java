package ch.xcal.serialization.parser.content.impl;

import java.util.Objects;

import ch.xcal.serialization.parser.content.IContent;

public class EnumContent implements IContent {

	private final ClassDesc classDesc;
	private final String name;

	private EnumContent(final ClassDesc classDesc, final String name) {
		this.classDesc = Objects.requireNonNull(classDesc);
		this.name = Objects.requireNonNull(name);
	}

	public static EnumContent create(final ClassDesc classDesc, final String name) {
		return new EnumContent(classDesc, name);
	}

	public ClassDesc getClassDesc() {
		return classDesc;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "EnumContent [classDesc=" + classDesc + ", name=" + name + "]";
	}
}
