package ch.xcal.serialization.parser.content.impl;

import ch.xcal.serialization.parser.content.IContent;

public class ResetContent implements IContent {
	public static final ResetContent INSTANCE = new ResetContent();

	private ResetContent() {
	}

	@Override
	public String toString() {
		return "ResetContent []";
	}
}
