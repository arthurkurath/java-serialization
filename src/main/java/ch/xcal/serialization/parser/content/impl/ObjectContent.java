package ch.xcal.serialization.parser.content.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IClassDesc;
import ch.xcal.serialization.parser.content.IContent;

public class ObjectContent implements IContent {

	private final IClassDesc classDesc;
	private final List<IContent> fields;
	private final List<IContent> annotations;
	private final ObjectContent superClassObject;

	private ObjectContent(final IClassDesc classDesc, final List<IContent> fields, final List<IContent> annotations,
			final ObjectContent superClassObject) {
		this.classDesc = Objects.requireNonNull(classDesc);
		this.fields = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(fields)));
		this.annotations = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(annotations)));
		this.superClassObject = superClassObject;
	}

	public static ObjectContent createWithAnnotationsOnly(final IClassDesc classDesc, final List<IContent> annotations,
			final ObjectContent superClassObject) {
		return new ObjectContent(classDesc, Collections.emptyList(), annotations, superClassObject);
	}

	public static ObjectContent create(final IClassDesc classDesc, final List<IContent> fields, final List<IContent> annotations,
			final ObjectContent superClassObject) {
		return new ObjectContent(classDesc, fields, annotations, superClassObject);
	}

	public IClassDesc getClassDesc() {
		return classDesc;
	}

	public List<IContent> getFields() {
		return fields;
	}

	public List<IContent> getAnnotations() {
		return annotations;
	}

	public ObjectContent getSuperClassObject() {
		return superClassObject;
	}

	@Override
	public String toString() {
		return "ObjectContent [classDesc=" + classDesc + ", fields=" + fields + ", annotations=" + annotations + ", superClassObject="
				+ superClassObject + "]";
	}
}
