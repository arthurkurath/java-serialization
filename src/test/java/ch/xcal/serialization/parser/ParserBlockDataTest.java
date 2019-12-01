package ch.xcal.serialization.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Test;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.BlockDataContent;
import ch.xcal.serialization.parser.handle.impl.ClassDescHandle;

public class ParserBlockDataTest {

	@Test
	public void testParseBlockData() throws IOException {
		final byte[] shortArray = new byte[255];
		for (int i = 0; i < shortArray.length; i++) {
			shortArray[i] = (byte) i;
		}
		final byte[] longArray = new byte[10000];
		for (int i = 0; i < longArray.length; i++) {
			longArray[i] = (byte) i;
		}

		final ParserResult result = parseBlockData(shortArray, longArray);
		final List<IContent> contents = result.getContents();
		assertTrue(contents.size() > 5);

		// shortArray
		int i = 0;
		assertTrue(contents.get(i) instanceof BlockDataContent);
		assertArrayEquals(shortArray, ((BlockDataContent) contents.get(i)).getValue());
		i++;

		// Object.class
		assertTrue(contents.get(i) instanceof ClassDescHandle);
		i++;

		// longArray
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(10000);
		for (; i < contents.size(); i++) {
			if (contents.get(i) instanceof BlockDataContent) {
				byteOutput.write(((BlockDataContent) contents.get(i)).getValue());
			} else {
				break;
			}
		}
		assertArrayEquals(longArray, byteOutput.toByteArray());

		// Object.class
		assertTrue(contents.get(i) instanceof ClassDescHandle);
		i++;

		// 10
		assertTrue(contents.get(i) instanceof BlockDataContent);
		assertArrayEquals(new byte[]{0, 0, 0, 10}, ((BlockDataContent) contents.get(i)).getValue());

		i++;
		assertEquals(i, contents.size());
	}

	private ParserResult parseBlockData(byte[] value1, byte[] value2) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.write(value1);
		out.writeObject(Object.class);
		out.write(value2);
		out.writeObject(Object.class);
		out.writeInt(10);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}
}
