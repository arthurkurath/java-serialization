package ch.xcal.serialization.stream.ref;

import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.descs.IFieldDesc;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public class ProxyClassDescElement implements IClassDescElement {

	private final List<String> interfaces;
	private final List<IRootStreamElement> annotations;
	private final ClassDescHandle superClassHandle;

	private ProxyClassDescElement(final List<String> interfaces, final List<IRootStreamElement> annotations,
			final ClassDescHandle superClassHandle) {
		this.interfaces = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(interfaces)));
		this.annotations = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(annotations)));
		this.superClassHandle = superClassHandle;
	}

	public static ProxyClassDescElement create(final List<String> interfaces, final List<IRootStreamElement> annotations,
			final ClassDescHandle superClassHandle) {
		return new ProxyClassDescElement(interfaces, annotations, superClassHandle);
	}

	public List<String> getInterfaces() {
		return interfaces;
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
	public long getSerialVersionUID() {
		return 0l;
	}

	@Override
	public byte getFlags() {
		return ObjectStreamConstants.SC_SERIALIZABLE;
	}

	@Override
	public List<IFieldDesc> getFields() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "ProxyClassDescElement [interfaces=" + interfaces + ", annotations=" + annotations + ", superClassHandle=" + superClassHandle
				+ "]";
	}
}
