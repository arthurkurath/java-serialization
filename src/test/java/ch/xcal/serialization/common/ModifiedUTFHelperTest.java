package ch.xcal.serialization.common;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.junit.Test;

public class ModifiedUTFHelperTest {

	@Test
	public void testByteLength() {
		assertEquals(2, ModifiedUTFHelper.getUTFByteLength(String.valueOf('\u0000')));
		assertEquals(1, ModifiedUTFHelper.getUTFByteLength("a"));
		assertEquals(2, ModifiedUTFHelper.getUTFByteLength("ä"));
		assertEquals(3, ModifiedUTFHelper.getUTFByteLength("€"));
	}

	@Test
	public void testWriteRead() throws IOException {
		final StringBuilder sb = new StringBuilder(0x0000FFFF);
		for (int i = 0; i < 0x0000FFFF; i++) {
			sb.append((char) i);
		}
		assertWriteAndRead(sb.toString());
	}

	@Test
	public void testLongerThanBufferString() throws IOException {
		// String which is longer than BufferSize
		// we build a string with 1, 2 and 3 byte UTF-8 characters and check whether the buffer works as intended

		// single byte
		// -----------
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ModifiedUTFHelper.MAX_BUFFER_SIZE + 10; i++) {
			sb.append("a");
		}
		String testString = sb.toString();
		assertWriteAndRead(testString);

		// two bytes
		// ---------
		sb = new StringBuilder();
		for (int i = 0; i < (ModifiedUTFHelper.MAX_BUFFER_SIZE + 10) / 2; i++) {
			sb.append("ä");
		}
		testString = sb.toString();
		assertWriteAndRead(testString);
		// offset 1
		testString = "a" + testString;
		assertWriteAndRead(testString);

		// three bytes
		// -----------
		sb = new StringBuilder();
		for (int i = 0; i < (ModifiedUTFHelper.MAX_BUFFER_SIZE + 10) / 3; i++) {
			sb.append("€");
		}
		testString = sb.toString();
		assertWriteAndRead(testString);
		// offset 1
		testString = "a" + testString;
		assertWriteAndRead(testString);
		// offset 2
		testString = "aa" + testString;
		assertWriteAndRead(testString);
	}

	private void assertWriteAndRead(final String str) throws IOException {
		final int byteLength = ModifiedUTFHelper.getUTFByteLength(str);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteLength);
		ModifiedUTFHelper.writeUTF(byteArrayOutputStream, str, byteLength);
		final byte[] writtenBytes = byteArrayOutputStream.toByteArray();
		assertEquals(byteLength, writtenBytes.length);
		assertEquals(str, ModifiedUTFHelper.readUTF(new DataInputStream(new ByteArrayInputStream(writtenBytes)), byteLength));
	}
}
