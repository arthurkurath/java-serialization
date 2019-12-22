package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.ObjectElement;

public class ObjectHandle extends AbstractHandle<ObjectElement> {

	@Override
	public Class<ObjectElement> getReferencedStreamElementClass() {
		return ObjectElement.class;
	}
}
