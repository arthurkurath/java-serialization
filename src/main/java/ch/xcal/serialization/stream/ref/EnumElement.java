package ch.xcal.serialization.stream.ref;

import java.util.Objects;

import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;
import ch.xcal.serialization.stream.root.handle.StringHandle;

public class EnumElement implements IReferencedStreamElement {

	private final ClassDescHandle classDesc;
	private final StringHandle name;

	private EnumElement(final ClassDescHandle classDesc, final StringHandle name) {
		this.classDesc = Objects.requireNonNull(classDesc);
		this.name = Objects.requireNonNull(name);
	}

	public static EnumElement create(final ClassDescHandle classDesc, final StringHandle name) {
		return new EnumElement(classDesc, name);
	}

	public ClassDescHandle getClassDesc() {
		return classDesc;
	}

	public StringHandle getName() {
		return name;
	}

	@Override
	public String toString() {
		return "EnumContent [classDesc=" + classDesc + ", name=" + name + "]";
	}
}
