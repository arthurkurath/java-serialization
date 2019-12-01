package ch.xcal.serialization.parser;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamConstants;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import ch.xcal.serialization.parser.content.IClassDesc;
import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.ArrayContent;
import ch.xcal.serialization.parser.content.impl.BlockDataContent;
import ch.xcal.serialization.parser.content.impl.BooleanContent;
import ch.xcal.serialization.parser.content.impl.ByteContent;
import ch.xcal.serialization.parser.content.impl.CharContent;
import ch.xcal.serialization.parser.content.impl.ClassDesc;
import ch.xcal.serialization.parser.content.impl.DoubleContent;
import ch.xcal.serialization.parser.content.impl.EnumContent;
import ch.xcal.serialization.parser.content.impl.ExceptionContent;
import ch.xcal.serialization.parser.content.impl.FieldDesc;
import ch.xcal.serialization.parser.content.impl.FloatContent;
import ch.xcal.serialization.parser.content.impl.IntegerContent;
import ch.xcal.serialization.parser.content.impl.LongContent;
import ch.xcal.serialization.parser.content.impl.ObjectContent;
import ch.xcal.serialization.parser.content.impl.ProxyClassDesc;
import ch.xcal.serialization.parser.content.impl.ResetContent;
import ch.xcal.serialization.parser.content.impl.ShortContent;
import ch.xcal.serialization.parser.content.impl.StringContent;
import ch.xcal.serialization.parser.content.impl.TypeDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc.ArrayTypeDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc.ObjectTypeDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.parser.handle.IClassDescHandle;
import ch.xcal.serialization.parser.handle.IHandle;
import ch.xcal.serialization.parser.handle.impl.ArrayHandle;
import ch.xcal.serialization.parser.handle.impl.ClassDescHandle;
import ch.xcal.serialization.parser.handle.impl.EnumHandle;
import ch.xcal.serialization.parser.handle.impl.NullHandle;
import ch.xcal.serialization.parser.handle.impl.ObjectHandle;

public class Parser {
	private final DataInputStream byteStream;
	static final int READ_UTF_STRING_BUFFER_SIZE = 4096;

	public static ParserResult parse(final InputStream inputStream) throws IOException {
		return new Parser(inputStream).parse();
	}

	// stream state
	private int resetCounter = -1;
	private int nextHandle;
	private Map<Integer, Map<Integer, IHandle>> handleMap = new HashMap<>();
	private Map<IHandle, IContent> referenceMap = new HashMap<>();

	// stream values
	private short magicNumber = -1;
	private short streamVersion = -1;
	private List<IContent> contents = new ArrayList<>();

	protected Parser(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream);
		if (inputStream instanceof DataInputStream) {
			byteStream = (DataInputStream) inputStream;
		} else {
			byteStream = new DataInputStream(inputStream);
		}
		resetStream();
	}

	// helpers
	private ResetContent resetStream() {
		resetCounter++;
		handleMap.put(resetCounter, new HashMap<>());
		nextHandle = 0x7E0000;
		return ResetContent.INSTANCE;
	}

	private NullHandle nullReference() {
		return NullHandle.INSTANCE;
	}

	private void putReference(IHandle handle, IContent referencedObject) {
		if (referenceMap.containsKey(handle)) {
			throw new IllegalArgumentException("Handle already exists");
		}
		referenceMap.put(handle, Objects.requireNonNull(referencedObject));
	}

	private IContent resolveReference(IHandle handle) {
		if (!referenceMap.containsKey(handle)) {
			throw new IllegalArgumentException("Unknown handle");
		}
		return referenceMap.get(handle);
	}

	private <T extends IHandle> T newHandle(Class<T> handleClass, IContent referencedObject) {
		T newHandle = newHandle(handleClass);
		putReference(newHandle, referencedObject);
		return newHandle;
	}

	private <T extends IHandle> T newHandle(Class<T> handleClass) {
		final IHandle newHandle;
		if (handleClass == ClassDescHandle.class) {
			newHandle = new ClassDescHandle();
		} else if (handleClass == ObjectHandle.class) {
			newHandle = new ObjectHandle();
		} else if (handleClass == ArrayHandle.class) {
			newHandle = new ArrayHandle();
		} else if (handleClass == EnumHandle.class) {
			newHandle = new EnumHandle();
		} else {
			throw new IllegalArgumentException("unknown handle " + handleClass);
		}
		handleMap.get(resetCounter).put(nextHandle++, newHandle);
		@SuppressWarnings("unchecked")
		T result = (T) newHandle;
		return result;
	}

	private <T extends IHandle> T prevObject(final Class<T> expectedClass) throws IOException {
		final int handleNr = (byteStream.readUnsignedShort() << 16) + byteStream.readUnsignedShort();
		final IHandle handle = handleMap.get(resetCounter).get(handleNr);
		if (handle == null || !expectedClass.isInstance(handle)) {
			throw new IllegalStateException("Handle does not match: " + handleNr);
		}
		@SuppressWarnings("unchecked")
		T result = (T) handle;
		return result;
	}

	public ParserResult parse() throws IOException {
		readMagicNumber();
		readVersion();
		try {
			while (true) {
				try {
					contents.add(readContent(byteStream.readByte()));
				} catch (ParsedSerializedException e) {
					contents.add(e.getExceptionHandle());
				}
			}
		} catch (EOFException e) {
			return ParserResult.create(referenceMap, contents);
		}
	}

	private void readMagicNumber() throws IOException {
		magicNumber = byteStream.readShort();
		if (magicNumber != ((short) 0xACED)) {
			throw new IllegalStateException("magic number");
		}
	}

	private void readVersion() throws IOException {
		streamVersion = byteStream.readShort();
		if (streamVersion != 5) {
			throw new IllegalStateException("stream version");
		}
	}

	private IContent readContent(final byte contentType) throws ParsedSerializedException {
		try {
			if (contentType != ObjectStreamConstants.TC_BLOCKDATA && contentType != ObjectStreamConstants.TC_BLOCKDATALONG) {
				return readObject(contentType);
			} else {
				return readBlockData(contentType);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private IContent readObject(final byte contentType) throws ParsedSerializedException, IOException {
		switch (contentType) {
			case ObjectStreamConstants.TC_OBJECT :
				return readNewObject();
			case ObjectStreamConstants.TC_CLASS :
				return readNewClass();
			case ObjectStreamConstants.TC_ARRAY :
				return readNewArray();
			case ObjectStreamConstants.TC_STRING :
				return newString(byteStream.readUnsignedShort());
			case ObjectStreamConstants.TC_LONGSTRING :
				return newString(byteStream.readLong());
			case ObjectStreamConstants.TC_ENUM :
				return readNewEnum();
			case ObjectStreamConstants.TC_CLASSDESC :
				return readClass();
			case ObjectStreamConstants.TC_PROXYCLASSDESC :
				return readProxyClass();
			case ObjectStreamConstants.TC_REFERENCE :
				return prevObject(IHandle.class);
			case ObjectStreamConstants.TC_NULL :
				return nullReference();
			case ObjectStreamConstants.TC_EXCEPTION :
				return readException();
			case ObjectStreamConstants.TC_RESET :
				return resetStream();
			default :
				throw new IllegalStateException("unknown content type " + contentType);
		}
	}

	private IContent readBlockData(final byte contentType) throws IOException {
		final int length;
		switch (contentType) {
			case ObjectStreamConstants.TC_BLOCKDATA :
				length = byteStream.readUnsignedByte();
				break;
			case ObjectStreamConstants.TC_BLOCKDATALONG :
				length = byteStream.readInt();
				break;
			default :
				throw new IllegalStateException("Not in blockData block");
		}
		final byte[] blockData = new byte[length];
		byteStream.readFully(blockData);
		return BlockDataContent.create(blockData);
	}

	private ObjectHandle readNewObject() throws ParsedSerializedException, IOException {
		final IContent classDesc0 = resolveReference(readClassDesc());
		if (!(classDesc0 instanceof IClassDesc)) {
			throw new IllegalStateException("class desc is not instance of ClassDesc");
		}
		final ObjectHandle handle = newHandle(ObjectHandle.class);
		final List<IClassDesc> classHierarchy = ((ClassDesc) classDesc0).getClassHierarchy();
		// start with superclass
		final ListIterator<IClassDesc> classHierarchyIt = classHierarchy.listIterator(classHierarchy.size());
		ObjectContent lastReadObject = null;
		while (classHierarchyIt.hasPrevious()) {
			final IClassDesc classDesc = classHierarchyIt.previous();
			final byte classDescFlags = classDesc.getFlags();
			final ObjectContent object;
			if ((ObjectStreamConstants.SC_SERIALIZABLE & classDescFlags) > 0x0) {
				final List<IContent> fields = new ArrayList<>(classDesc.getFields().size());
				for (final FieldDesc field : classDesc.getFields()) {
					fields.add(readContent(field.getType()));
				}
				if ((ObjectStreamConstants.SC_WRITE_METHOD & classDescFlags) > 0x0) {
					object = ObjectContent.create(classDesc, fields, readObjectAnnotation(), lastReadObject);
				} else {
					object = ObjectContent.create(classDesc, fields, Collections.emptyList(), lastReadObject);
				}
			} else if ((ObjectStreamConstants.SC_EXTERNALIZABLE & classDescFlags) > 0x0) {
				if ((ObjectStreamConstants.SC_BLOCK_DATA & classDescFlags) > 0x0) {
					object = ObjectContent.createWithAnnotationsOnly(classDesc, readObjectAnnotation(), lastReadObject);
				} else {
					throw new UnsupportedOperationException("Cannot read external contents which were not written in block data mode");
				}
			} else {
				throw new UnsupportedOperationException("Cannot read class with flags " + classDescFlags);
			}
			lastReadObject = object;
		}

		putReference(handle, lastReadObject);
		return handle;
	}

	private IContent readContent(final TypeDesc type) throws ParsedSerializedException, IOException {
		if (PrimitiveTypeDesc.BYTE == type) {
			return ByteContent.create(byteStream.readByte());
		} else if (PrimitiveTypeDesc.CHAR == type) {
			return CharContent.create(byteStream.readChar());
		} else if (PrimitiveTypeDesc.DOUBLE == type) {
			return DoubleContent.create(byteStream.readDouble());
		} else if (PrimitiveTypeDesc.FLOAT == type) {
			return FloatContent.create(byteStream.readFloat());
		} else if (PrimitiveTypeDesc.INTEGER == type) {
			return IntegerContent.create(byteStream.readInt());
		} else if (PrimitiveTypeDesc.LONG == type) {
			return LongContent.create(byteStream.readLong());
		} else if (PrimitiveTypeDesc.SHORT == type) {
			return ShortContent.create(byteStream.readShort());
		} else if (PrimitiveTypeDesc.BOOLEAN == type) {
			return BooleanContent.create(byteStream.readBoolean());
		} else if (type instanceof ObjectTypeDesc || type instanceof ArrayTypeDesc) {
			return readObject(byteStream.readByte());
		} else {
			throw new UnsupportedOperationException("Unknown type " + type);
		}
	}

	private ClassDescHandle readNewClass() throws ParsedSerializedException, IOException {
		return newHandle(ClassDescHandle.class, readClassDesc());
	}

	private ArrayHandle readNewArray() throws ParsedSerializedException, IOException {
		final IClassDescHandle classDesc = readClassDesc();
		final ArrayHandle handle = newHandle(ArrayHandle.class);
		final int size = byteStream.readInt();
		final TypeDesc type0 = TypeDesc.parse(((ClassDesc) resolveReference(classDesc)).getName());
		if (!(type0 instanceof ArrayTypeDesc)) {
			throw new IllegalStateException("Type is not an array");
		}
		final ArrayTypeDesc type = (ArrayTypeDesc) type0;
		final List<IContent> elements = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			elements.add(readContent(type.getComponentType()));
		}
		putReference(handle, ArrayContent.create(type, elements));
		return handle;
	}

	private EnumHandle readNewEnum() throws ParsedSerializedException, IOException {
		final IClassDescHandle classDesc = readClassDesc();
		final EnumHandle handle = newHandle(EnumHandle.class);
		final String name = ((StringContent) resolveReference((ObjectHandle) readObject(byteStream.readByte()))).getValue();
		putReference(handle, EnumContent.create((ClassDesc) resolveReference(classDesc), name));
		return handle;
	}

	private IClassDescHandle readClassDesc() throws ParsedSerializedException, IOException {
		final byte classDescType = byteStream.readByte();
		switch (classDescType) {
			case ObjectStreamConstants.TC_CLASSDESC :
				return readClass();
			case ObjectStreamConstants.TC_PROXYCLASSDESC :
				return readProxyClass();
			case ObjectStreamConstants.TC_NULL :
				return nullReference();
			case ObjectStreamConstants.TC_REFERENCE :
				return prevObject(ClassDescHandle.class);
		}
		throw new IllegalStateException("ClassDescType: " + classDescType);
	}

	private IClassDescHandle readSuperClassDesc() throws ParsedSerializedException, IOException {
		return readClassDesc();
	}

	private ClassDescHandle readClass() throws ParsedSerializedException, IOException {
		final String className = readUtf(byteStream.readUnsignedShort());
		final long serialVersionUID = byteStream.readLong();
		final ClassDescHandle handle = newHandle(ClassDescHandle.class);
		final byte classDescFlags = byteStream.readByte();
		final short numberOfFields = byteStream.readShort();
		final List<FieldDesc> fieldDescs = new ArrayList<>(numberOfFields);
		for (short i = 0; i < numberOfFields; i++) {
			fieldDescs.add(readFieldDesc());
		}
		final List<IContent> classAnnotations = readClassAnnotation();

		final IClassDescHandle superClass = readSuperClassDesc();
		putReference(handle, ClassDesc.create(className, serialVersionUID, classDescFlags, fieldDescs, classAnnotations,
				superClass == NullHandle.INSTANCE ? null : (IClassDesc) resolveReference(superClass)));
		return handle;
	}

	private ClassDescHandle readProxyClass() throws ParsedSerializedException, IOException {
		final ClassDescHandle handle = newHandle(ClassDescHandle.class);
		final int numberOfInterfaces = byteStream.readInt();
		final List<String> interfaces = new ArrayList<>(numberOfInterfaces);
		for (int i = 0; i < numberOfInterfaces; i++) {
			interfaces.add(readUtf(byteStream.readUnsignedShort()));
		}
		final List<IContent> classAnnotations = readClassAnnotation();
		final IClassDescHandle superClass = readSuperClassDesc();
		putReference(handle, ProxyClassDesc.create(interfaces, classAnnotations,
				superClass == NullHandle.INSTANCE ? null : (IClassDesc) resolveReference(superClass)));
		return handle;
	}

	private List<IContent> readClassAnnotation() throws ParsedSerializedException, IOException {
		return readAnnotations();
	}

	private List<IContent> readObjectAnnotation() throws ParsedSerializedException, IOException {
		return readAnnotations();
	}

	private List<IContent> readAnnotations() throws ParsedSerializedException, IOException {
		final List<IContent> annotations = new ArrayList<>();
		byte contentType;
		while ((contentType = byteStream.readByte()) != ObjectStreamConstants.TC_ENDBLOCKDATA) {
			annotations.add(readContent(contentType));
		}
		return annotations;
	}

	private FieldDesc readFieldDesc() throws ParsedSerializedException, IOException {
		final char typeCodeChar = (char) byteStream.readUnsignedByte();
		final String fieldName = readUtf(byteStream.readUnsignedShort());

		final String type;
		if (typeCodeChar == '[' || typeCodeChar == 'L') {
			// read class name descriptor
			type = ((StringContent) resolveReference((ObjectHandle) readObject(byteStream.readByte()))).getValue();

		} else {
			type = Character.toString(typeCodeChar);
		}
		return FieldDesc.create(TypeDesc.parse(type), fieldName);
	}

	private ObjectHandle readException() throws ParsedSerializedException, IOException {
		resetStream();
		final ObjectHandle throwableHandle = (ObjectHandle) readObject(byteStream.readByte());
		resetStream();
		throw new ParsedSerializedException(ExceptionContent.create(throwableHandle));
	}

	private ObjectHandle newString(final long stringBytes) throws IOException {
		if (stringBytes < 0 || stringBytes > Integer.MAX_VALUE) {
			throw new IllegalStateException("String length: " + stringBytes);
		}
		return newHandle(ObjectHandle.class, StringContent.create(readUtf((int) stringBytes)));
	}

	private String readUtf(int stringBytes) throws IOException {
		int processedBytes = 0;
		final byte[] buffer = new byte[Math.min(READ_UTF_STRING_BUFFER_SIZE, stringBytes)];
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

	private static class ParsedSerializedException extends Exception {

		private static final long serialVersionUID = 1L;

		private final ExceptionContent exceptionHandle;

		public ParsedSerializedException(final ExceptionContent handle) {
			this.exceptionHandle = Objects.requireNonNull(handle);
		}

		public ExceptionContent getExceptionHandle() {
			return exceptionHandle;
		}
	}
}
