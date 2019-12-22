package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.StringElement;

public class StringHandle extends AbstractHandle<StringElement> {

	@Override
	public Class<StringElement> getReferencedStreamElementClass() {
		return StringElement.class;
	}
}
