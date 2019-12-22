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
import ch.xcal.serialization.stream.ref.ClassDescElement;
import ch.xcal.serialization.stream.ref.ObjectElement;
import ch.xcal.serialization.stream.ref.StringElement;
import ch.xcal.serialization.stream.root.ExceptionMarker;
import ch.xcal.serialization.stream.root.handle.StringHandle;

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
		final SerializationStream result = Parser.parse(new ByteArrayInputStream(o.toByteArray()));
		assertEquals(3, result.getRootElements().size());

		assertTrue(result.getRootElements().get(0) instanceof StringHandle);
		final IStreamElement content1 = result.resolveHandle((StringHandle) result.getRootElements().get(0));
		assertTrue(content1 instanceof StringElement);
		assertEquals("BeforeException", ((StringElement) content1).getValue());

		assertTrue(result.getRootElements().get(1) instanceof ExceptionMarker);
		final ObjectElement throwableContent = (ObjectElement) result
				.resolveHandle(((ExceptionMarker) result.getRootElements().get(1)).getThrowableHandle());
		assertEquals(IOException.class.getName(), ((ClassDescElement) result.resolveHandle(throwableContent.getClassDesc())).getName());

		assertTrue(result.getRootElements().get(2) instanceof StringHandle);
		final IStreamElement content3 = result.resolveHandle((StringHandle) result.getRootElements().get(2));
		assertTrue(content3 instanceof StringElement);
		assertEquals("AfterException", ((StringElement) content3).getValue());
	}

	@Test
	public void testParseExceptionInArray() throws IOException {
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final ObjectOutputStream out = new ObjectOutputStream(o);
		try {
			out.writeObject(new Object[]{
					"FirstElement",
					new ExceptionThrower(new IOException()),
					"LastElement"
			});
		} catch (IOException e) {
			// nop
		}
		out.flush();
		final SerializationStream result = Parser.parse(new ByteArrayInputStream(o.toByteArray()));
		assertEquals(1, result.getRootElements().size());
		assertTrue(result.getRootElements().get(0) instanceof ExceptionMarker);
		final ObjectElement throwableContent = (ObjectElement) result
				.resolveHandle(((ExceptionMarker) result.getRootElements().get(0)).getThrowableHandle());
		assertEquals(IOException.class.getName(), ((ClassDescElement) result.resolveHandle(throwableContent.getClassDesc())).getName());
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
