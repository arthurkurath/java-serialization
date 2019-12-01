package ch.xcal.serialization.parser.content;

import java.util.List;

import ch.xcal.serialization.parser.content.impl.FieldDesc;

public interface IClassDesc extends IContent {

	long getSerialVersionUID();

	byte getFlags();

	List<FieldDesc> getFields();

	List<IContent> getAnnotations();

	List<IClassDesc> getClassHierarchy();

	default boolean hasSuperClass() {
		return getClassHierarchy().size() > 1;
	}
}
