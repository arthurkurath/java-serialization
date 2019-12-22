package ch.xcal.serialization.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SerializationStream {

	public static final short MAGIC_NUMBER = (short) 0xACED;
	public static final short STREAM_VERSION = (short) 5;

	private final Map<IHandle<?>, IReferencedStreamElement> handleMap;
	private final List<IRootStreamElement> contents;

	private SerializationStream(final Map<IHandle<?>, IReferencedStreamElement> handleMap, final List<IRootStreamElement> contents) {
		this.handleMap = Collections.unmodifiableMap(new IdentityHashMap<>(Objects.requireNonNull(handleMap)));
		this.contents = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(contents)));
	}

	public static SerializationStream create(final Map<IHandle<?>, IReferencedStreamElement> handleMap,
			final List<IRootStreamElement> contents) {
		return new SerializationStream(handleMap, contents);
	}

	public <REF extends IReferencedStreamElement> REF resolveHandle(final IHandle<REF> handle) {
		return handle.getReferencedStreamElementClass().cast(handleMap.get(handle));
	}

	public List<IRootStreamElement> getRootElements() {
		return contents;
	}
}
