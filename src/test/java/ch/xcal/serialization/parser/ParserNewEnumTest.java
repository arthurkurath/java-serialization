package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.stream.SerializationStream;
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.EnumElement;
import ch.xcal.serialization.stream.root.handle.EnumHandle;

public class ParserNewEnumTest {

	enum SimpleEnum {
		ENUM1, ENUM2
	};

	enum ComplexEnum {

		ENUM1("1"), ENUM2("2");

		private final String field;

		private ComplexEnum(String field) {
			this.field = field;
		}

		public String getField() {
			return field;
		}
	};

	@Test
	public void testSimpleEnum() throws IOException {
		final SerializationStream result = parseSingleEnum(SimpleEnum.ENUM1);
		final EnumElement enumContent = assertEnum(result);
		assertEquals("ch.xcal.serialization.parser.ParserNewEnumTest$SimpleEnum",
				((ClassDescElement) result.resolveHandle(enumContent.getClassDesc())).getName());
		assertEquals("ENUM1", result.resolveHandle(enumContent.getName()).getValue());
	}

	@Test
	public void testComplexEnum() throws IOException {
		final SerializationStream result = parseSingleEnum(ComplexEnum.ENUM2);
		final EnumElement enumContent = assertEnum(result);
		assertEquals("ch.xcal.serialization.parser.ParserNewEnumTest$ComplexEnum",
				((ClassDescElement) result.resolveHandle(enumContent.getClassDesc())).getName());
		assertEquals("ENUM2", result.resolveHandle(enumContent.getName()).getValue());
	}

	private SerializationStream parseSingleEnum(final Enum<?> enum0) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(enum0);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}

	private EnumElement assertEnum(SerializationStream result) {
		assertEquals(1, result.getRootElements().size());
		assertTrue(result.getRootElements().get(0) instanceof EnumHandle);
		return result.resolveHandle((EnumHandle) result.getRootElements().get(0));
	}
}
