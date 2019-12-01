package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.StringContent;
import ch.xcal.serialization.parser.handle.IObjectHandle;

public class ParserNewStringTest {

	@Test
	public void testSimpleString() throws IOException {
		String simpleString = "abcdefg";
		assertStringResult(simpleString, parseSingleString(simpleString));

		simpleString = "yä®€ \u1D11";
		assertStringResult(simpleString, parseSingleString(simpleString));
	}

	@Test
	public void testLongerThanBufferString() throws IOException {
		// String which is longer than BufferSize
		// we build a string with 1, 2 and 3 byte UTF-8 characters and check whether the buffer works as intended

		// single byte
		// -----------
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Parser.READ_UTF_STRING_BUFFER_SIZE + 10; i++) {
			sb.append("a");
		}
		String testString = sb.toString();
		assertStringResult(testString, parseSingleString(testString));

		// two bytes
		// ---------
		sb = new StringBuilder();
		for (int i = 0; i < (Parser.READ_UTF_STRING_BUFFER_SIZE + 10) / 2; i++) {
			sb.append("ä");
		}
		testString = sb.toString();
		assertStringResult(testString, parseSingleString(testString));
		// offset 1
		testString = "a" + testString;
		assertStringResult(testString, parseSingleString(testString));

		// three bytes
		// -----------
		sb = new StringBuilder();
		for (int i = 0; i < (Parser.READ_UTF_STRING_BUFFER_SIZE + 10) / 3; i++) {
			sb.append("€");
		}
		testString = sb.toString();
		assertStringResult(testString, parseSingleString(testString));
		// offset 1
		testString = "a" + testString;
		assertStringResult(testString, parseSingleString(testString));
		// offset 2
		testString = "aa" + testString;
		assertStringResult(testString, parseSingleString(testString));
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

	private void assertStringResult(final String expectedString, final ParserResult parserResult) {
		assertEquals(1, parserResult.getContents().size());
		final IContent content = parserResult.getContents().get(0);
		assertTrue(content instanceof IObjectHandle);
		final IContent resolvedContent = parserResult.getContent((IObjectHandle) content);
		assertTrue(resolvedContent instanceof StringContent);
		assertEquals(expectedString, ((StringContent) resolvedContent).getValue());
	}

	private ParserResult parseSingleString(String string) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(string);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}
}
