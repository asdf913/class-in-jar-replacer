import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.meeuw.functional.Consumers;
import org.meeuw.functional.Predicates;
import org.zeroturnaround.zip.ZipEntryCallback;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

class ClassInJarReplacerTest {

	private static Method METHOD_CAST, METHOD_GET_FILE, METHOD_GET_CLASS, METHOD_TO_STRING, METHOD_UPDATE_ZIP_ENTRY5,
			METHOD_UPDATE_ZIP_ENTRY6, METHOD_ADD_JAVA_CLASS_INTO_ZIP_FILE, METHOD_GET_LIST,
			METHOD_ADD_DROP_TARGET_LISTENER, METHOD_STREAM, METHOD_FILTER, METHOD_TO_LIST, METHOD_GET_NAME_MEMBER,
			METHOD_GET_NAME_FILE, METHOD_GET_NAME_ZIP_ENTRY, METHOD_SET_TEXT, METHOD_CONTAINS_KEY_MULTI_MAP,
			METHOD_CONTAINS_KEY_MAP, METHOD_GET, METHOD_TEST_AND_ACCEPT3, METHOD_TEST_AND_ACCEPT4, METHOD_GET_VALUE,
			METHOD_GET_INSTRUCTIONS, METHOD_GET_CONSTANT_POOL, METHOD_GET_METHOD, METHOD_SET_EDITABLE,
			METHOD_GET_CLASS_NAME_STACK_TRACE_ELEMENT, METHOD_GET_CLASS_NAME_JAVA_CLASS, METHOD_EXISTS,
			METHOD_GET_SELECTED_ITEM, METHOD_GET_ABSOLUTE_PATH, METHOD_INT_VALUE, METHOD_IS_SELECTED,
			METHOD_GET_LIST_CELL_RENDERER_COMPONENT, METHOD_GET_VALUE_FIELD_MAP_BY_STATIC_FIELDS_AND_VALUES, METHOD_PUT,
			METHOD_SET_DROP_TARGET = null;

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
		(METHOD_UPDATE_ZIP_ENTRY5 = clz.getDeclaredMethod("updateZipEntry", File.class, JTextComponent.class,
				File.class, JTextComponent.class, Number.class)).setAccessible(true);
		//
		(METHOD_UPDATE_ZIP_ENTRY6 = clz.getDeclaredMethod("updateZipEntry", File.class, File.class, Collection.class,
				JTextComponent.class, JTextComponent.class, Integer.TYPE)).setAccessible(true);
		//
		(METHOD_ADD_JAVA_CLASS_INTO_ZIP_FILE = clz.getDeclaredMethod("addJavaClassIntoZipFile", File.class, File.class,
				JTextComponent.class, Integer.TYPE)).setAccessible(true);
		//
		(METHOD_GET_LIST = clz.getDeclaredMethod("getList", Transferable.class)).setAccessible(true);
		//
		(METHOD_ADD_DROP_TARGET_LISTENER = clz.getDeclaredMethod("addDropTargetListener", DropTarget.class,
				DropTargetListener.class)).setAccessible(true);
		//
		(METHOD_STREAM = clz.getDeclaredMethod("stream", Collection.class)).setAccessible(true);
		//
		(METHOD_FILTER = clz.getDeclaredMethod("filter", Stream.class, Predicate.class)).setAccessible(true);
		//
		(METHOD_TO_LIST = clz.getDeclaredMethod("toList", Stream.class)).setAccessible(true);
		//
		(METHOD_GET_NAME_MEMBER = clz.getDeclaredMethod("getName", Member.class)).setAccessible(true);
		//
		(METHOD_GET_NAME_FILE = clz.getDeclaredMethod("getName", File.class)).setAccessible(true);
		//
		(METHOD_GET_NAME_ZIP_ENTRY = clz.getDeclaredMethod("getName", ZipEntry.class)).setAccessible(true);
		//
		(METHOD_SET_TEXT = clz.getDeclaredMethod("setText", JTextComponent.class, String.class)).setAccessible(true);
		//
		(METHOD_CONTAINS_KEY_MULTI_MAP = clz.getDeclaredMethod("containsKey", Multimap.class, Object.class))
				.setAccessible(true);
		//
		(METHOD_CONTAINS_KEY_MAP = clz.getDeclaredMethod("containsKey", Map.class, Object.class)).setAccessible(true);
		//
		(METHOD_GET = clz.getDeclaredMethod("get", Multimap.class, Object.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_ACCEPT3 = clz.getDeclaredMethod("testAndAccept", Predicate.class, Object.class,
				Consumer.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_ACCEPT4 = clz.getDeclaredMethod("testAndAccept", BiPredicate.class, Object.class, Object.class,
				FailableBiConsumer.class)).setAccessible(true);
		//
		(METHOD_GET_VALUE = clz.getDeclaredMethod("getValue", ConstantPushInstruction.class)).setAccessible(true);
		//
		(METHOD_GET_INSTRUCTIONS = clz.getDeclaredMethod("getInstructions", InstructionList.class)).setAccessible(true);
		//
		(METHOD_GET_CONSTANT_POOL = clz.getDeclaredMethod("getConstantPool", JavaClass.class)).setAccessible(true);
		//
		(METHOD_GET_METHOD = clz.getDeclaredMethod("getMethod", JavaClass.class, java.lang.reflect.Method.class))
				.setAccessible(true);
		//
		(METHOD_SET_EDITABLE = clz.getDeclaredMethod("setEditable", Boolean.TYPE, JTextComponent.class,
				JTextComponent.class, JTextComponent[].class)).setAccessible(true);
		//
		(METHOD_GET_CLASS_NAME_STACK_TRACE_ELEMENT = clz.getDeclaredMethod("getClassName", StackTraceElement.class))
				.setAccessible(true);
		//
		(METHOD_GET_CLASS_NAME_JAVA_CLASS = clz.getDeclaredMethod("getClassName", JavaClass.class)).setAccessible(true);
		//
		(METHOD_EXISTS = clz.getDeclaredMethod("exists", File.class)).setAccessible(true);
		//
		(METHOD_GET_SELECTED_ITEM = clz.getDeclaredMethod("getSelectedItem", JComboBox.class)).setAccessible(true);
		//
		(METHOD_GET_ABSOLUTE_PATH = clz.getDeclaredMethod("getAbsolutePath", File.class)).setAccessible(true);
		//
		(METHOD_INT_VALUE = clz.getDeclaredMethod("intValue", Number.class, Integer.TYPE)).setAccessible(true);
		//
		(METHOD_IS_SELECTED = clz.getDeclaredMethod("isSelected", AbstractButton.class)).setAccessible(true);
		//
		(METHOD_GET_LIST_CELL_RENDERER_COMPONENT = clz.getDeclaredMethod("getListCellRendererComponent",
				ListCellRenderer.class, JList.class, Object.class, Integer.TYPE, Boolean.TYPE, Boolean.TYPE))
				.setAccessible(true);
		//
		(METHOD_GET_VALUE_FIELD_MAP_BY_STATIC_FIELDS_AND_VALUES = clz
				.getDeclaredMethod("getValueFieldMapByStaticFieldsAndValues", Field[].class, int[].class))
				.setAccessible(true);
		//
		(METHOD_PUT = clz.getDeclaredMethod("put", Map.class, Object.class, Object.class)).setAccessible(true);
		//
		(METHOD_SET_DROP_TARGET = clz.getDeclaredMethod("setDropTarget", Component.class, DropTarget.class))
				.setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private DataFlavor[] transferDataFlavors = null;

		private Object transferData = null;

		private Stream<?> stream = null;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String methodName = method != null ? method.getName() : null;
			//
			if (proxy instanceof Transferable) {
				//
				if (Objects.equals(methodName, "getTransferDataFlavors")) {
					//
					return transferDataFlavors;
					//
				} else if (Objects.equals(methodName, "getTransferData")) {
					//
					return transferData;
					//
				} // if
					//
			} else if (proxy instanceof Stream) {
				//
				if (Objects.equals(methodName, "filter")) {
					//
					return stream;
					//
				} // if
					//
			} else if (proxy instanceof ListCellRenderer) {
				//
				if (Objects.equals(methodName, "getListCellRendererComponent")) {
					//
					return null;
					//
				} // if
					//
			} // if
				//
			throw new Throwable(methodName);
			//
		}

	}

	private ClassInJarReplacer instance = null;

	private IH ih = null;

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
		ih = new IH();
		//
	}

	@Test
	void testCast() throws Throwable {
		//
		Assertions.assertNull(cast(null, null));
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
	void testActionPerformed() throws Throwable {
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
	void testDrop() throws Throwable {
		//
		Assertions.assertDoesNotThrow(() -> drop(instance, null));
		//
		final DropTarget dtFile = GraphicsEnvironment.isHeadless()
				? cast(DropTarget.class, Narcissus.allocateInstance(DropTarget.class))
				: new DropTarget();
		//
		FieldUtils.writeDeclaredField(instance, "dtFile", dtFile, true);
		//
		final Method method = DropTarget.class.getDeclaredMethod("createDropTargetContext");
		//
		Assertions.assertDoesNotThrow(() -> drop(instance, new DropTargetDropEvent(
				cast(DropTargetContext.class, Narcissus.invokeObjectMethod(dtFile, method)), new Point(), 0, 0)));
		//
	}

	private static void drop(final DropTargetListener instance, final DropTargetDropEvent dtde) {
		if (instance != null) {
			instance.drop(dtde);
		}
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
		Assertions.assertDoesNotThrow(() -> updateZipEntry(filePomXml, null, null, null, null));
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
		Assertions.assertDoesNotThrow(() -> updateZipEntry(fileZip, null, null, null, null));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(fileZip, null, filePomXml, null, null));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(null, null, null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(new File("."), null, null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(filePomXml, null, Collections.emptyList(), null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> updateZipEntry(null, null, Collections.singleton(null), null, null, 0));
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

	private static void updateZipEntry(final File fileJar, final JTextComponent jtcResult, final File file,
			final JTextComponent jtcAddedOrUpdated, final Number compressionMethod) throws Throwable {
		try {
			METHOD_UPDATE_ZIP_ENTRY5.invoke(null, fileJar, jtcResult, file, jtcAddedOrUpdated, compressionMethod);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static void updateZipEntry(final File file, final File fileJar, final Collection<ZipEntry> collection,
			final JTextComponent jtcResult, final JTextComponent jtcAddedOrUpdated, final int cm) throws Throwable {
		try {
			METHOD_UPDATE_ZIP_ENTRY6.invoke(null, file, fileJar, collection, jtcResult, jtcAddedOrUpdated, cm);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testAddJavaClassIntoZipFile() throws IOException {
		//
		Assertions.assertDoesNotThrow(() -> addJavaClassIntoZipFile(null, null, null, 0));
		//
		Assertions.assertDoesNotThrow(() -> addJavaClassIntoZipFile(new File("."), null, null, 0));
		//
		final Class<?> c = Class.class;
		//
		try (final InputStream is = c
				.getResourceAsStream(String.format("/%1$s.class", StringUtils.replace(c.getName(), ".", "/")))) {
			//
			final File file = File.createTempFile(RandomStringUtils.randomAlphanumeric(3), null);
			//
			if (file != null) {
				//
				file.deleteOnExit();
				//
			} // if
				//
				//
			FileUtils.copyInputStreamToFile(is, file);
			//
			Assertions.assertDoesNotThrow(() -> addJavaClassIntoZipFile(file, null, null, 0));
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
			Assertions.assertDoesNotThrow(() -> addJavaClassIntoZipFile(file, fileZip, null, 0));
			//
		} // try
			//
	}

	private static void addJavaClassIntoZipFile(final File file, final File fileJar, final JTextComponent jtc,
			final int cm) throws Throwable {
		try {
			METHOD_ADD_JAVA_CLASS_INTO_ZIP_FILE.invoke(null, file, fileJar, jtc, cm);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetList() throws Throwable {
		//
		final Transferable transferable = Reflection.newProxy(Transferable.class, ih);
		//
		Assertions.assertNull(getList(transferable));
		//
		if (ih != null) {
			//
			ih.transferDataFlavors = new DataFlavor[] { null };
			//
		} // if
			//
		Assertions.assertNull(getList(transferable));
		//
		if (ih != null) {
			//
			ih.transferDataFlavors = new DataFlavor[] { DataFlavor.allHtmlFlavor, DataFlavor.javaFileListFlavor };
			//
		} // if
			//
		Assertions.assertNull(getList(transferable));
		//
		Assertions.assertSame(ih.transferData = Collections.emptyList(), getList(transferable));
		//
	}

	private static List<?> getList(final Transferable transferable) throws Throwable {
		try {
			final Object obj = METHOD_GET_LIST.invoke(null, transferable);
			if (obj == null) {
				return null;
			} else if (obj instanceof List) {
				return (List) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testAddDropTargetListener() {
		//
		Assertions.assertDoesNotThrow(() -> addDropTargetListener(
				cast(DropTarget.class, Narcissus.allocateInstance(DropTarget.class)), null));
		//
	}

	private static void addDropTargetListener(final DropTarget instance, final DropTargetListener dropTargetListener)
			throws Throwable {
		try {
			METHOD_ADD_DROP_TARGET_LISTENER.invoke(null, instance, dropTargetListener);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testFilter() throws Throwable {
		//
		Assertions.assertNull(filter(stream(null), null));
		//
		Assertions.assertNull(filter(Stream.empty(), null));
		//
		final Stream<?> stream = Reflection.newProxy(Stream.class, ih);
		//
		Assertions.assertSame(ih.stream = stream, filter(stream, null));
		//
	}

	@Test
	void testToList() throws Throwable {
		//
		Assertions.assertNull(toList(null));
		//
	}

	private static <T> Stream<T> stream(final Collection<T> instance) throws Throwable {
		try {
			final Object obj = METHOD_STREAM.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Stream) {
				return (Stream) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate)
			throws Throwable {
		try {
			final Object obj = METHOD_FILTER.invoke(null, instance, predicate);
			if (obj == null) {
				return null;
			} else if (obj instanceof Stream) {
				return (Stream) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T> List<T> toList(final Stream<T> instance) throws Throwable {
		try {
			final Object obj = METHOD_TO_LIST.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof List) {
				return (List) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetName() throws Throwable {
		//
		Assertions.assertNull(getName((Member) null));
		//
		Assertions.assertNull(getName((File) null));
		//
		Assertions.assertNull(getName((ZipEntry) null));
		//
		final String string = "string";
		//
		Assertions.assertEquals(string, getName(new ZipEntry(string)));
		//
	}

	private static String getName(final Member instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_NAME_MEMBER.invoke(null, instance);
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

	private static String getName(final File instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_NAME_FILE.invoke(null, instance);
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

	private static String getName(final ZipEntry instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_NAME_ZIP_ENTRY.invoke(null, instance);
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
	void testSetText() {
		//
		Assertions.assertDoesNotThrow(() -> setText(new JTextField(), null));
		//
	}

	private static void setText(final JTextComponent instance, final String text) throws Throwable {
		try {
			METHOD_SET_TEXT.invoke(null, instance, text);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testContainsKey() throws Throwable {
		//
		Assertions.assertFalse(containsKey((Map) null, null));
		//
		Assertions.assertTrue(containsKey(Collections.singletonMap(null, null), null));
		//
		Assertions.assertFalse(containsKey((Multimap) null, null));
		//
		Assertions.assertFalse(containsKey(ImmutableListMultimap.of(), null));
		//
		final Object key = "";
		//
		Assertions.assertTrue(containsKey(ImmutableListMultimap.of(key, key), key));
		//
	}

	private static boolean containsKey(final Multimap<?, ?> instance, final Object key) throws Throwable {
		try {
			final Object obj = METHOD_CONTAINS_KEY_MULTI_MAP.invoke(null, instance, key);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(obj != null && obj.getClass() != null ? obj.getClass().toString() : null);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) throws Throwable {
		try {
			final Object obj = METHOD_CONTAINS_KEY_MAP.invoke(null, instance, key);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(obj != null && obj.getClass() != null ? obj.getClass().toString() : null);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGet() throws Throwable {
		//
		Assertions.assertNull(get(null, null));
		//
		Assertions.assertEquals(Collections.emptyList(), get(ImmutableListMultimap.of(), null));
		//
	}

	private static <K, V> Collection<V> get(final Multimap<K, V> instance, final K key) throws Throwable {
		try {
			final Object obj = METHOD_GET.invoke(null, instance, key);
			if (obj == null) {
				return null;
			} else if (obj instanceof Collection) {
				return (Collection) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testTestAndAccept() {
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(null, null, null));
		//
		final Predicate<?> predicate = Predicates.alwaysTrue();
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(predicate, null, null));
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(predicate, null, Consumers.nop()));
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(null, null, null, null));
		//
		final BiPredicate<?, ?> biPredicate = Predicates.biAlwaysTrue();
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(biPredicate, null, null, null));
		//
		Assertions.assertDoesNotThrow(() -> testAndAccept(biPredicate, null, null, (a, b) -> {
		}));
		//
	}

	private static <T> void testAndAccept(final Predicate<T> predicate, final T value, final Consumer<T> consumer)
			throws Throwable {
		try {
			METHOD_TEST_AND_ACCEPT3.invoke(null, predicate, value, consumer);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T, U, E extends Throwable> void testAndAccept(final BiPredicate<T, U> predicate, final T t,
			final U u, final FailableBiConsumer<T, U, E> consumer) throws Throwable {
		try {
			METHOD_TEST_AND_ACCEPT4.invoke(null, predicate, t, u, consumer);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetValue() throws Throwable {
		//
		Assertions.assertNull(getValue(null));
		//
	}

	private static Number getValue(final ConstantPushInstruction instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_VALUE.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Number) {
				return (Number) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetInstructions() throws Throwable {
		//
		Assertions.assertNull(getInstructions(null));
		//
	}

	private static Instruction[] getInstructions(final InstructionList instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_INSTRUCTIONS.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Instruction[]) {
				return (Instruction[]) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetConstantPool() throws Throwable {
		//
		Assertions.assertNull(getConstantPool(null));
		//
	}

	private static ConstantPool getConstantPool(final JavaClass instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_CONSTANT_POOL.invoke(null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof ConstantPool) {
				return (ConstantPool) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetMethod() throws Throwable {
		//
		Assertions.assertNull(getMethod(null, null));
		//
	}

	private static org.apache.bcel.classfile.Method getMethod(final JavaClass instance, final Method method)
			throws Throwable {
		try {
			final Object obj = METHOD_GET_METHOD.invoke(null, instance, method);
			if (obj == null) {
				return null;
			} else if (obj instanceof org.apache.bcel.classfile.Method) {
				return (org.apache.bcel.classfile.Method) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testSetEditable() {
		//
		Assertions.assertDoesNotThrow(() -> setEditable(false, null, null, (JTextComponent[]) null));
		//
	}

	private static void setEditable(final boolean flag, final JTextComponent a, final JTextComponent b,
			final JTextComponent... bs) throws Throwable {
		try {
			METHOD_SET_EDITABLE.invoke(null, flag, a, b, bs);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetClassName() throws Throwable {
		//
		Assertions.assertNull(getClassName((StackTraceElement) null));
		//
		Assertions.assertNull(getClassName((JavaClass) null));
		//
	}

	private static String getClassName(final StackTraceElement instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_CLASS_NAME_STACK_TRACE_ELEMENT.invoke(null, instance);
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

	private static String getClassName(final JavaClass instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_CLASS_NAME_JAVA_CLASS.invoke(null, instance);
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
	void testExists() throws Throwable {
		//
		Assertions.assertFalse(exists(new File("non_exists")));
		//
	}

	private static boolean exists(final File instance) throws Throwable {
		try {
			final Object obj = METHOD_EXISTS.invoke(null, instance);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetSelectedItem() throws Throwable {
		//
		Assertions.assertNull(getSelectedItem(new JComboBox<>()));
		//
	}

	private static Object getSelectedItem(final JComboBox<?> instance) throws Throwable {
		try {
			return METHOD_GET_SELECTED_ITEM.invoke(null, instance);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetAbsolutePath() throws Throwable {
		//
		Assertions.assertNotNull(getAbsolutePath(new File(".")));
		//
	}

	private static String getAbsolutePath(final File instance) throws Throwable {
		try {
			final Object obj = METHOD_GET_ABSOLUTE_PATH.invoke(null, instance);
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
	void testIntValue() throws Throwable {
		//
		final int one = 1;
		//
		final Integer ONE = Integer.valueOf(one);
		//
		Assertions.assertEquals(ONE, intValue(ONE, one));
		//
	}

	private static int intValue(final Number instance, final int defaultValue) throws Throwable {
		try {
			final Object obj = METHOD_INT_VALUE.invoke(null, instance, defaultValue);
			if (obj instanceof Integer) {
				return ((Integer) obj).intValue();
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testIsSelected() throws Throwable {
		//
		Assertions.assertFalse(isSelected(null));
		//
		final AbstractButton ab = new JCheckBox();
		//
		Assertions.assertFalse(isSelected(ab));
		//
		ab.setSelected(true);
		//
		Assertions.assertTrue(isSelected(ab));
		//
	}

	private static boolean isSelected(final AbstractButton instance) throws Throwable {
		try {
			final Object obj = METHOD_IS_SELECTED.invoke(null, instance);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetListCellRendererComponent() throws Throwable {
		//
		Assertions.assertNull(getListCellRendererComponent(null, null, null, 0, false, false));
		//
		final ListCellRenderer<?> listCellRenderer = Reflection.newProxy(ListCellRenderer.class, ih);
		//
		Assertions.assertNull(getListCellRendererComponent(listCellRenderer, null, null, 0, false, false));
		//
	}

	private static <E> Component getListCellRendererComponent(final ListCellRenderer<E> instance,
			final JList<? extends E> list, final E value, final int index, final boolean isSelected,
			final boolean cellHasFocus) throws Throwable {
		try {
			final Object obj = METHOD_GET_LIST_CELL_RENDERER_COMPONENT.invoke(null, instance, list, value, index,
					isSelected, cellHasFocus);
			if (obj == null) {
				return null;
			} else if (obj instanceof Component) {
				return (Component) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetValueFieldMapByStaticFieldsAndValues() throws Throwable {
		//
		Assertions.assertNull(getValueFieldMapByStaticFieldsAndValues(null, null));
		//
		Assertions.assertNull(getValueFieldMapByStaticFieldsAndValues(new Field[] { null }, null));
		//
		Assertions.assertNull(
				getValueFieldMapByStaticFieldsAndValues(new Field[] { Boolean.class.getDeclaredField("TRUE") }, null));
		//
	}

	private static Map<Object, Field> getValueFieldMapByStaticFieldsAndValues(final Field[] fs, final int[] objects)
			throws Throwable {
		try {
			final Object obj = METHOD_GET_VALUE_FIELD_MAP_BY_STATIC_FIELDS_AND_VALUES.invoke(null, fs, objects);
			if (obj == null) {
				return null;
			} else if (obj instanceof Map) {
				return (Map) obj;
			}
			throw new Throwable(toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testPut() {
		//
		Assertions.assertDoesNotThrow(() -> put(null, null, null));
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) throws Throwable {
		try {
			METHOD_PUT.invoke(null, instance, key, value);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testSFetDropTarget() {
		//
		Assertions.assertDoesNotThrow(() -> setDropTarget(null, null));
		//
		Assertions.assertDoesNotThrow(() -> setDropTarget(new JTextField(), null));
		//
	}

	private static void setDropTarget(final Component instance, final DropTarget dt) throws Throwable {
		try {
			METHOD_SET_DROP_TARGET.invoke(null, instance, dt);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testEmptyMethods() throws Throwable {
		//
		final Class<?> clz = ClassInJarReplacer.class;
		//
		JavaClass javaClass = null;
		//
		try (final InputStream is = clz
				.getResourceAsStream(String.format("/%1$s.class", StringUtils.replace(clz.getName(), ".", "/")))) {
			//
			javaClass = new ClassParser(is, null).parse();
			//
		} catch (final IOException e) {
			//
			e.printStackTrace();
			//
		} // try
			//
		final org.apache.bcel.classfile.Method[] ms = javaClass != null ? javaClass.getMethods() : null;
		//
		org.apache.bcel.classfile.Method m = null;
		//
		ConstantPoolGen cpg = null;
		//
		Instruction[] ins = null;
		//
		RETURN r = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ms[i]) == null) {
				//
				continue;
				//
			} // if
				//
			if (cpg == null) {
				//
				cpg = new ConstantPoolGen(getConstantPool(javaClass));
				//
			} // if
				//
			if ((ins = getInstructions(new MethodGen(m, null, cpg).getInstructionList())) != null && ins.length == 1
					&& (r = cast(RETURN.class, ins[0])) != null && Objects.equals(Type.VOID, r.getType())) {
				//
				getMethodsByName(getDeclaredMethods(clz), m != null ? m.getName() : null).forEach(x -> {
					//
					if (x == null) {
						//
						return;
						//
					} // if
						//
					if (x.getParameterCount() == 1) {
						//
						Assertions.assertDoesNotThrow(() -> x.invoke(instance, (Object) null));
						//
					} // if
						//
				});
				//
			} // if
				//
		} // for
			//
	}

	private static Method[] getDeclaredMethods(final Class<?> instance) {
		return instance != null ? instance.getDeclaredMethods() : null;
	}

	@Test
	void testInnerClass() throws Throwable {
		//
		final Class<?> clz = ClassInJarReplacer.class;
		//
		final ProtectionDomain pd = clz.getProtectionDomain();
		//
		final CodeSource cs = pd != null ? pd.getCodeSource() : null;
		//
		final URL location = cs != null ? cs.getLocation() : null;
		//
		final File file = new File(location != null ? location.toURI() : null);
		//
		final File[] fs = file.listFiles();
		//
		File f = null;
		//
		JavaClass javaClass = null;
		//
		ConstantPoolGen cpg = null;
		//
		org.apache.bcel.classfile.Method[] ms = null;
		//
		org.apache.bcel.classfile.Method m = null;
		//
		for (int i = 0; fs != null && i < fs.length; i++) {
			//
			if ((f = fs[i]) == null || f.getName() == null || !f.getName().matches("^[^$]+\\$\\d+.class$")) {
				//
				continue;
				//
			} // if
				//
			try (final InputStream is = new FileInputStream(f)) {
				//
				javaClass = new ClassParser(is, null).parse();
				//
			} catch (final IOException e) {
				//
				e.printStackTrace();
				//
			} // try
				//
			ms = javaClass != null ? javaClass.getMethods() : null;
			//
			m = null;
			//
			cpg = null;
			//
			for (int j = 0; ms != null && j < ms.length; j++) {
				//
				if ((m = ms[j]) == null) {
					//
					continue;
					//
				} // if
					//
				if (cpg == null) {
					//
					cpg = new ConstantPoolGen(getConstantPool(javaClass));
					//
				} // if
					//
				final Class<?> c = Class.forName(getClassName(javaClass));
				//
				getMethodsByName(getDeclaredMethods(c), m != null ? m.getName() : null).forEach(x -> {
					//
					if (x == null) {
						//
						return;
						//
					} // if
						//
					final Class<?>[] parameterTypes = x.getParameterTypes();
					//
					Class<?> parameterType = null;
					//
					List<Object> parameters = null;
					//
					for (int k = 0; parameterTypes != null && k < parameterTypes.length; k++) {
						//
						if ((parameterType = parameterTypes[k]) == null) {
							//
							continue;
							//
						} // if
							//
						if (Objects.equals(parameterType, Integer.TYPE)) {
							//
							add(parameters = ObjectUtils.getIfNull(parameters, ArrayList::new), Integer.valueOf(0));
							//
						} else if (Objects.equals(parameterType, Boolean.TYPE)) {
							//
							add(parameters = ObjectUtils.getIfNull(parameters, ArrayList::new), Boolean.FALSE);
							//
						} else {
							//
							add(parameters = ObjectUtils.getIfNull(parameters, ArrayList::new), null);
							//
						} // if
							//
					} // for
						//
					final Object[] objects = parameters != null ? parameters.toArray() : null;
					//
					if (!Modifier.isStatic(x.getModifiers())) {
						//
						Assertions.assertDoesNotThrow(
								() -> Narcissus.invokeMethod(Narcissus.allocateInstance(c), x, objects));
						//
					} // if
						//
				});
				//
			} // for
				//
		} // for
			//
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static List<Method> getMethodsByName(final Method[] ms, final String name) {
		//
		return Arrays.stream(ms).filter(m -> m != null && Objects.equals(m.getName(), name)).toList();
		//
	}

	@Test
	void testZipEntryCallbackImpl() throws Throwable {
		//
		final Class<?> clz = Class.forName("ClassInJarReplacer$ZipEntryCallbackImpl");
		//
		final Constructor<?> constructor = clz != null ? clz.getDeclaredConstructor() : null;
		//
		if (constructor != null) {
			//
			constructor.setAccessible(true);
			//
		} // if
			//
		final ZipEntryCallback zipEntryCallback = constructor != null
				? cast(ZipEntryCallback.class, constructor.newInstance())
				: null;
		//
		Assertions.assertDoesNotThrow(() -> process(zipEntryCallback, null, null));
		//
		Assertions.assertDoesNotThrow(() -> process(zipEntryCallback, null, null));
		//
		final Method method = clz != null ? clz.getDeclaredMethod("put", Multimap.class, Object.class, Object.class)
				: null;
		//
		if (method != null) {
			//
			method.setAccessible(true);
			//
		} // if
			//
		Assertions.assertNull(method != null ? method.invoke(zipEntryCallback, null, null, null) : null);
		//
	}

	private static void process(final ZipEntryCallback instance, final InputStream in, final ZipEntry zipEntry)
			throws IOException {
		if (instance != null) {
			instance.process(in, zipEntry);
		}
	}

}