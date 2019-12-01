package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.ArrayContent;
import ch.xcal.serialization.parser.content.impl.IntegerContent;
import ch.xcal.serialization.parser.content.impl.StringContent;
import ch.xcal.serialization.parser.content.impl.TypeDesc.ObjectTypeDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.parser.handle.IArrayHandle;
import ch.xcal.serialization.parser.handle.impl.ArrayHandle;
import ch.xcal.serialization.parser.handle.impl.ObjectHandle;

public class ParserNewArrayTest {

	@Test
	public void testIntArray() throws IOException {
		final int[] array = new int[]{0, 1, 2};
		final ParserResult result = parseSingleArray(array);
		final ArrayContent resultArray = assertArray(result);
		assertSimpleIntegerArray(resultArray, array.length);
	}

	@Test
	public void testObjectArrayOfIntArray() throws IOException {
		final int[] intArray = new int[]{0, 1, 2};
		final Object[] array = new Object[]{intArray};
		final ParserResult result = parseSingleArray(array);
		final ArrayContent resultArray = assertArray(result);
		assertTrue(resultArray.getTypeDesc().getComponentType() instanceof ObjectTypeDesc);
		assertEquals(1, resultArray.getTypeDesc().getDimensions());
		assertEquals(1, resultArray.getElements().size());
		assertTrue(resultArray.getElements().get(0) instanceof ArrayHandle);
		final ArrayContent innerArray = (ArrayContent) result.getContent((ArrayHandle) resultArray.getElements().get(0));
		assertSimpleIntegerArray(innerArray, intArray.length);
	}

	@Test
	public void testMultidimensionalArray() throws IOException {
		final String[][] stringArray = new String[][]{
				new String[]{"value1"},
				new String[]{"value1", "value2"}};

		final ParserResult result = parseSingleArray(stringArray);
		final ArrayContent resultArray = assertArray(result);
		// component is another array
		assertTrue(resultArray.getTypeDesc().getComponentType() instanceof ObjectTypeDesc);
		assertEquals(2, resultArray.getTypeDesc().getDimensions());
		assertEquals(2, resultArray.getElements().size());
		assertTrue(resultArray.getElements().get(0) instanceof ArrayHandle);
		assertTrue(resultArray.getElements().get(1) instanceof ArrayHandle);
		final ArrayContent innerArray1 = (ArrayContent) result.getContent((ArrayHandle) resultArray.getElements().get(0));
		assertTrue(innerArray1.getTypeDesc().getComponentType() instanceof ObjectTypeDesc);
		assertEquals(1, innerArray1.getTypeDesc().getDimensions());
		assertEquals(1, innerArray1.getElements().size());
		assertTrue(innerArray1.getElements().get(0) instanceof ObjectHandle);
		final IContent innerArray1Element1 = result.getContent((ObjectHandle) innerArray1.getElements().get(0));
		assertTrue(innerArray1Element1 instanceof StringContent);
		assertEquals("value1", ((StringContent) innerArray1Element1).getValue());
	}

	private ParserResult parseSingleArray(Object array) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(array);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}

	private ArrayContent assertArray(ParserResult result) {
		assertEquals(1, result.getContents().size());
		assertTrue(result.getContents().get(0) instanceof IArrayHandle);
		final ArrayContent array = (ArrayContent) result.getContent((IArrayHandle) result.getContents().get(0));
		return array;
	}

	private void assertSimpleIntegerArray(final ArrayContent array, final int expectedLength) {
		assertEquals(PrimitiveTypeDesc.INTEGER, array.getTypeDesc().getComponentType());
		assertEquals(1, array.getTypeDesc().getDimensions());
		assertEquals(expectedLength, array.getElements().size());
		for (int i = 0; i < expectedLength; i++) {
			assertTrue(array.getElements().get(i) instanceof IntegerContent);
			assertEquals(i, ((IntegerContent) array.getElements().get(i)).getValue());
		}
	}
}
