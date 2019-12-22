package ch.xcal.serialization.stream.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.stream.IPrimitiveElementOrHandle;
import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public class ObjectElement implements IReferencedStreamElement {

	private final ClassDescHandle classDesc;
	private final List<IPrimitiveElementOrHandle> fields;
	private final List<IRootStreamElement> annotations;
	private final ObjectElement superClassObject;

	private ObjectElement(final ClassDescHandle classDesc, final List<IPrimitiveElementOrHandle> fields,
			final List<IRootStreamElement> annotations,
			final ObjectElement superClassObject) {
		this.classDesc = Objects.requireNonNull(classDesc);
		this.fields = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(fields)));
		this.annotations = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(annotations)));
		this.superClassObject = superClassObject;
	}

	public static ObjectElement createWithAnnotationsOnly(final ClassDescHandle classDesc, final List<IRootStreamElement> annotations,
			final ObjectElement superClassObject) {
		return new ObjectElement(classDesc, Collections.emptyList(), annotations, superClassObject);
	}

	public static ObjectElement create(final ClassDescHandle classDesc, final List<IPrimitiveElementOrHandle> fields,
			final List<IRootStreamElement> annotations, final ObjectElement superClassObject) {
		return new ObjectElement(classDesc, fields, annotations, superClassObject);
	}

	public ClassDescHandle getClassDesc() {
		return classDesc;
	}

	public List<IPrimitiveElementOrHandle> getFields() {
		return fields;
	}

	public List<IRootStreamElement> getAnnotations() {
		return annotations;
	}

	public ObjectElement getSuperClassObject() {
		return superClassObject;
	}

	@Override
	public String toString() {
		return "ObjectContent [classDesc=" + classDesc + ", fields=" + fields + ", annotations=" + annotations + ", superClassObject="
				+ superClassObject + "]";
	}
}
