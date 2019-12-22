package ch.xcal.serialization.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.Objects;

public final class ModifiedUTFHelper {

	static final int MAX_BUFFER_SIZE = 4096;

	private ModifiedUTFHelper() {
	}

	/**
	 * Read modified utf string from the data input where the second argument denotes the number of bytes which should
	 * be interpreted as a string. In contrast to {@link DataInput#readUTF()}, this method can also read long strings
	 * 
	 * @param byteStream
	 *            stream to read data from
	 * @param stringBytes
	 *            number of bytes that should be interpreted as a string
	 * @return resulting string
	 * @throws IOException
	 */
	public static String readUTF(final DataInput byteStream, int stringBytes) throws IOException {
		int processedBytes = 0;
		final byte[] buffer = new byte[Math.min(MAX_BUFFER_SIZE, stringBytes)];
		final char[] result = new char[stringBytes];
		int bufferOffset = 0;
		int bufferCounter;
		int bufferSize;
		int resultCounter = 0;
		int nextChar;
		int nextChar2;
		int nextChar3;

		while (processedBytes < stringBytes) {
			bufferCounter = 0;
			bufferSize = Math.min(buffer.length - bufferOffset, stringBytes - processedBytes - bufferOffset);
			byteStream.readFully(buffer, bufferOffset, bufferSize);
			if (bufferOffset > 0) {
				bufferSize = bufferSize + bufferOffset;
				bufferOffset = 0;
			}

			processBytes : while (bufferCounter < bufferSize) {
				nextChar = (int) buffer[bufferCounter] & 0xFF;
				bufferCounter++;
				if (nextChar <= 127) {
					result[resultCounter] = (char) nextChar;
					resultCounter++;
					processedBytes++;
				} else {
					switch (nextChar >> 4) {
						case 12 :
						case 13 :
							// two byte unicode (110x xxxx 10xx xxxx)
							if (processedBytes + 1 >= stringBytes) {
								throw new UTFDataFormatException("Partial character at the end");
							} else if (bufferCounter >= bufferSize) {
								buffer[0] = buffer[bufferSize - 1];
								bufferOffset = 1;
								break processBytes;
							}
							nextChar2 = (int) buffer[bufferCounter] & 0xFF;
							bufferCounter++;
							processedBytes += 2;
							if ((nextChar2 & 0xC0) != 0x80) {
								throw new UTFDataFormatException("Illegal partial character at offset " + processedBytes);
							}
							result[resultCounter] = (char) (((nextChar & 0x1F) << 6) | (nextChar2 & 0x3F));
							resultCounter++;
							break;

						case 14 :
							// three byte unicode (1110 xxxx 10xx xxxx 10xx xxxx)
							if (processedBytes + 2 >= stringBytes) {
								throw new UTFDataFormatException("Partial character at the end");
							} else if (bufferCounter >= bufferSize) {
								buffer[0] = buffer[bufferSize - 1];
								bufferOffset = 1;
								break processBytes;
							} else if (bufferCounter + 1 >= bufferSize) {
								buffer[0] = buffer[bufferSize - 2];
								buffer[1] = buffer[bufferSize - 1];
								bufferOffset = 2;
								break processBytes;
							}
							nextChar2 = (int) buffer[bufferCounter] & 0xFF;
							bufferCounter++;
							nextChar3 = (int) buffer[bufferCounter] & 0xFF;
							bufferCounter++;
							processedBytes += 3;
							if ((nextChar2 & 0xC0) != 0x80 || (nextChar3 & 0xC0) != 0x80) {
								throw new UTFDataFormatException("Illegal partial character at offset " + processedBytes);
							}
							result[resultCounter] = (char) (((nextChar & 0x0F) << 12) | ((nextChar2 & 0x3F) << 6) | (nextChar3 & 0x3F));
							resultCounter++;
							break;

						default :
							throw new UTFDataFormatException("Illegal UTF-Sequence at offset " + processedBytes);
					}
				}
			}
		}
		return new String(result, 0, resultCounter);
	}

	/**
	 * Calculates the number of bytes which are necessary for writing str to a byte stream in the modified UTF-8 format
	 * 
	 * @param str
	 * @return
	 */
	public static int getUTFByteLength(final String str) {
		int byteLength = 0;
		int strLength = str.length();
		for (int i = 0; i < strLength; i++) {
			int nextChar = str.charAt(i);
			if (nextChar > 0 && nextChar <= 127) {
				// single length
				byteLength++;
			} else if (nextChar <= 0x07FF) {
				// up to 11 bits -> we need 2 bytes (\u0000 is also included in this category)
				byteLength += 2;
			} else {
				// otherwise 3 bytes are needed
				byteLength += 3;
			}
		}
		return byteLength;
	}

	/**
	 * Writes str to the outputStream in the modified UTF-8 format ({@link DataOutput#writeUTF(String)}. Does not output
	 * any size or other information, i.e. the first byte of str is written as the first byte to the stream
	 * 
	 * @param outputStream
	 *            stream which is used for the output
	 * @param str
	 * @param byteLength
	 *            number of bytes which are necessary, as calculated in {@link #getUTFByteLength(String)}
	 * @throws IOException
	 */
	public static void writeUTF(final OutputStream outputStream, final String str, final int byteLength) throws IOException {
		Objects.requireNonNull(outputStream);
		Objects.requireNonNull(str);
		final int strLength = str.length();
		int bufferIndex = 0;
		final byte[] buffer = new byte[Math.min(MAX_BUFFER_SIZE, byteLength)];
		for (int i = 0; i < strLength; i++) {
			int nextChar = str.charAt(i);
			if (nextChar > 0 && nextChar <= 127) {
				if (bufferIndex == MAX_BUFFER_SIZE) {
					outputStream.write(buffer, 0, bufferIndex);
					bufferIndex = 0;
				}
				buffer[bufferIndex] = (byte) nextChar;
				bufferIndex++;
			} else if (nextChar <= 0x07FF) {
				if (bufferIndex >= MAX_BUFFER_SIZE - 1) {
					outputStream.write(buffer, 0, bufferIndex);
					bufferIndex = 0;
				}
				// 0x110 & bytes 10 - 6
				buffer[bufferIndex] = (byte) (0xC0 | ((nextChar >> 6) & 0x1F));
				bufferIndex++;
				// 0x10 & bytes 5 - 0
				buffer[bufferIndex] = (byte) (0x80 | (nextChar & 0x3F));
				bufferIndex++;
			} else {
				if (bufferIndex >= MAX_BUFFER_SIZE - 2) {
					outputStream.write(buffer, 0, bufferIndex);
					bufferIndex = 0;
				}
				// 0x110 & bytes 15 - 12
				buffer[bufferIndex] = (byte) (0xE0 | ((nextChar >> 12) & 0x0F));
				bufferIndex++;
				// 0x10 & bytes 11 - 6
				buffer[bufferIndex] = (byte) (0x80 | ((nextChar >> 6) & 0x3F));
				bufferIndex++;
				// 0x10 & bytes 5 - 0
				buffer[bufferIndex] = (byte) (0x80 | (nextChar & 0x3F));
				bufferIndex++;
			}
		}
		outputStream.write(buffer, 0, bufferIndex);
	}
}
