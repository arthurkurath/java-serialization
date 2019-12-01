package ch.xcal.serialization.parser.content.impl;

import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IClassDesc;
import ch.xcal.serialization.parser.content.IContent;

public class ProxyClassDesc implements IClassDesc {

	private final List<String> interfaces;
	private final List<IContent> annotations;
	private final List<IClassDesc> classHierarchy;

	private ProxyClassDesc(final List<String> interfaces, final List<IContent> annotations, final IClassDesc superClass) {
		this.interfaces = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(interfaces)));
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

	public static ProxyClassDesc create(final List<String> interfaces, final List<IContent> annotations, final IClassDesc superClass) {
		return new ProxyClassDesc(interfaces, annotations, superClass);
	}

	public List<String> getInterfaces() {
		return interfaces;
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
	public long getSerialVersionUID() {
		return 0l;
	}

	@Override
	public byte getFlags() {
		return ObjectStreamConstants.SC_SERIALIZABLE;
	}

	@Override
	public List<FieldDesc> getFields() {
		return Collections.emptyList();
	}
}
