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
import ch.xcal.serialization.parser.content.impl.ExceptionContent;
import ch.xcal.serialization.parser.content.impl.ObjectContent;
import ch.xcal.serialization.parser.content.impl.StringContent;
import ch.xcal.serialization.parser.handle.impl.ObjectHandle;

public class ParserExceptionTest {

	@Test
	public void testParseException() throws IOException {

		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		out.writeObject("BeforeException");
		try {
			out.writeObject(new ExceptionThrower(new IOException()));
		} catch (IOException ex) {
			// nop
		}
		out.writeObject("AfterException");
		out.flush();
		final ParserResult result = Parser.parse(new ByteArrayInputStream(o.toByteArray()));
		assertEquals(3, result.getContents().size());

		assertTrue(result.getContents().get(0) instanceof ObjectHandle);
		final IContent content1 = result.getContent((ObjectHandle) result.getContents().get(0));
		assertTrue(content1 instanceof StringContent);
		assertEquals("BeforeException", ((StringContent) content1).getValue());

		assertTrue(result.getContents().get(1) instanceof ExceptionContent);
		final ObjectContent throwableContent = (ObjectContent) result
				.getContent(((ExceptionContent) result.getContents().get(1)).getThrowableHandle());
		assertEquals(IOException.class.getName(), ((ClassDesc) throwableContent.getClassDesc()).getName());

		assertTrue(result.getContents().get(2) instanceof ObjectHandle);
		final IContent content3 = result.getContent((ObjectHandle) result.getContents().get(2));
		assertTrue(content3 instanceof StringContent);
		assertEquals("AfterException", ((StringContent) content3).getValue());
	}

	private static class ExceptionThrower implements Serializable {

		private static final long serialVersionUID = 1L;

		private final transient IOException exception;

		public ExceptionThrower(final IOException exception) {
			this.exception = exception;
		}

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeObject("writeObject");
			throw exception;
		}
	}
}
