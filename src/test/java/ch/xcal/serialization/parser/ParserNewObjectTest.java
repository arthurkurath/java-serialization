package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.ClassDesc;
import ch.xcal.serialization.parser.content.impl.IntegerContent;
import ch.xcal.serialization.parser.content.impl.ObjectContent;
import ch.xcal.serialization.parser.content.impl.StringContent;
import ch.xcal.serialization.parser.handle.IObjectHandle;
import ch.xcal.serialization.parser.handle.impl.NullHandle;
import ch.xcal.serialization.parser.handle.impl.ObjectHandle;

public class ParserNewObjectTest {
	@Test
	public void testIntHolderObject() throws IOException {
		final IntHolder intHolder = new IntHolder();
		intHolder.value = 10;
		final ParserResult result = parseSingleObject(intHolder);
		final ObjectContent obj = assertObjectOfClazz(result, intHolder.getClass());
		assertEquals(1, obj.getFields().size());
		assertTrue(obj.getFields().get(0) instanceof IntegerContent);
		assertEquals(intHolder.value, ((IntegerContent) obj.getFields().get(0)).getValue());
	}

	@Test
	public void testIntHolderHolderObject() throws IOException {
		final IntHolderHolder nullHolder = new IntHolderHolder();
		final ParserResult result = parseSingleObject(nullHolder);
		final ObjectContent obj = assertObjectOfClazz(result, nullHolder.getClass());
		assertEquals(1, obj.getFields().size());
		assertTrue(obj.getFields().get(0) instanceof NullHandle);

		final IntHolderHolder otherIntHolder = new IntHolderHolder();
		otherIntHolder.value = new IntHolder();
		otherIntHolder.value.value = -1;
		final ParserResult result2 = parseSingleObject(otherIntHolder);
		final ObjectContent obj2 = assertObjectOfClazz(result2, otherIntHolder.getClass());
		assertEquals(1, obj2.getFields().size());
		assertTrue(obj2.getFields().get(0) instanceof ObjectHandle);
		final ObjectContent obj2Value = (ObjectContent) result2.getContent((ObjectHandle) obj2.getFields().get(0));
		assertEquals(otherIntHolder.value.getClass().getName(), ((ClassDesc) obj2Value.getClassDesc()).getName());
		assertEquals(1, obj2Value.getFields().size());
		assertTrue(obj2Value.getFields().get(0) instanceof IntegerContent);
		assertEquals(otherIntHolder.value.value, ((IntegerContent) obj2Value.getFields().get(0)).getValue());
	}

	@Test
	public void testSubclassObject() throws IOException {
		final SubClass subClass = new SubClass();
		subClass.value = "subClass";
		((SuperClass) subClass).value = "superClass";

		final ParserResult result = parseSingleObject(subClass);
		final ObjectContent subClassObj = assertObjectOfClazz(result, subClass.getClass());
		final ObjectContent superClassObj = subClassObj.getSuperClassObject();
		assertEquals(SuperClass.class.getName(), ((ClassDesc) superClassObj.getClassDesc()).getName());

		assertEquals(1, subClassObj.getFields().size());
		assertTrue(subClassObj.getFields().get(0) instanceof ObjectHandle);
		final IContent subClassContent = result.getContent((ObjectHandle) subClassObj.getFields().get(0));
		assertTrue(subClassContent instanceof StringContent);
		assertEquals("subClass", ((StringContent) subClassContent).getValue());

		assertEquals(1, superClassObj.getFields().size());
		assertTrue(superClassObj.getFields().get(0) instanceof ObjectHandle);
		final IContent superClassContent = result.getContent((ObjectHandle) superClassObj.getFields().get(0));
		assertTrue(superClassContent instanceof StringContent);
		assertEquals("superClass", ((StringContent) superClassContent).getValue());
	}

	private ObjectContent assertObjectOfClazz(ParserResult result, Class<?> clz) {
		assertEquals(1, result.getContents().size()); // classDesc & object
		assertTrue(result.getContents().get(0) instanceof IObjectHandle);
		final ObjectContent obj = (ObjectContent) result.getContent((IObjectHandle) result.getContents().get(0));
		assertEquals(clz.getName(), ((ClassDesc) obj.getClassDesc()).getName());
		return obj;
	}

	private ParserResult parseSingleObject(Object clz) throws IOException {
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
