package ch.xcal.serialization.parser.content.impl;

import java.util.Objects;

public class FieldDesc {

	private final TypeDesc type;
	private final String fieldName;

	private FieldDesc(final TypeDesc type, final String fieldName) {
		this.type = Objects.requireNonNull(type);
		this.fieldName = Objects.requireNonNull(fieldName);
	}

	public static FieldDesc create(final TypeDesc type, final String fieldName) {
		return new FieldDesc(type, fieldName);
	}

	public TypeDesc getType() {
		return type;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String toString() {
		return "FieldDesc [type=" + type + ", fieldName=" + fieldName + "]";
	}
}
