import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.text.JTextComponent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.toolfactory.narcissus.Narcissus;

class ClassInJarReplacerTest {

	private static Method METHOD_CAST, METHOD_GET_FILE, METHOD_GET_CLASS, METHOD_TO_STRING, METHOD_UPDATE_ZIP_ENTRY4,
			METHOD_UPDATE_ZIP_ENTRY5 = null;

	@BeforeAll
	static void beforeAll() throws ReflectiveOperationException {
		//
		final Class<?> clz = ClassInJarReplacer.class;
		//
		(METHOD_CAST = clz.getDeclaredMethod("cast", Class.class, Object.class)).setAccessible(true);
		//
		(METHOD_GET_FILE = clz.getDeclaredMethod("getFile", List.class)).setAccessible(true);
		//
		(METHOD_GET_CLASS = clz.getDeclaredMethod("getClass", Object.class)).setAccessible(true);
		//
		(METHOD_TO_STRING = clz.getDeclaredMethod("toString", Object.class)).setAccessible(true);
		//
		(METHOD_UPDATE_ZIP_ENTRY4 = clz.getDeclaredMethod("updateZipEntry", File.class, JTextComponent.class,
				File.class, Number.class)).setAccessible(true);
		//
		(METHOD_UPDATE_ZIP_ENTRY5 = clz.getDeclaredMethod("updateZipEntry", File.class, File.class, Collection.class,
				JTextComponent.class, Integer.TYPE)).setAccessible(true);
		//
	}

	private ClassInJarReplacer instance = null;

	@BeforeEach
	void beforeEach() throws Throwable {
		//
		if (!GraphicsEnvironment.isHeadless()) {
			//
			final Constructor<ClassInJarReplacer> constructor = ClassInJarReplacer.class.getDeclaredConstructor();
			//
			if (constructor != null) {
				//
				constructor.setAccessible(true);
				//
			} // if
				//
			instance = constructor != null ? constructor.newInstance() : null;
			//
		} else {
			//
			instance = cast(ClassInJarReplacer.class, Narcissus.allocateInstance(ClassInJarReplacer.class));
			//
		} // if
			//
	}

	private static <T> T cast(final Class<T> clz, final Object instance) throws Throwable {
		try {
			return (T) METHOD_CAST.invoke(null, clz, instance);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testMain() {
		//
		Assertions.assertDoesNotThrow(() -> ClassInJarReplacer.main(null));
		//
	}

	@Test
	void testActionPerformed() {
		//
		Assertions.assertDoesNotThrow(() -> actionPerformed(instance, null));
		//
		Assertions.assertDoesNotThrow(() -> actionPerformed(instance, new ActionEvent("", 0, null)));
		//
	}

	private static void actionPerformed(final ActionListener instance, final ActionEvent actionEvent) {
		if (instance != null) {
			instance.actionPerformed(actionEvent);
		}
	}

	@Test
	void testDrop() {
		//
		Assertions.assertDoesNotThrow(() -> {
			//
			if (instance != null) {
				//
				instance.drop(null);
				//
			} // if
				//
		});
		//
	}

	@Test
	void testGetFile() throws Throwable {
		//
		Assertions.assertNull(getFile(Collections.nCopies(2, null)));
		//
		Assertions.assertNull(getFile(Collections.singletonList(null)));
		//
		Assertions.assertNull(getFile(Collections.singletonList("")));
		//
		final File file = new File(".");
		//
		Assertions.assertSame(file, getFile(Collections.singletonList(file)));
		//
	}

	private static File getFile(final List<?> list) throws Throwable {
		try {
			final Object obj = METHOD_GET_FILE.invoke(null, list);
			if (obj == null) {
				return null;
			} else if (obj instanceof File) {
				return (File) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetClass() throws Throwable {
		//
		Assertions.assertNull(getClass(null));
		//
	}

	private static Class<?> getClass(final Object instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_CLASS.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Class<?>) {
				return (Class<?>) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testToString() throws Throwable {
		//
		final String string = "";
		//
		Assertions.assertSame(string, toString(string));
		//
	}

	private static String toString(final Object instance) throws Throwable {
		try {
			final Object obj = METHOD_TO_STRING.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof String) {
				return (String) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testUpdateZipEntry() throws IOException {
		//
		final File filePomXml = new File("pom.xml");
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(filePomXml, null, null, null));
		//
		final File fileZip = File.createTempFile(RandomStringUtils.randomAlphanumeric(3), null);
		//
		if (fileZip != null) {
			//
			fileZip.deleteOnExit();
			//
		} // if
			//
		FileUtils.writeByteArrayToFile(fileZip, crateZipAsByteArray("a.b", "".getBytes()));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(fileZip, null, null, null));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(fileZip, null, filePomXml, null));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(null, null, null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(new File("."), null, null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(filePomXml, null, Collections.emptyList(), null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(null, null, Collections.singleton(null), null, 0));
		//
	}

	private static byte[] crateZipAsByteArray(final String filename, final byte[] content) throws IOException {
		//
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final ZipOutputStream zip = new ZipOutputStream(baos)) {
			//
			zip.putNextEntry(new ZipEntry(filename));
			//
			zip.write(content, 0, content != null ? content.length : 0);
			//
			zip.closeEntry();
			//
			zip.flush();
			//
			return baos.toByteArray();
			//
		} // try
			//
	}

	private static void updateZipEntry(final File fileJar, final JTextComponent jtc, final File file,
			final Number compressionMethod) throws Throwable {
		try {
			METHOD_UPDATE_ZIP_ENTRY4.invoke(null, fileJar, jtc, file, compressionMethod);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static void updateZipEntry(final File file, final File fileJar, final Collection<ZipEntry> collection,
			final JTextComponent jtc, final int cm) throws Throwable {
		try {
			METHOD_UPDATE_ZIP_ENTRY5.invoke(null, file, fileJar, collection, jtc, cm);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}