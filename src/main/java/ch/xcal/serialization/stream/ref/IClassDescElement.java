package ch.xcal.serialization.stream.ref;

import java.util.List;

import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.descs.IFieldDesc;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;

public interface IClassDescElement extends IReferencedStreamElement {

	long getSerialVersionUID();

	byte getFlags();

	List<IFieldDesc> getFields();

	List<IRootStreamElement> getAnnotations();

	ClassDescHandle getSuperClassHandle();
}
