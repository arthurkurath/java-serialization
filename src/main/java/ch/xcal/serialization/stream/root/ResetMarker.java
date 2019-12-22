package ch.xcal.serialization.stream.root;

import ch.xcal.serialization.stream.IRootStreamElement;

public class ResetMarker implements IRootStreamElement {
	public static final ResetMarker INSTANCE = new ResetMarker();

	private ResetMarker() {
	}

	@Override
	public String toString() {
		return "ResetContent []";
	}
}
