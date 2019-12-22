package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.stream.IStreamElement;
import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.root.handle.StringHandle;

public class ParserNewStringTest {

	@Test
	public void testSimpleString() throws IOException {
		String simpleString = "abcdefg";
		assertStringResult(simpleString, parseSingleString(simpleString));

		simpleString = "yä®€ \u1D11";
		assertStringResult(simpleString, parseSingleString(simpleString));
	}

	@Test
	public void testLongString() throws IOException {
		int length = 2 * 65536;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append("a");
		}
		String testString = sb.toString();
		assertStringResult(testString, parseSingleString(testString));
	}

	private void assertStringResult(final String expectedString, final SerializationStream parserResult) {
		assertEquals(1, parserResult.getRootElements().size());
		final IStreamElement content = parserResult.getRootElements().get(0);
		assertTrue(content instanceof StringHandle);
		assertEquals(expectedString, parserResult.resolveHandle((StringHandle) content).getValue());
	}

	private SerializationStream parseSingleString(String string) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(string);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}
}
