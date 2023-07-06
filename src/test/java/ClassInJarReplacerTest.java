import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.toolfactory.narcissus.Narcissus;

class ClassInJarReplacerTest {

	private static Method METHOD_CAST, METHOD_GET_FILE, METHOD_GET_CLASS = null;

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

	private static String toString(final Object instance) {
		return instance != null ? instance.toString() : null;
	}

}