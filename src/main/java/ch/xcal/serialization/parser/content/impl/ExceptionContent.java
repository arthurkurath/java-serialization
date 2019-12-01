package ch.xcal.serialization.parser.content.impl;

import java.util.Objects;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.handle.impl.ObjectHandle;

public class ExceptionContent implements IContent {

	private final ObjectHandle throwableHandle;

	private ExceptionContent(final ObjectHandle throwableHandle) {
		this.throwableHandle = Objects.requireNonNull(throwableHandle);
	}

	public static ExceptionContent create(final ObjectHandle throwableHandle) {
		return new ExceptionContent(throwableHandle);
	}

	public ObjectHandle getThrowableHandle() {
		return throwableHandle;
	}
}
