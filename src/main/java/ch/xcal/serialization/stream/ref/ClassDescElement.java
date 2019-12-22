package ch.xcal.serialization.stream.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.descs.IFieldDesc;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public class ClassDescElement implements IClassDescElement {

	private final String name;
	private final long serialVersionUID;
	private final byte flags;
	private final List<IFieldDesc> fields;
	private final List<IRootStreamElement> annotations;
	private final ClassDescHandle superClassHandle;

	private ClassDescElement(final String name, final long serialVersionUID, final byte flags, final List<IFieldDesc> fields,
			final List<IRootStreamElement> annotations, final ClassDescHandle superClassHandle) {

		this.name = Objects.requireNonNull(name);
		this.serialVersionUID = serialVersionUID;
		this.flags = flags;
		this.fields = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(fields)));
		this.annotations = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(annotations)));
		this.superClassHandle = superClassHandle;
	}

	public static ClassDescElement create(final String name, final long serialVersionUID, final byte flags,
			final List<IFieldDesc> fields, final List<IRootStreamElement> annotations, final ClassDescHandle superClassHandle) {
		return new ClassDescElement(name, serialVersionUID, flags, fields, annotations, superClassHandle);
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
	public List<IFieldDesc> getFields() {
		return fields;
	}

	@Override
	public List<IRootStreamElement> getAnnotations() {
		return annotations;
	}

	@Override
	public ClassDescHandle getSuperClassHandle() {
		return superClassHandle;
	}

	@Override
	public String toString() {
		return "ClassDescElement [name=" + name + ", serialVersionUID=" + serialVersionUID + ", flags=" + flags + ", fields=" + fields
				+ ", annotations=" + annotations + ", superClassHandle=" + superClassHandle + "]";
	}
}
