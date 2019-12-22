package ch.xcal.serialization.parser;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.xcal.serialization.common.ModifiedUTFHelper;
import ch.xcal.serialization.stream.IHandle;
import ch.xcal.serialization.stream.IPrimitiveElementOrHandle;
import ch.xcal.serialization.stream.IReferencedStreamElement;
import ch.xcal.serialization.stream.IRootStreamElement;
import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.descs.AbstractFieldDesc;
import ch.xcal.serialization.stream.descs.AbstractFieldDesc.PrimitiveFieldDesc;
import ch.xcal.serialization.stream.descs.AbstractFieldDesc.ReferenceFieldDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.ArrayTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.stream.descs.IFieldDesc;
import ch.xcal.serialization.stream.primitive.BooleanElement;
import ch.xcal.serialization.stream.primitive.ByteElement;
import ch.xcal.serialization.stream.primitive.CharElement;
import ch.xcal.serialization.stream.primitive.DoubleElement;
import ch.xcal.serialization.stream.primitive.FloatElement;
import ch.xcal.serialization.stream.primitive.IntegerElement;
import ch.xcal.serialization.stream.primitive.LongElement;
import ch.xcal.serialization.stream.primitive.ShortElement;
import ch.xcal.serialization.stream.ref.ArrayElement;
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.ClassElement;
import ch.xcal.serialization.stream.ref.EnumElement;
import ch.xcal.serialization.stream.ref.IClassDescElement;
import ch.xcal.serialization.stream.ref.ObjectElement;
import ch.xcal.serialization.stream.ref.ProxyClassDescElement;
import ch.xcal.serialization.stream.ref.StringElement;
import ch.xcal.serialization.stream.root.BlockDataElement;
import ch.xcal.serialization.stream.root.ExceptionMarker;
import ch.xcal.serialization.stream.root.NullHandle;
import ch.xcal.serialization.stream.root.ResetMarker;
import ch.xcal.serialization.stream.root.handle.ArrayHandle;
import ch.xcal.serialization.stream.root.handle.ClassDescHandle;
import ch.xcal.serialization.stream.root.handle.ClassHandle;
import ch.xcal.serialization.stream.root.handle.EnumHandle;
import ch.xcal.serialization.stream.root.handle.ObjectHandle;
import ch.xcal.serialization.stream.root.handle.StringHandle;

/**
 * Parser for a serialization stream.
 * 
 * https://docs.oracle.com/javase/8/docs/platform/serialization/spec/protocol.html
 */
public class Parser {
	private final DataInputStream byteStream;

	public static SerializationStream parse(final InputStream inputStream) throws IOException {
		return new Parser(inputStream).parse();
	}

	// stream state
	private int nextHandle;
	private Map<Integer, IHandle<?>> handleMap = new HashMap<>();
	// handles are only marker objects, i.e. can be compared using identity
	private Map<IHandle<?>, IReferencedStreamElement> referenceMap = new IdentityHashMap<>();

	// stream values
	private List<IRootStreamElement> rootElements = new ArrayList<>();

	protected Parser(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream);
		if (inputStream.getClass() == DataInputStream.class) {
			byteStream = (DataInputStream) inputStream;
		} else {
			byteStream = new DataInputStream(inputStream);
		}
		resetStream();
	}

	// helpers
	private ResetMarker resetStream() {
		handleMap = new HashMap<>();
		nextHandle = 0x7E0000;
		return ResetMarker.INSTANCE;
	}

	private NullHandle nullReference() {
		return NullHandle.INSTANCE;
	}

	private <REF extends IReferencedStreamElement> void putReference(IHandle<REF> handle, REF referencedObject) {
		if (referenceMap.containsKey(handle)) {
			throw new IllegalArgumentException("Handle already exists");
		}
		referenceMap.put(handle, Objects.requireNonNull(referencedObject));
	}

	private <REF extends IReferencedStreamElement> REF resolveReference(IHandle<REF> handle) {
		if (!referenceMap.containsKey(handle)) {
			throw new IllegalArgumentException("Unknown handle");
		}
		return handle.getReferencedStreamElementClass().cast(referenceMap.get(handle));
	}

	private <T extends IHandle<REF>, REF extends IReferencedStreamElement> T newHandle(Class<T> handleClass, REF referencedObject) {
		T newHandle = newHandle(handleClass);
		putReference(newHandle, referencedObject);
		return newHandle;
	}

	private <T extends IHandle<?>> T newHandle(Class<T> handleClass) {
		final IHandle<?> newHandle;
		if (handleClass == ClassDescHandle.class) {
			newHandle = new ClassDescHandle();
		} else if (handleClass == ClassHandle.class) {
			newHandle = new ClassHandle();
		} else if (handleClass == StringHandle.class) {
			newHandle = new StringHandle();
		} else if (handleClass == ObjectHandle.class) {
			newHandle = new ObjectHandle();
		} else if (handleClass == ArrayHandle.class) {
			newHandle = new ArrayHandle();
		} else if (handleClass == EnumHandle.class) {
			newHandle = new EnumHandle();
		} else {
			throw new IllegalArgumentException("unknown handle " + handleClass);
		}
		handleMap.put(nextHandle, newHandle);
		nextHandle++;
		return handleClass.cast(newHandle);
	}

	private <T extends IHandle<?>> T prevObject(final Class<T> expectedClass) throws IOException {
		final int handleNr = byteStream.readInt();
		final IHandle<?> handle = handleMap.get(handleNr);
		if (handle == null) {
			throw new IllegalStateException("Handle not found: " + handleNr);
		}
		return expectedClass.cast(handle);
	}

	public SerializationStream parse() throws IOException {
		readMagicNumber();
		readVersion();
		try {
			while (true) {
				try {
					rootElements.add(readContent(byteStream.readByte()));
				} catch (ParsedSerializedException e) {
					rootElements.add(e.getExceptionHandle());
				}
			}
		} catch (EOFException e) {
			return SerializationStream.create(referenceMap, rootElements);
		}
	}

	private void readMagicNumber() throws IOException {
		short magicNumber = byteStream.readShort();
		if (magicNumber != SerializationStream.MAGIC_NUMBER) {
			throw new IllegalStateException("magic number");
		}
	}

	private void readVersion() throws IOException {
		short streamVersion = byteStream.readShort();
		if (streamVersion != SerializationStream.STREAM_VERSION) {
			throw new IllegalStateException("stream version");
		}
	}

	private IRootStreamElement readContent(final byte contentType) throws ParsedSerializedException {
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

	private IRootStreamElement readObject(final byte contentType) throws ParsedSerializedException, IOException {
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

	private BlockDataElement readBlockData(final byte contentType) throws IOException {
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
		return BlockDataElement.create(blockData);
	}

	private ObjectHandle readNewObject() throws ParsedSerializedException, IOException {
		final ClassDescHandle classDescHandle = readClassDesc();
		final ObjectHandle handle = newHandle(ObjectHandle.class);
		putReference(handle, readObjectRec(classDescHandle));
		return handle;
	}

	private ObjectElement readObjectRec(final ClassDescHandle classDescHandle) throws ParsedSerializedException, IOException {
		final IClassDescElement classDesc = resolveReference(classDescHandle);
		final ObjectElement superClassObject = classDesc.getSuperClassHandle() != null
				? readObjectRec(classDesc.getSuperClassHandle())
				: null;
		final byte classDescFlags = classDesc.getFlags();
		if ((ObjectStreamConstants.SC_SERIALIZABLE & classDescFlags) > 0x0) {
			final List<IPrimitiveElementOrHandle> fields = new ArrayList<>(classDesc.getFields().size());
			for (final IFieldDesc field : classDesc.getFields()) {
				if (field instanceof PrimitiveFieldDesc) {
					fields.add(readPrimitiveValue(((PrimitiveFieldDesc) field).getType()));
				} else {
					fields.add(readReferenceValue());
				}
			}
			if ((ObjectStreamConstants.SC_WRITE_METHOD & classDescFlags) > 0x0) {
				return ObjectElement.create(classDescHandle, fields, readObjectAnnotation(), superClassObject);
			} else {
				return ObjectElement.create(classDescHandle, fields, Collections.emptyList(), superClassObject);
			}
		} else if ((ObjectStreamConstants.SC_EXTERNALIZABLE & classDescFlags) > 0x0) {
			if ((ObjectStreamConstants.SC_BLOCK_DATA & classDescFlags) > 0x0) {
				return ObjectElement.createWithAnnotationsOnly(classDescHandle, readObjectAnnotation(), superClassObject);
			} else {
				throw new UnsupportedOperationException("Cannot read external contents which were not written in block data mode");
			}
		} else {
			throw new UnsupportedOperationException("Cannot read class with flags " + classDescFlags);
		}
	}

	private IPrimitiveElementOrHandle readPrimitiveValue(final PrimitiveTypeDesc type) throws ParsedSerializedException, IOException {
		if (PrimitiveTypeDesc.BYTE == type) {
			return ByteElement.create(byteStream.readByte());
		} else if (PrimitiveTypeDesc.CHAR == type) {
			return CharElement.create(byteStream.readChar());
		} else if (PrimitiveTypeDesc.DOUBLE == type) {
			return DoubleElement.create(byteStream.readDouble());
		} else if (PrimitiveTypeDesc.FLOAT == type) {
			return FloatElement.create(byteStream.readFloat());
		} else if (PrimitiveTypeDesc.INTEGER == type) {
			return IntegerElement.create(byteStream.readInt());
		} else if (PrimitiveTypeDesc.LONG == type) {
			return LongElement.create(byteStream.readLong());
		} else if (PrimitiveTypeDesc.SHORT == type) {
			return ShortElement.create(byteStream.readShort());
		} else if (PrimitiveTypeDesc.BOOLEAN == type) {
			return BooleanElement.create(byteStream.readBoolean());
		} else {
			throw new UnsupportedOperationException("Unknown type " + type);
		}
	}

	private IPrimitiveElementOrHandle readReferenceValue() throws ParsedSerializedException, IOException {
		// null or handle
		return (IPrimitiveElementOrHandle) readObject(byteStream.readByte());
	}

	private ClassHandle readNewClass() throws ParsedSerializedException, IOException {
		final ClassDescHandle classDescHandle = readClassDesc();
		return newHandle(ClassHandle.class, ClassElement.create(classDescHandle));
	}

	private ArrayHandle readNewArray() throws ParsedSerializedException, IOException {
		final ClassDescHandle classDesc = readClassDesc();
		final ArrayHandle handle = newHandle(ArrayHandle.class);
		final int size = byteStream.readInt();
		final AbstractTypeDesc type0 = AbstractTypeDesc.parse(((ClassDescElement) resolveReference(classDesc)).getName());
		if (!(type0 instanceof ArrayTypeDesc)) {
			throw new IllegalStateException("Type is not an array");
		}
		final ArrayTypeDesc type = (ArrayTypeDesc) type0;
		final List<IPrimitiveElementOrHandle> elements = new ArrayList<>(size);
		if (type.getComponentType() instanceof PrimitiveTypeDesc) {
			final PrimitiveTypeDesc componentType = (PrimitiveTypeDesc) type.getComponentType();
			for (int i = 0; i < size; i++) {
				elements.add(readPrimitiveValue(componentType));
			}
		} else {
			for (int i = 0; i < size; i++) {
				elements.add(readReferenceValue());
			}
		}
		putReference(handle, ArrayElement.create(classDesc, elements));
		return handle;
	}

	private EnumHandle readNewEnum() throws ParsedSerializedException, IOException {
		final ClassDescHandle classDesc = readClassDesc();
		final EnumHandle handle = newHandle(EnumHandle.class);
		putReference(handle, EnumElement.create(classDesc, readString()));
		return handle;
	}

	private StringHandle readString() throws ParsedSerializedException, IOException {
		final byte stringType = byteStream.readByte();
		switch (stringType) {
			case ObjectStreamConstants.TC_STRING :
				return newString(byteStream.readUnsignedShort());
			case ObjectStreamConstants.TC_LONGSTRING :
				return newString(byteStream.readLong());
			case ObjectStreamConstants.TC_REFERENCE :
				return prevObject(StringHandle.class);
		}
		throw new IllegalStateException("StringType: " + stringType);

	}

	private ClassDescHandle readClassDesc() throws ParsedSerializedException, IOException {
		final byte classDescType = byteStream.readByte();
		switch (classDescType) {
			case ObjectStreamConstants.TC_CLASSDESC :
				return readClass();
			case ObjectStreamConstants.TC_PROXYCLASSDESC :
				return readProxyClass();
			case ObjectStreamConstants.TC_REFERENCE :
				return prevObject(ClassDescHandle.class);
			case ObjectStreamConstants.TC_NULL :
				return ClassDescHandle.NULL_CLASS_DESC_INSTANCE;
		}
		throw new IllegalStateException("ClassDescType: " + classDescType);
	}

	private ClassDescHandle readSuperClass() throws ParsedSerializedException, IOException {
		final ClassDescHandle superClass = readClassDesc();
		return superClass == ClassDescHandle.NULL_CLASS_DESC_INSTANCE ? null : superClass;
	}

	private ClassDescHandle readClass() throws ParsedSerializedException, IOException {
		final String className = ModifiedUTFHelper.readUTF(byteStream, byteStream.readUnsignedShort());
		final long serialVersionUID = byteStream.readLong();
		final ClassDescHandle handle = newHandle(ClassDescHandle.class);
		final byte classDescFlags = byteStream.readByte();
		final short numberOfFields = byteStream.readShort();
		final List<IFieldDesc> fieldDescs = new ArrayList<>(numberOfFields);
		for (short i = 0; i < numberOfFields; i++) {
			fieldDescs.add(readFieldDesc());
		}
		final List<IRootStreamElement> classAnnotations = readClassAnnotation();
		final ClassDescHandle superClass = readSuperClass();
		putReference(handle,
				ClassDescElement.create(className, serialVersionUID, classDescFlags, fieldDescs, classAnnotations, superClass));
		return handle;
	}

	private ClassDescHandle readProxyClass() throws ParsedSerializedException, IOException {
		final ClassDescHandle handle = newHandle(ClassDescHandle.class);
		final int numberOfInterfaces = byteStream.readInt();
		final List<String> interfaces = new ArrayList<>(numberOfInterfaces);
		for (int i = 0; i < numberOfInterfaces; i++) {
			interfaces.add(ModifiedUTFHelper.readUTF(byteStream, byteStream.readUnsignedShort()));
		}
		final List<IRootStreamElement> classAnnotations = readClassAnnotation();
		final ClassDescHandle superClass = readSuperClass();
		putReference(handle, ProxyClassDescElement.create(interfaces, classAnnotations, superClass));
		return handle;
	}

	private List<IRootStreamElement> readClassAnnotation() throws ParsedSerializedException, IOException {
		return readAnnotations();
	}

	private List<IRootStreamElement> readObjectAnnotation() throws ParsedSerializedException, IOException {
		return readAnnotations();
	}

	private List<IRootStreamElement> readAnnotations() throws ParsedSerializedException, IOException {
		final List<IRootStreamElement> annotations = new ArrayList<>();
		byte contentType;
		while ((contentType = byteStream.readByte()) != ObjectStreamConstants.TC_ENDBLOCKDATA) {
			annotations.add(readContent(contentType));
		}
		return annotations;
	}

	private AbstractFieldDesc readFieldDesc() throws ParsedSerializedException, IOException {
		final char typeCodeChar = (char) byteStream.readUnsignedByte();
		final String fieldName = ModifiedUTFHelper.readUTF(byteStream, byteStream.readUnsignedShort());

		if (typeCodeChar == '[') {
			final StringHandle typeHandle = readString();
			return ReferenceFieldDesc.createArray(fieldName, typeHandle);
		} else if (typeCodeChar == 'L') {
			final StringHandle typeHandle = readString();
			return ReferenceFieldDesc.createObject(fieldName, typeHandle);
		} else {
			final PrimitiveTypeDesc type = (PrimitiveTypeDesc) AbstractTypeDesc.parse(Character.toString(typeCodeChar));
			return PrimitiveFieldDesc.create(fieldName, type);
		}
	}

	private ObjectHandle readException() throws ParsedSerializedException, IOException {
		resetStream();
		final ObjectHandle throwableHandle = (ObjectHandle) readObject(byteStream.readByte());
		resetStream();
		throw new ParsedSerializedException(ExceptionMarker.create(throwableHandle));
	}

	private StringHandle newString(final long stringBytes) throws IOException {
		if (stringBytes < 0 || stringBytes > Integer.MAX_VALUE) {
			throw new IllegalStateException("String length: " + stringBytes);
		}
		return newHandle(StringHandle.class, StringElement.create(ModifiedUTFHelper.readUTF(byteStream, (int) stringBytes)));
	}

	private static class ParsedSerializedException extends Exception {

		private static final long serialVersionUID = 1L;

		private final ExceptionMarker exceptionHandle;

		public ParsedSerializedException(final ExceptionMarker handle) {
			this.exceptionHandle = Objects.requireNonNull(handle);
		}

		public ExceptionMarker getExceptionHandle() {
			return exceptionHandle;
		}
	}
}
