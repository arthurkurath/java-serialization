package ch.xcal.serialization.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.xcal.serialization.parser.content.impl.EnumContent;
import ch.xcal.serialization.parser.handle.IEnumHandle;

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
		final ParserResult result = parseSingleEnum(SimpleEnum.ENUM1);
		final EnumContent enumContent = assertEnum(result);
		assertEquals("ch.xcal.serialization.parser.ParserNewEnumTest$SimpleEnum", enumContent.getClassDesc().getName());
		assertEquals("ENUM1", enumContent.getName());
	}

	@Test
	public void testComplexEnum() throws IOException {
		final ParserResult result = parseSingleEnum(ComplexEnum.ENUM2);
		final EnumContent enumContent = assertEnum(result);
		assertEquals("ch.xcal.serialization.parser.ParserNewEnumTest$ComplexEnum", enumContent.getClassDesc().getName());
		assertEquals("ENUM2", enumContent.getName());
	}

	private ParserResult parseSingleEnum(final Enum<?> enum0) throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject(enum0);
		out.flush();
		final byte[] result = o.toByteArray();
		return Parser.parse(new ByteArrayInputStream(result));
	}

	private EnumContent assertEnum(ParserResult result) {
		assertEquals(1, result.getContents().size());
		assertTrue(result.getContents().get(0) instanceof IEnumHandle);
		final EnumContent enumContent = (EnumContent) result.getContent((IEnumHandle) result.getContents().get(0));
		return enumContent;
	}
}
