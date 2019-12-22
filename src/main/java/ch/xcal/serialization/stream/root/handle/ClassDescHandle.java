package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.IClassDescElement;

public class ClassDescHandle extends AbstractHandle<IClassDescElement> {

	public static final ClassDescHandle NULL_CLASS_DESC_INSTANCE = new ClassDescHandle();

	@Override
	public Class<IClassDescElement> getReferencedStreamElementClass() {
		return IClassDescElement.class;
	}
}
