package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.ClassElement;

public class ClassHandle extends AbstractHandle<ClassElement> {

	@Override
	public Class<ClassElement> getReferencedStreamElementClass() {
		return ClassElement.class;
	}
}
