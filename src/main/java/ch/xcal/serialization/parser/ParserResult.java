package ch.xcal.serialization.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.handle.IHandle;

public class ParserResult {

	private final Map<IHandle, IContent> handleMap;
	private final List<IContent> contents;

	private ParserResult(final Map<IHandle, IContent> handleMap, final List<IContent> contents) {
		this.handleMap = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(handleMap)));
		this.contents = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(contents)));
	}

	public static ParserResult create(final Map<IHandle, IContent> handleMap, final List<IContent> contents) {
		return new ParserResult(handleMap, contents);
	}

	public IContent getContent(final IHandle handle) {
		return handleMap.get(handle);
	}

	public List<IContent> getContents() {
		return contents;
	}
}
