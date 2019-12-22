package ch.xcal.serialization.stream.ref;

import java.util.Objects;

import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public class ClassElement implements IReferencedStreamElement {

	private final ClassDescHandle classDesc;

	private ClassElement(final ClassDescHandle classDesc) {
		this.classDesc = Objects.requireNonNull(classDesc);
	}

	public static ClassElement create(final ClassDescHandle classDesc) {
		return new ClassElement(classDesc);
	}

	public ClassDescHandle getClassDesc() {
		return classDesc;
	}
}
