package ch.xcal.serialization.stream.root;

import java.util.Objects;

import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.root.handle.ObjectHandle;

public class ExceptionMarker implements IRootStreamElement {

	private final ObjectHandle throwableHandle;

	private ExceptionMarker(final ObjectHandle throwableHandle) {
		this.throwableHandle = Objects.requireNonNull(throwableHandle);
	}

	public static ExceptionMarker create(final ObjectHandle throwableHandle) {
		return new ExceptionMarker(throwableHandle);
	}

	public ObjectHandle getThrowableHandle() {
		return throwableHandle;
	}

	@Override
	public String toString() {
		return "ExceptionContent [throwableHandle=" + throwableHandle + "]";
	}
}
