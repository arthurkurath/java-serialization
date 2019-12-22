package ch.xcal.serialization.stream.descs;

import java.util.Objects;

import ch.xcal.serialization.stream.descs.AbstractTypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.stream.root.handle.StringHandle;

public abstract class AbstractFieldDesc implements IFieldDesc {

	final String fieldName;

	AbstractFieldDesc(final String fieldName) {
		this.fieldName = Objects.requireNonNull(fieldName);
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	public static class PrimitiveFieldDesc extends AbstractFieldDesc {

		private final PrimitiveTypeDesc type;

		private PrimitiveFieldDesc(final String fieldName, final PrimitiveTypeDesc type) {
			super(fieldName);
			this.type = Objects.requireNonNull(type);
		}

		public static PrimitiveFieldDesc create(final String fieldName, final PrimitiveTypeDesc type) {
			return new PrimitiveFieldDesc(fieldName, type);
		}

		public PrimitiveTypeDesc getType() {
			return type;
		}

		@Override
		public String toString() {
			return "PrimitiveFieldDesc [type=" + type + ", fieldName=" + fieldName + "]";
		}
	}

	public static class ReferenceFieldDesc extends AbstractFieldDesc {

		private final boolean arrayType;
		private final StringHandle typeHandle;

		private ReferenceFieldDesc(final String fieldName, final boolean arrayType, final StringHandle typeHandle) {
			super(fieldName);
			this.arrayType = arrayType;
			this.typeHandle = Objects.requireNonNull(typeHandle);
		}

		public static ReferenceFieldDesc createObject(final String fieldName, final StringHandle typeHandle) {
			return new ReferenceFieldDesc(fieldName, false, typeHandle);
		}

		public static ReferenceFieldDesc createArray(final String fieldName, final StringHandle typeHandle) {
			return new ReferenceFieldDesc(fieldName, true, typeHandle);
		}

		public boolean isArrayType() {
			return arrayType;
		}

		public StringHandle getTypeHandle() {
			return typeHandle;
		}

		@Override
		public String toString() {
			return "ReferenceFieldDesc [arrayType=" + arrayType + ", typeHandle=" + typeHandle + ", fieldName=" + fieldName + "]";
		}
	}
}
