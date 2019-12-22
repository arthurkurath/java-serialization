package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

import ch.xcal.serialization.stream.IStreamElement;
import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.primitive.IntegerElement;
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.ObjectElement;
import ch.xcal.serialization.stream.ref.StringElement;
import ch.xcal.serialization.stream.root.NullHandle;
import ch.xcal.serialization.stream.root.handle.ObjectHandle;
import ch.xcal.serialization.stream.root.handle.StringHandle;

public class ParserNewObjectTest {
	@Test
	public void testIntHolderObject() throws IOException {
		final IntHolder intHolder = new IntHolder();
		intHolder.value = 10;
		final SerializationStream result = parseSingleObject(intHolder);
		final ObjectElement obj = assertObjectOfClazz(result, intHolder.getClass());
		assertEquals(1, obj.getFields().size());
		assertTrue(obj.getFields().get(0) instanceof IntegerElement);
		assertEquals(intHolder.value, ((IntegerElement) obj.getFields().get(0)).getValue());
	}

	@Test
	public void testIntHolderHolderObject() throws IOException {
		final IntHolderHolder nullHolder = new IntHolderHolder();
		final SerializationStream result = parseSingleObject(nullHolder);
		final ObjectElement obj = assertObjectOfClazz(result, nullHolder.getClass());
		assertEquals(1, obj.getFields().size());
		assertTrue(obj.getFields().get(0) instanceof NullHandle);

		final IntHolderHolder otherIntHolder = new IntHolderHolder();
		otherIntHolder.value = new IntHolder();
		otherIntHolder.value.value = -1;
		final SerializationStream result2 = parseSingleObject(otherIntHolder);
		final ObjectElement obj2 = assertObjectOfClazz(result2, otherIntHolder.getClass());
		assertEquals(1, obj2.getFields().size());
		assertTrue(obj2.getFields().get(0) instanceof ObjectHandle);
		final ObjectElement obj2Value = (ObjectElement) result2.resolveHandle((ObjectHandle) obj2.getFields().get(0));
		assertEquals(otherIntHolder.value.getClass().getName(),
				((ClassDescElement) result2.resolveHandle(obj2Value.getClassDesc())).getName());
		assertEquals(1, obj2Value.getFields().size());
		assertTrue(obj2Value.getFields().get(0) instanceof IntegerElement);
		assertEquals(otherIntHolder.value.value, ((IntegerElement) obj2Value.getFields().get(0)).getValue());
	}

	@Test
	public void testSubclassObject() throws IOException {
		final SubClass subClass = new SubClass();
		subClass.value = "subClass";
		((SuperClass) subClass).value = "superClass";

		final SerializationStream result = parseSingleObject(subClass);
		final ObjectElement subClassObj = assertObjectOfClazz(result, subClass.getClass());
		final ObjectElement superClassObj = subClassObj.getSuperClassObject();
		assertEquals(SuperClass.class.getName(), ((ClassDescElement) result.resolveHandle(superClassObj.getClassDesc())).getName());

		assertEquals(1, subClassObj.getFields().size());
		assertTrue(subClassObj.getFields().get(0) instanceof StringHandle);
		final IStreamElement subClassContent = result.resolveHandle((StringHandle) subClassObj.getFields().get(0));
		assertTrue(subClassContent instanceof StringElement);
		assertEquals("subClass", ((StringElement) subClassContent).getValue());

		assertEquals(1, superClassObj.getFields().size());
		assertTrue(superClassObj.getFields().get(0) instanceof StringHandle);
		final IStreamElement superClassContent = result.resolveHandle((StringHandle) superClassObj.getFields().get(0));
		assertTrue(superClassContent instanceof StringElement);
		assertEquals("superClass", ((StringElement) superClassContent).getValue());
	}

	private ObjectElement assertObjectOfClazz(SerializationStream result, Class<?> clz) {
		assertEquals(1, result.getRootElements().size()); // classDesc & object
		assertTrue(result.getRootElements().get(0) instanceof ObjectHandle);
		final ObjectElement obj = result.resolveHandle((ObjectHandle) result.getRootElements().get(0));
		assertEquals(clz.getName(), ((ClassDescElement) result.resolveHandle(obj.getClassDesc())).getName());
		return obj;
	}

	private SerializationStream parseSingleObject(Object clz) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(clz);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}

	public static class IntHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		public int value;
	}

	public static class IntHolderHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		public IntHolder value;
	}

	public static class SuperClass implements Serializable {
		private static final long serialVersionUID = 1L;
		public String value;
	}

	public static class SubClass extends SuperClass {
		private static final long serialVersionUID = 1L;
		public String value;
	}
}
