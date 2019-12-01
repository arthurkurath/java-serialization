package ch.xcal.serialization.parser.content.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IClassDesc;
import ch.xcal.serialization.parser.content.IContent;

public class ClassDesc implements IClassDesc {

	private final String name;
	private final long serialVersionUID;
	private final byte flags;
	private final List<FieldDesc> fields;
	private final List<IContent> annotations;
	private final List<IClassDesc> classHierarchy;

	private ClassDesc(final String name, final long serialVersionUID, final byte flags, final List<FieldDesc> fields,
			final List<IContent> annotations, final IClassDesc superClass) {

		this.name = Objects.requireNonNull(name);
		this.serialVersionUID = serialVersionUID;
		this.flags = flags;
		this.fields = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(fields)));
		this.annotations = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(annotations)));
		if (superClass != null) {
			final List<IClassDesc> classHierarchy = new ArrayList<>(superClass.getClassHierarchy().size() + 1);
			classHierarchy.add(this);
			classHierarchy.addAll(superClass.getClassHierarchy());
			this.classHierarchy = classHierarchy;
		} else {
			this.classHierarchy = Collections.singletonList(this);
		}
	}

	public static ClassDesc create(final String name, final long serialVersionUID, final byte flags, final List<FieldDesc> fields,
			final List<IContent> annotations, final IClassDesc superClass) {
		return new ClassDesc(name, serialVersionUID, flags, fields, annotations, superClass);
	}

	public String getName() {
		return name;
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public byte getFlags() {
		return flags;
	}

	@Override
	public List<FieldDesc> getFields() {
		return fields;
	}

	@Override
	public List<IContent> getAnnotations() {
		return annotations;
	}

	@Override
	public List<IClassDesc> getClassHierarchy() {
		return classHierarchy;
	}

	@Override
	public String toString() {
		return "ClassDesc [name=" + name + ", serialVersionUID=" + serialVersionUID + ", flags=" + flags + ", fields=" + fields
				+ ", annotations=" + annotations + ", superClass=" + (hasSuperClass() ? classHierarchy.get(1) : null) + "]";
	}
}
