package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.ArrayTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.ObjectTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.stream.primitive.IntegerElement;
import ch.xcal.serialization.stream.ref.ArrayElement;
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.StringElement;
import ch.xcal.serialization.stream.root.handle.ArrayHandle;
import ch.xcal.serialization.stream.root.handle.StringHandle;

public class ParserNewArrayTest {

	@Test
	public void testIntArray() throws IOException {
		final int[] array = new int[]{0, 1, 2};
		final SerializationStream result = parseSingleArray(array);
		final ArrayElement resultArray = assertArray(result);
		assertSimpleIntegerArray(resultArray, resolveArrayType(result, resultArray), array.length);
	}

	@Test
	public void testObjectArrayOfIntArray() throws IOException {
		final int[] intArray = new int[]{0, 1, 2};
		final Object[] array = new Object[]{intArray};
		final SerializationStream result = parseSingleArray(array);
		final ArrayElement resultArray = assertArray(result);
		final ArrayTypeDesc typeDesc = resolveArrayType(result, resultArray);
		assertTrue(typeDesc.getComponentType() instanceof ObjectTypeDesc);
		assertEquals(1, typeDesc.getDimensions());
		assertEquals(1, resultArray.getElements().size());
		assertTrue(resultArray.getElements().get(0) instanceof ArrayHandle);
		final ArrayElement innerArray = (ArrayElement) result.resolveHandle((ArrayHandle) resultArray.getElements().get(0));
		assertSimpleIntegerArray(innerArray, resolveArrayType(result, innerArray), intArray.length);
	}

	@Test
	public void testMultidimensionalArray() throws IOException {
		final String[][] stringArray = new String[][]{
				new String[]{"value1"},
				new String[]{"value1", "value2"}};

		final SerializationStream result = parseSingleArray(stringArray);
		final ArrayElement resultArray = assertArray(result);
		// component is another array
		final ArrayTypeDesc resultArrayTypeDesc = resolveArrayType(result, resultArray);
		assertTrue(resultArrayTypeDesc.getComponentType() instanceof ObjectTypeDesc);
		assertEquals(2, resultArrayTypeDesc.getDimensions());
		assertEquals(2, resultArray.getElements().size());
		assertTrue(resultArray.getElements().get(0) instanceof ArrayHandle);
		assertTrue(resultArray.getElements().get(1) instanceof ArrayHandle);
		final ArrayElement innerArray1 = (ArrayElement) result.resolveHandle((ArrayHandle) resultArray.getElements().get(0));
		final ArrayTypeDesc innerArray1TypeDesc = resolveArrayType(result, innerArray1);
		assertTrue(innerArray1TypeDesc.getComponentType() instanceof ObjectTypeDesc);
		assertEquals(1, innerArray1TypeDesc.getDimensions());
		assertEquals(1, innerArray1.getElements().size());
		assertTrue(innerArray1.getElements().get(0) instanceof StringHandle);
		final StringElement innerArray1Element1 = result.resolveHandle((StringHandle) innerArray1.getElements().get(0));
		assertEquals("value1", innerArray1Element1.getValue());
	}

	private SerializationStream parseSingleArray(Object array) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(array);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}

	private ArrayElement assertArray(SerializationStream result) {
		assertEquals(1, result.getRootElements().size());
		assertTrue(result.getRootElements().get(0) instanceof ArrayHandle);
		final ArrayElement array = result.resolveHandle((ArrayHandle) result.getRootElements().get(0));
		return array;
	}

	private void assertSimpleIntegerArray(final ArrayElement array, final ArrayTypeDesc arrayTypeDesc, final int expectedLength) {
		assertEquals(PrimitiveTypeDesc.INTEGER, arrayTypeDesc.getComponentType());
		assertEquals(1, arrayTypeDesc.getDimensions());
		assertEquals(expectedLength, array.getElements().size());
		for (int i = 0; i < expectedLength; i++) {
			assertTrue(array.getElements().get(i) instanceof IntegerElement);
			assertEquals(i, ((IntegerElement) array.getElements().get(i)).getValue());
		}
	}

	private ArrayTypeDesc resolveArrayType(final SerializationStream result, final ArrayElement arrayElement) {
		return (ArrayTypeDesc) AbstractTypeDesc.parse(((ClassDescElement) result.resolveHandle(arrayElement.getClassDescHandle())).getName());
	}
}
