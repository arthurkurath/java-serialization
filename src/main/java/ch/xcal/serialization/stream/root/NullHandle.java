package ch.xcal.serialization.stream.root;

import ch.xcal.serialization.stream.IPrimitiveElementOrHandle;
import ch.xcal.serialization.stream.IRootStreamElement;

public class NullHandle implements IRootStreamElement, IPrimitiveElementOrHandle {
	public static final NullHandle INSTANCE = new NullHandle();

	private NullHandle() {
	}
}
