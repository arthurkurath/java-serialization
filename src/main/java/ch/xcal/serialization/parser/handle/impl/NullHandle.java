package ch.xcal.serialization.parser.handle.impl;

import ch.xcal.serialization.parser.handle.IClassDescHandle;
import ch.xcal.serialization.parser.handle.IObjectHandle;

public class NullHandle extends AbstractHandle implements IClassDescHandle, IObjectHandle {
	public static final NullHandle INSTANCE = new NullHandle();

	private NullHandle() {
	}
}
