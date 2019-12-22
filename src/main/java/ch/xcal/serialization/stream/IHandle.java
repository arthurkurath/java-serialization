package ch.xcal.serialization.stream;

public interface IHandle<REF extends IReferencedStreamElement> extends IRootStreamElement, IPrimitiveElementOrHandle {

	Class<REF> getReferencedStreamElementClass();
}
