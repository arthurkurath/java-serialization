package ch.xcal.serialization.stream.root.handle;

import ch.xcal.serialization.stream.ref.ArrayElement;

public class ArrayHandle extends AbstractHandle<ArrayElement> {

	@Override
	public Class<ArrayElement> getReferencedStreamElementClass() {
		return ArrayElement.class;
	}
}
