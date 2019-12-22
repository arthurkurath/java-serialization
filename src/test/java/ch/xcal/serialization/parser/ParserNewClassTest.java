package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;

import org.junit.Test;

import ch.xcal.serialization.stream.IStreamElement;
import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.descs.AbstractFieldDesc.PrimitiveFieldDesc;
import ch.xcal.serialization.stream.descs.AbstractFieldDesc.ReferenceFieldDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.ArrayTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.ObjectTypeDesc;
import ch.xcal.serialization.stream.descs.AbstractTypeDesc.PrimitiveTypeDesc;
import ch.xcal.serialization.stream.descs.IFieldDesc;
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.ClassElement;
import ch.xcal.serialization.stream.ref.IClassDescElement;
import ch.xcal.serialization.stream.ref.ProxyClassDescElement;
import ch.xcal.serialization.stream.root.handle.ClassHandle;

public class ParserNewClassTest {

	@Test
	public void testObjectClass() throws IOException {
		final SerializationStream parserResult = parseSingleClass(Object.class);
		final ClassDescElement result = getClassDescResult(parserResult);
		assertEquals(Object.class.getName(), result.getName());
		assertEquals(0, result.getFields().size());
		assertNull(result.getSuperClassHandle());
	}

	@Test
	public void testIntHolderClass() throws IOException {
		final SerializationStream parserResult = parseSingleClass(IntHolder.class);
		final ClassDescElement result = getClassDescResult(parserResult);
		assertEquals(IntHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final IFieldDesc valueField = result.getFields().get(0);
		assertTrue(valueField instanceof PrimitiveFieldDesc);
		assertEquals("value", valueField.getFieldName());
		assertEquals(PrimitiveTypeDesc.INTEGER, ((PrimitiveFieldDesc) valueField).getType());

		assertNull(result.getSuperClassHandle());
	}

	@Test
	public void testIntHolderHolderClass() throws IOException {
		final SerializationStream parserResult = parseSingleClass(IntHolderHolder.class);
		final ClassDescElement result = getClassDescResult(parserResult);

		assertEquals(IntHolderHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final IFieldDesc valueField = result.getFields().get(0);
		assertTrue(valueField instanceof ReferenceFieldDesc);
		assertEquals("value", valueField.getFieldName());
		final ObjectTypeDesc type = (ObjectTypeDesc) AbstractTypeDesc
				.parse(parserResult.resolveHandle(((ReferenceFieldDesc) valueField).getTypeHandle()).getValue());
		assertTrue(type instanceof ObjectTypeDesc);
		assertEquals(IntHolder.class.getName(), type.getTypeName());

		assertNull(result.getSuperClassHandle());
	}

	@Test
	public void testIntArrayHolderClass() throws IOException {
		final SerializationStream parserResult = parseSingleClass(IntArrayHolder.class);
		final ClassDescElement result = getClassDescResult(parserResult);

		assertEquals(IntArrayHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final IFieldDesc valueField = result.getFields().get(0);
		assertTrue(valueField instanceof ReferenceFieldDesc);
		assertEquals("value", valueField.getFieldName());
		final ArrayTypeDesc arrayType = (ArrayTypeDesc) AbstractTypeDesc
				.parse(parserResult.resolveHandle(((ReferenceFieldDesc) valueField).getTypeHandle()).getValue());
		assertEquals(1, arrayType.getDimensions());
		assertEquals(PrimitiveTypeDesc.INTEGER, arrayType.getComponentType());

		assertNull(result.getSuperClassHandle());
	}

	@Test
	public void testStringArrayHolderClass() throws IOException {
		final SerializationStream parserResult = parseSingleClass(StringArrayHolder.class);
		final ClassDescElement result = getClassDescResult(parserResult);

		assertEquals(StringArrayHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final IFieldDesc valueField = result.getFields().get(0);
		assertTrue(valueField instanceof ReferenceFieldDesc);
		assertEquals("value", valueField.getFieldName());
		final ArrayTypeDesc arrayType = (ArrayTypeDesc) AbstractTypeDesc
				.parse(parserResult.resolveHandle(((ReferenceFieldDesc) valueField).getTypeHandle()).getValue());
		assertEquals(2, arrayType.getDimensions());
		assertEquals(String.class.getName(), arrayType.getComponentType().getTypeName());

		assertNull(result.getSuperClassHandle());
	}

	@Test
	public void testProxyClass() throws IOException {
		final TestProxy testProxy = (TestProxy) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{TestProxy.class},
				new TestProxyInvocationHandler());

		final SerializationStream parserResult = parseSingleClass(testProxy.getClass());
		final ProxyClassDescElement result = getClassDescResult(parserResult);
		assertEquals(Collections.singletonList(TestProxy.class.getName()), result.getInterfaces());
	}

	@SuppressWarnings("unchecked")
	private <T extends IClassDescElement> T getClassDescResult(final SerializationStream parserResult) {
		assertEquals(1, parserResult.getRootElements().size());
		final IStreamElement classDescHandle = parserResult.getRootElements().get(0);
		assertTrue(classDescHandle instanceof ClassHandle);
		final ClassElement resolvedClassElement = parserResult.resolveHandle((ClassHandle) classDescHandle);
		return (T) parserResult.resolveHandle(resolvedClassElement.getClassDesc());
	}

	private SerializationStream parseSingleClass(Class<?> clz) throws IOException {
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

	public static class IntArrayHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		public int[] value;
	}

	public static class StringArrayHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		public String[][] value;
	}

	public static interface TestProxy {
	}

	public static class TestProxyInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return new Object();
		}
	}
}
