package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import ch.xcal.serialization.parser.content.IClassDesc;
import ch.xcal.serialization.parser.content.IContent;
import ch.xcal.serialization.parser.content.impl.ClassDesc;
import ch.xcal.serialization.parser.content.impl.FieldDesc;
import ch.xcal.serialization.parser.content.impl.ProxyClassDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc;
import ch.xcal.serialization.parser.content.impl.TypeDesc.ArrayTypeDesc;
import ch.xcal.serialization.parser.handle.impl.ClassDescHandle;

public class ParserNewClassTest {

	@Test
	public void testObjectClass() throws IOException {
		final ParserResult parserResult = parseSingleClass(Object.class);
		final ClassDesc result = getClassDescResult(parserResult);
		assertEquals(Object.class.getName(), result.getName());
		assertEquals(0, result.getFields().size());
		assertFalse(result.hasSuperClass());
	}

	@Test
	public void testIntHolderClass() throws IOException {
		final ParserResult parserResult = parseSingleClass(IntHolder.class);
		final ClassDesc result = getClassDescResult(parserResult);
		assertEquals(IntHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final FieldDesc valueField = result.getFields().get(0);
		assertEquals("value", valueField.getFieldName());
		assertEquals(TypeDesc.PrimitiveTypeDesc.INTEGER, valueField.getType());

		assertFalse(result.hasSuperClass());
	}

	@Test
	public void testIntHolderHolderClass() throws IOException {
		final ParserResult parserResult = parseSingleClass(IntHolderHolder.class);
		final ClassDesc result = getClassDescResult(parserResult);

		assertEquals(IntHolderHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final FieldDesc valueField = result.getFields().get(0);
		assertEquals("value", valueField.getFieldName());
		assertTrue(valueField.getType() instanceof TypeDesc.ObjectTypeDesc);
		assertEquals(IntHolder.class.getName(), valueField.getType().getTypeName());

		assertFalse(result.hasSuperClass());
	}

	@Test
	public void testIntArrayHolderClass() throws IOException {
		final ParserResult parserResult = parseSingleClass(IntArrayHolder.class);
		final ClassDesc result = getClassDescResult(parserResult);

		assertEquals(IntArrayHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final FieldDesc valueField = result.getFields().get(0);
		assertEquals("value", valueField.getFieldName());
		assertTrue(valueField.getType() instanceof TypeDesc.ArrayTypeDesc);
		final TypeDesc.ArrayTypeDesc arrayType = (ArrayTypeDesc) valueField.getType();
		assertEquals(1, arrayType.getDimensions());
		assertEquals(TypeDesc.PrimitiveTypeDesc.INTEGER, arrayType.getComponentType());

		assertFalse(result.hasSuperClass());
	}

	@Test
	public void testStringArrayHolderClass() throws IOException {
		final ParserResult parserResult = parseSingleClass(StringArrayHolder.class);
		final ClassDesc result = getClassDescResult(parserResult);

		assertEquals(StringArrayHolder.class.getName(), result.getName());
		assertEquals(1, result.getFields().size());

		final FieldDesc valueField = result.getFields().get(0);
		assertEquals("value", valueField.getFieldName());
		assertTrue(valueField.getType() instanceof TypeDesc.ArrayTypeDesc);
		final TypeDesc.ArrayTypeDesc arrayType = (ArrayTypeDesc) valueField.getType();
		assertEquals(2, arrayType.getDimensions());
		assertEquals(String.class.getName(), arrayType.getComponentType().getTypeName());

		assertFalse(result.hasSuperClass());
	}

	@Test
	public void testProxyClass() throws IOException {
		final TestProxy testProxy = (TestProxy) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{TestProxy.class},
				new TestProxyInvocationHandler());

		final ParserResult parserResult = parseSingleClass(testProxy.getClass());
		final ProxyClassDesc result = getClassDescResult(parserResult);
		assertEquals(Collections.singletonList(TestProxy.class.getName()), result.getInterfaces());
	}

	@SuppressWarnings("unchecked")
	private <T extends IClassDesc> T getClassDescResult(final ParserResult parserResult) {
		assertEquals(1, parserResult.getContents().size());
		final IContent classDescHandle = parserResult.getContents().get(0);
		assertTrue(classDescHandle instanceof ClassDescHandle);
		final IContent resolvedClassDescHandle = parserResult.getContent((ClassDescHandle) classDescHandle);
		assertTrue(resolvedClassDescHandle instanceof ClassDescHandle);
		return (T) parserResult.getContent((ClassDescHandle) resolvedClassDescHandle);
	}

	private ParserResult parseSingleClass(Class<?> clz) throws IOException {
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
