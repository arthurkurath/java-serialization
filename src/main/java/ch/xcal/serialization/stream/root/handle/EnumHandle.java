package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.EnumElement;

public class EnumHandle extends AbstractHandle<EnumElement> {

	@Override
	public Class<EnumElement> getReferencedStreamElementClass() {
		return EnumElement.class;
	}
}
