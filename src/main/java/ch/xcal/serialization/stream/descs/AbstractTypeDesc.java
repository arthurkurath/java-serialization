package ch.xcal.serialization.stream.descs;

import java.util.Objects;

public abstract class AbstractTypeDesc implements ITypeDesc {

	public static AbstractTypeDesc parse(final String type) {
		Objects.requireNonNull(type);
		int dimensions = 0;
		int index = 0;
		while (index < type.length()) {
			if (type.charAt(index) != '[') {
				break;
			}
			dimensions++;
			index++;
		}
		final String componentType = type.substring(index);
		final AbstractTypeDesc componentTypeDesc;
		switch (componentType) {
			case "B" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.BYTE;
				break;
			case "C" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.CHAR;
				break;
			case "D" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.DOUBLE;
				break;
			case "F" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.FLOAT;
				break;
			case "I" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.INTEGER;
				break;
			case "J" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.LONG;
				break;
			case "S" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.SHORT;
				break;
			case "Z" :
				componentTypeDesc = AbstractTypeDesc.PrimitiveTypeDesc.BOOLEAN;
				break;
			default :
				if (!componentType.startsWith("L") || !componentType.endsWith(";")) {
					// should be an object
					throw new IllegalStateException("unknown field desc " + componentType);
				}
				componentTypeDesc = AbstractTypeDesc.ObjectTypeDesc
						.create(componentType.substring(1, componentType.length() - 1).replace("/", "."));
		}

		return dimensions > 0 ? AbstractTypeDesc.ArrayTypeDesc.create(dimensions, componentTypeDesc) : componentTypeDesc;
	}

	public static class PrimitiveTypeDesc extends AbstractTypeDesc {

		private final String name;

		private PrimitiveTypeDesc(final String name) {
			this.name = Objects.requireNonNull(name);
		}

		public static final PrimitiveTypeDesc BYTE = new PrimitiveTypeDesc("byte");
		public static final PrimitiveTypeDesc CHAR = new PrimitiveTypeDesc("char");
		public static final PrimitiveTypeDesc DOUBLE = new PrimitiveTypeDesc("double");
		public static final PrimitiveTypeDesc FLOAT = new PrimitiveTypeDesc("float");
		public static final PrimitiveTypeDesc INTEGER = new PrimitiveTypeDesc("integer");
		public static final PrimitiveTypeDesc LONG = new PrimitiveTypeDesc("long");
		public static final PrimitiveTypeDesc SHORT = new PrimitiveTypeDesc("short");
		public static final PrimitiveTypeDesc BOOLEAN = new PrimitiveTypeDesc("boolean");

		@Override
		public String getTypeName() {
			return name;
		}

		@Override
		public String toString() {
			return "PrimitiveTypeDesc [name=" + name + "]";
		}
	}

	public static class ObjectTypeDesc extends AbstractTypeDesc {

		private final String name;

		private ObjectTypeDesc(final String name) {
			this.name = Objects.requireNonNull(name);
		}

		public static ObjectTypeDesc create(final String name) {
			return new ObjectTypeDesc(name);
		}

		@Override
		public String getTypeName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ObjectTypeDesc other = (ObjectTypeDesc) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ObjectTypeDesc [name=" + name + "]";
		}
	}

	public static class ArrayTypeDesc extends AbstractTypeDesc {
		private final int dimensions;
		private final AbstractTypeDesc componentType;

		private ArrayTypeDesc(final int dimensions, final AbstractTypeDesc componentType) {
			if (dimensions <= 0 || componentType == null || componentType instanceof ArrayTypeDesc) {
				throw new IllegalArgumentException();
			}
			this.dimensions = dimensions;
			this.componentType = componentType;
		}

		public static ArrayTypeDesc create(final int dimensions, final AbstractTypeDesc componentType) {
			return new ArrayTypeDesc(dimensions, componentType);
		}

		public int getDimensions() {
			return dimensions;
		}

		public AbstractTypeDesc getComponentType() {
			return componentType;
		}

		@Override
		public String getTypeName() {
			final StringBuilder sb = new StringBuilder(componentType.getTypeName());
			for (int i = 0; i < dimensions; i++) {
				sb.append("[");
			}
			for (int i = 0; i < dimensions; i++) {
				sb.append("]");
			}
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
			result = prime * result + dimensions;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArrayTypeDesc other = (ArrayTypeDesc) obj;
			if (componentType == null) {
				if (other.componentType != null)
					return false;
			} else if (!componentType.equals(other.componentType))
				return false;
			if (dimensions != other.dimensions)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ArrayTypeDesc [dimensions=" + dimensions + ", componentType=" + componentType + "]";
		}
	}
}
