package ch.xcal.serialization.stream.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.stream.IPrimitiveElementOrHandle;
import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public class ArrayElement implements IReferencedStreamElement {

	private final ClassDescHandle classDescHandle;
	private final List<IPrimitiveElementOrHandle> elements;

	private ArrayElement(final ClassDescHandle classDescHandle, final List<IPrimitiveElementOrHandle> elements) {
		this.classDescHandle = Objects.requireNonNull(classDescHandle);
		this.elements = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(elements)));
	}

	public static ArrayElement create(final ClassDescHandle type, final List<IPrimitiveElementOrHandle> elements) {
		return new ArrayElement(type, elements);
	}

	public ClassDescHandle getClassDescHandle() {
		return classDescHandle;
	}

	public List<IPrimitiveElementOrHandle> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return "ArrayContent [classDescHandle=" + classDescHandle + ", elements=" + elements + "]";
	}
}
