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

import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.root.BlockDataElement;
import ch.xcal.serialization.stream.root.handle.ClassHandle;

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

		final SerializationStream result = parseBlockData(shortArray, longArray);
		final List<IRootStreamElement> contents = result.getRootElements();
		assertTrue(contents.size() > 5);

		// shortArray
		int i = 0;
		assertTrue(contents.get(i) instanceof BlockDataElement);
		assertArrayEquals(shortArray, ((BlockDataElement) contents.get(i)).getValue());
		i++;

		// Object.class
		assertTrue(contents.get(i) instanceof ClassHandle);
		i++;

		// longArray
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(10000);
		for (; i < contents.size(); i++) {
			if (contents.get(i) instanceof BlockDataElement) {
				byteOutput.write(((BlockDataElement) contents.get(i)).getValue());
			} else {
				break;
			}
		}
		assertArrayEquals(longArray, byteOutput.toByteArray());

		// Object.class
		assertTrue(contents.get(i) instanceof ClassHandle);
		i++;

		// 10
		assertTrue(contents.get(i) instanceof BlockDataElement);
		assertArrayEquals(new byte[]{0, 0, 0, 10}, ((BlockDataElement) contents.get(i)).getValue());

		i++;
		assertEquals(i, contents.size());
	}

	@Test
	public void testPrimitiveTypes() throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeBoolean(true);
		out.writeByte(0x3);
		out.writeShort(10);
		out.writeUTF("xyz");
		out.flush();
		final byte[] result = o.toByteArray();
		final SerializationStream stream = Parser.parse(new ByteArrayInputStream(result));

		assertEquals(1, stream.getRootElements().size());
		final BlockDataElement blockContent = (BlockDataElement) stream.getRootElements().get(0);
		assertArrayEquals(new byte[]{
				0x1,
				0x3,
				0x0, 0xa,
				0x0, 0x3, (byte) 120, (byte) 121, (byte) 122
		}, blockContent.getValue());
	}

	private SerializationStream parseBlockData(byte[] value1, byte[] value2) throws IOException {
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
