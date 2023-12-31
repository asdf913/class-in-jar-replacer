import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.meeuw.functional.Predicates;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

import io.github.toolfactory.narcissus.Narcissus;
import net.miginfocom.swing.MigLayout;

public class ClassInJarReplacer extends JFrame implements DropTargetListener, ActionListener {

	private static final long serialVersionUID = -2027349858511726609L;

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	@Note("File Jar")
	private DropTarget dtFileJar = null;

	@Note("File")
	private DropTarget dtFile = null;

	@Note("File Jar")
	private File fileJar = null;

	@Note("File")
	private File file = null;

	private JComboBox<?> jcbCompressionLevel = null;

	@Note("File Jar")
	private JTextComponent jtfFileJar = null;

	@Note("File")
	private JTextComponent jtfFile = null;

	@Note("Added or Updated")
	private JTextComponent jtfAddedOrUpdated = null;

	@Note("Result")
	private JTextComponent jtfResult = null;

	@Note("Auto")
	private AbstractButton jcbAuto = null;

	@Note("Execute")
	private AbstractButton jbExecute = null;

	private ClassInJarReplacer() {
	}

	private void init() throws TooManyListenersException {
		//
		// If "java.awt.Container.component" is null, return this method immediately
		//
		// The below check is for "-Djava.awt.headless=true"
		//
		final List<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(this))),
				f -> Objects.equals(getName(f), "component")));
		//
		final Field f = IterableUtils.size(fs) == 1 ? IterableUtils.get(fs, 0) : null;
		//
		final boolean isGui = f == null || Narcissus.getObjectField(this, f) != null;
		//
		final BiPredicate<Component, Object> biPredicate = Predicates.biAlways(isGui, null);
		//
		final int span = 4;
		//
		testAndAccept(biPredicate, new JLabel("Jar"), String.format("span %1$s", span), this::add);
		//
		final String wrap = "wrap";
		//
		testAndAccept(biPredicate, new JLabel("File"), wrap, this::add);
		//
		// File Drop Area
		//
		final String growx = "growx";
		//
		final int width = 300;
		//
		final int height = 300;
		//
		JPanel jp = null;
		//
		testAndAccept(biPredicate, jp = new JPanel(),
				String.format("wmin %1$s,hmin %2$s,%3$s,span %4$s", width, height, growx, span), this::add);
		//
		final Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
		//
		jp.setBorder(border);
		//
		testAndAccept(Predicates.biAlways(isGui, null), jp,
				dtFileJar = isGui ? new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null) : null,
				ClassInJarReplacer::setDropTarget);
		//
		//
		addDropTargetListener(dtFileJar, this);
		//
		testAndAccept(biPredicate, jp = new JPanel(),
				String.format("wmin %1$s,hmin %2$s,%3$s,%4$s", width, height, growx, wrap), this::add);
		//
		jp.setBorder(border);
		//
		testAndAccept(Predicates.biAlways(isGui, null), jp,
				dtFile = isGui ? new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null) : null,
				ClassInJarReplacer::setDropTarget);
		//
		addDropTargetListener(dtFile, this);
		//
		// File Path
		//
		testAndAccept(biPredicate, jtfFileJar = new JTextField(), String.format("span %1$s,%2$s", span, growx),
				this::add);
		//
		testAndAccept(biPredicate, jtfFile = new JTextField(), String.format("%1$s,%2$s", growx, wrap), this::add);
		//
		// Button
		//
		final Predicate<Component> predicate = Predicates.always(isGui, null);
		//
		testAndAccept(predicate, jcbAuto = new JCheckBox("Auto"), this::add);
		//
		final int[] compressionMethods = getCompressionMethods();
		//
		testAndAccept(predicate, jcbCompressionLevel = new JComboBox<>(
				Arrays.stream(compressionMethods).boxed().toArray(Integer[]::new)), this::add);
		//
		jcbCompressionLevel.setToolTipText("Compression Method");
		//
		final ListCellRenderer<?> listCellRender = jcbCompressionLevel.getRenderer();
		//
		final Map<Object, Field> valueFieldMap = getValueFieldMapByStaticFieldsAndValues(
				ZipEntry.class.getDeclaredFields(), compressionMethods);
		//
		jcbCompressionLevel.setRenderer(new ListCellRenderer<Object>() {

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				//
				if (containsKey(valueFieldMap, value)) {
					//
					return ClassInJarReplacer.getListCellRendererComponent((ListCellRenderer) listCellRender, list,
							String.format("%1$s (%2$s)", getName(MapUtils.getObject(valueFieldMap, value)), value),
							index, isSelected, cellHasFocus);
					//
				}
				//
				return ClassInJarReplacer.getListCellRendererComponent((ListCellRenderer) listCellRender, list, value,
						index, isSelected, cellHasFocus);
				//
			}

		});
		//
		testAndAccept(predicate, jbExecute = new JButton("Execute"), this::add);
		//
		jbExecute.addActionListener(this);
		//
		testAndAccept(biPredicate, jtfResult = new JTextField(), String.format("%1$s,wmin %2$s", growx, 50), this::add);
		//
		testAndAccept(biPredicate, jtfAddedOrUpdated = new JTextField(), String.format("wmin %1$s", 55), this::add);
		//
		setEditable(false, jtfFileJar, jtfFile, jtfResult, jtfAddedOrUpdated);
		//
	}

	private static void setDropTarget(final Component instance, final DropTarget dt) {
		if (instance != null) {
			instance.setDropTarget(dt);
		}
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static <E> Component getListCellRendererComponent(final ListCellRenderer<E> instance,
			final JList<? extends E> list, final E value, final int index, final boolean isSelected,
			final boolean cellHasFocus) {
		return instance != null ? instance.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
				: null;
	}

	private static void addDropTargetListener(final DropTarget instance, final DropTargetListener dropTargetListener)
			throws TooManyListenersException {
		if (instance != null) {
			instance.addDropTargetListener(dropTargetListener);
		}
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <E> Stream<E> stream(final Collection<E> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		//
		return instance != null && (predicate != null || Proxy.isProxyClass(getClass(instance)))
				? instance.filter(predicate)
				: null;
		//
	}

	private static <T> List<T> toList(final Stream<T> instance) {
		return instance != null ? instance.toList() : null;
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <T> void testAndAccept(final Predicate<T> predicate, final T value, final Consumer<T> consumer) {
		if (test(predicate, value) && consumer != null) {
			consumer.accept(value);
		}
	}

	private static final <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, U, E extends Throwable> void testAndAccept(final BiPredicate<T, U> predicate, final T t,
			final U u, final FailableBiConsumer<T, U, E> consumer) throws E {
		if (predicate != null && predicate.test(t, u) && consumer != null) {
			consumer.accept(t, u);
		}
	}

	private static int[] getCompressionMethods() {
		//
		JavaClass javaClass = null;
		//
		Method method = null;
		//
		try (final InputStream is = ZipEntry.class.getResourceAsStream(
				String.format("/%1$s.class", StringUtils.replace(ZipEntry.class.getName(), ".", "/")))) {
			//
			method = getMethod(javaClass = new ClassParser(is, null).parse(),
					ZipEntry.class.getDeclaredMethod("setMethod", Integer.TYPE));
			//
		} catch (final IOException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
			//
		final Instruction[] ins = getInstructions(
				new MethodGen(method, null, new ConstantPoolGen(getConstantPool(javaClass))).getInstructionList());
		//
		Instruction in = null;
		//
		int[] ints = null;
		//
		for (int i = 0; ins != null && i < ins.length; i++) {
			//
			if ((in = ins[i]) instanceof IFEQ) {
				//
				ints = ArrayUtils.add(ints = ObjectUtils.getIfNull(ints, () -> new int[0]), 0);
				//
			} else if (in instanceof IF_ICMPEQ) {
				//
				final Number value = getValue(i > 1 ? cast(ConstantPushInstruction.class, ins[i - 1]) : null);
				//
				if (value != null) {
					//
					ints = ArrayUtils.add(ints = ObjectUtils.getIfNull(ints, () -> new int[0]), value.intValue());
					//
				} // if
					//
			} else if (in instanceof ATHROW) {
				//
				break;
				//
			} // if
				//
		} // for
			//
		Arrays.sort(ints);
		//
		return ints;
		//
	}

	private static Map<Object, Field> getValueFieldMapByStaticFieldsAndValues(final Field[] fs, final int[] objects) {
		//
		Field f = null;
		//
		Map<Object, Field> map = null;
		//
		Object object = null;
		//
		for (int i = 0; fs != null && i < fs.length; i++) {
			//
			if ((f = fs[i]) == null || !Modifier.isStatic(f.getModifiers())) {
				//
				continue;
				//
			} // if
				//
			for (int j = 0; objects != null && j < objects.length; j++) {
				//
				if (!Objects.equals(Narcissus.getStaticField(f), object = objects[j])) {
					//
					continue;
					//
				} // if
					//
				if (containsKey(map, object)) {
					//
					throw new IllegalStateException();
					//
				} // if
					//
				put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), object, f);
				//
			} // for
				//
		} // for
			//
		return map;
		//
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static Number getValue(final ConstantPushInstruction instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static Instruction[] getInstructions(final InstructionList instance) {
		return instance != null ? instance.getInstructions() : null;
	}

	private static ConstantPool getConstantPool(final JavaClass instance) {
		return instance != null ? instance.getConstantPool() : null;
	}

	private static Method getMethod(final JavaClass instance, final java.lang.reflect.Method method) {
		return instance != null ? instance.getMethod(method) : null;
	}

	private static void setEditable(final boolean flag, final JTextComponent a, final JTextComponent b,
			final JTextComponent... bs) {
		//
		setEditable(a, flag);
		//
		setEditable(b, flag);
		//
		for (int i = 0; bs != null && i < bs.length; i++) {
			//
			setEditable(bs[i], flag);
			//
		} //
			//
	}

	private static void setEditable(final JTextComponent instance, final boolean flag) {
		if (instance != null) {
			instance.setEditable(flag);
		}
	}

	public static void main(final String[] args) throws TooManyListenersException {
		//
		final ClassInJarReplacer instance = GraphicsEnvironment.isHeadless()
				? cast(ClassInJarReplacer.class, Narcissus.allocateInstance(ClassInJarReplacer.class))
				: new ClassInJarReplacer();
		//
		if (instance != null) {
			//
			instance.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			//
			instance.setLayout(new MigLayout());
			//
			instance.init();
			//
			if (Boolean.logicalAnd(!GraphicsEnvironment.isHeadless(), !isUnderDebugOrMaven())) {
				//
				instance.pack();
				//
				instance.setVisible(true);
				//
			} // if
				//
		} // if
			//
	}

	private static boolean isUnderDebugOrMaven() {
		//
		return Arrays.stream(new Throwable().getStackTrace())
				.anyMatch(x -> Arrays
						.asList("org.eclipse.jdt.internal.junit5.runner.JUnit5TestReference",
								"org.apache.maven.surefire.junitplatform.JUnitPlatformProvider")
						.contains(getClassName(x)));
		//
	}

	private static String getClassName(final StackTraceElement instance) {
		return instance != null ? instance.getClassName() : null;
	}

	public void dragEnter(final DropTargetDragEvent dtde) {
		//
	}

	public void dragOver(final DropTargetDragEvent dtde) {
		//
	}

	public void dropActionChanged(final DropTargetDragEvent dtde) {
		//
	}

	public void dragExit(final DropTargetEvent dte) {
		//
	}

	public void drop(final DropTargetDropEvent dtde) {
		//
		final Object source = getSource(dtde);
		//
		final boolean isGui = !GraphicsEnvironment.isHeadless();
		//
		final boolean isNotUnderDebugOrMaven = !isUnderDebugOrMaven();
		//
		if (Objects.equals(source, dtFileJar)) {
			//
			acceptDrop(dtde, DnDConstants.ACTION_COPY_OR_MOVE);
			//
			final File f = getFile(getList(getTransferable(dtde)));
			//
			final String absolutePath = getAbsolutePath(f);
			//
			if (!exists(f)) {
				//
				testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null),
						String.format("%1$s not exist", absolutePath), x -> JOptionPane.showMessageDialog(null, x));
				//
				return;
				//
			} else if (!isFile(f)) {
				//
				JOptionPane.showMessageDialog(null, String.format("%1$s is not a regular file", absolutePath));
				//
				return;
				//
			} // if
				//
			setText(jtfFileJar, getAbsolutePath(fileJar = f));
			//
		} else if (Objects.equals(source, dtFile)) {
			//
			acceptDrop(dtde, DnDConstants.ACTION_COPY_OR_MOVE);
			//
			final List<?> list = getList(getTransferable(dtde));
			//
			File f = null;
			//
			String absolutePath = null;
			//
			for (int i = 0; list != null && i < list.size(); i++) {
				//
				if ((f = cast(File.class, list.get(i))) == null) {
					//
					continue;
					//
				} // if
					//
				absolutePath = getAbsolutePath(f);
				//
				if (!exists(f)) {
					//
					testAndAccept(Predicates.always(isGui, null), String.format("%1$s not exist", absolutePath),
							x -> JOptionPane.showMessageDialog(null, x));
					//
					return;
					//
				} else if (!isFile(f)) {
					//
					JOptionPane.showMessageDialog(null, String.format("%1$s is not a regular file", absolutePath));
					//
					return;
					//
				} // if
					//
				setText(jtfFile, getAbsolutePath(file = f));
				//
				if (isSelected(jcbAuto)) {
					//
					updateZipEntry(fileJar, jtfResult, f, jtfAddedOrUpdated,
							cast(Number.class, getSelectedItem(jcbCompressionLevel)));
					//
				} // if
					//
			} // for
				//
		} // if
			//
	}

	private static boolean isSelected(final AbstractButton instance) {
		return instance != null && instance.isSelected();
	}

	private static Object getSource(final EventObject instance) {
		return instance != null ? instance.getSource() : null;
	}

	private static boolean exists(final File instance) {
		return instance != null && instance.exists();
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.isFile();
	}

	private static Object getSelectedItem(final JComboBox<?> instance) {
		return instance != null ? instance.getSelectedItem() : null;
	}

	private static String getAbsolutePath(final File instance) {
		return instance != null ? instance.getAbsolutePath() : null;
	}

	private static File getFile(final List<?> list) {
		//
		final boolean isGui = !GraphicsEnvironment.isHeadless();
		//
		final boolean isNotUnderDebugOrMaven = !isUnderDebugOrMaven();
		//
		if (list == null || list.isEmpty()) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "Please drop a file",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return null;
			//
		} else if (list.size() > 1) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "Only one file should be dropped",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return null;
			//
		} // if
			//
		final Object object = list.get(0);
		//
		if (object == null) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "A null is dropped",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return null;
			//
		} else if (!(object instanceof File)) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null),
					String.format("You have dropped %1$s", getClass(object)),
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return null;
			//
		} // if
			//
		return (File) object;
		//
	}

	private static void acceptDrop(final DropTargetDropEvent instance, final int dropAction) {
		if (instance != null) {
			instance.acceptDrop(dropAction);
		}
	}

	private static Transferable getTransferable(final DropTargetDropEvent instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		try {
			//
			if (Narcissus.getObjectField(instance.getDropTargetContext(),
					DropTargetContext.class.getDeclaredField("dropTargetContextPeer")) == null) {
				//
				return null;
				//
			} // if
				//
		} catch (final NoSuchFieldException e) {
			//
			// TODO Auto-generated catch block
			//
			e.printStackTrace();
			//
		} // try
			//
		return instance.getTransferable();
		//
	}

	private static List<?> getList(final Transferable transferable) {
		//
		final DataFlavor[] dataFlavors = transferable != null ? transferable.getTransferDataFlavors() : null;
		//
		DataFlavor dataFlavour = null;
		//
		List<?> list = null;
		//
		for (int i = 0; dataFlavors != null && i < dataFlavors.length; i++) {
			//
			if ((dataFlavour = dataFlavors[i]) == null
					|| !Objects.equals(List.class, dataFlavour.getRepresentationClass())) {
				//
				continue;
				//
			} // if
				//
			if (list == null) {
				//
				try {
					//
					list = cast(List.class, transferable != null ? transferable.getTransferData(dataFlavour) : null);
					//
				} catch (final UnsupportedFlavorException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // try
					//
			} else {
				//
				throw new IllegalStateException();
				//
			} // if
				//
		} // for
			//
		return list;
		//
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static void setText(final JTextComponent instance, final String text) {
		if (instance != null) {
			instance.setText(text);
		}
	}

	public void actionPerformed(final ActionEvent evt) {
		//
		if (Objects.equals(getSource(evt), this.jbExecute)) {
			//
			updateZipEntry(fileJar, jtfResult, file, jtfAddedOrUpdated,
					cast(Number.class, getSelectedItem(jcbCompressionLevel)));
			//
		} // if
			//
	}

	private static void updateZipEntry(final File fileJar, final JTextComponent jtcResult, final File file,
			final JTextComponent jtcAddedOrUpdated, final Number compressionMethod) {
		//
		setText(jtcResult, null);
		//
		final boolean isGui = !GraphicsEnvironment.isHeadless();
		//
		final boolean isNotUnderDebugOrMaven = !isUnderDebugOrMaven();
		//
		if (Boolean.logicalOr(!exists(fileJar), !isFile(fileJar))) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "Please drap a Jar File",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return;
			//
		} // if
			//
		ContentInfo ci = null;
		//
		try {
			//
			ci = new ContentInfoUtil().findMatch(fileJar);
			//
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
			//
		if (ci == null) {
			//
			JOptionPane.showMessageDialog(null, "ContentInfo could not be detected");
			//
			return;
			//
		} else if (!Objects.equals(getMimeType(ci), "application/zip")) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "Please drop a ZIP file",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return;
			//
		} // if
			//
		if (Boolean.logicalOr(!exists(file), !isFile(file))) {
			//
			testAndAccept(Predicates.always(isGui && isNotUnderDebugOrMaven, null), "Please drop a File",
					x -> JOptionPane.showMessageDialog(null, x));
			//
			return;
			//
		} // if
			//
		final ZipEntryCallbackImpl zecb = new ZipEntryCallbackImpl();
		//
		try {
			//
			ZipUtil.iterate(fileJar, zecb);
			//
		} catch (final Exception e) {
			//
			e.printStackTrace();
			//
		} // try
			//
		final Multimap<String, ZipEntry> names = zecb.entries;
		//
		updateZipEntry(file, fileJar, testAndApply(x -> containsKey(names, x), getName(file), x -> get(names, x), null),
				jtcResult, jtcAddedOrUpdated, intValue(compressionMethod, 0));
		//
	}

	private static void updateZipEntry(final File file, final File fileJar, final Collection<ZipEntry> collection,
			final JTextComponent jtcResult, final JTextComponent jtcAddedOrUpdated, final int cm) {
		//
		ContentInfo ci = null;
		//
		setText(jtcAddedOrUpdated, null);
		//
		if (collection == null || collection.isEmpty()) {
			//
			try {
				//
				ci = testAndApply(f -> exists(f) && isFile(f), file, new ContentInfoUtil()::findMatch, null);
				//
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
				//
			if (Objects.equals(getMimeType(ci), "application/x-java-applet")) {
				//
				addJavaClassIntoZipFile(file, fileJar, jtcResult, cm);
				//
				setText(jtcAddedOrUpdated, "Added");
				//
			} // if
				//
		} else if (collection.size() == 1) {
			//
			try {
				//
				setText(jtcResult, toString(testAndApply(Objects::nonNull,
						testAndApply(Objects::nonNull, file, FileUtils::readFileToByteArray, null),
						x -> ZipUtil.replaceEntry(fileJar, getName(IterableUtils.get(collection, 0)), x, cm), null)));
				//
				setText(jtcAddedOrUpdated, "Updated");
				//
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
				//
		} // if
			//
	}

	private static String toString(final Object instance) {
		return instance != null ? instance.toString() : null;
	}

	private static void addJavaClassIntoZipFile(final File file, final File fileJar, final JTextComponent jtc,
			final int cm) {
		//
		JavaClass javaClass = null;
		//
		try (final InputStream is = testAndApply(f -> exists(f) && isFile(f), file, FileInputStream::new, null)) {
			//
			javaClass = parse(testAndApply(x -> x != null, is, x -> new ClassParser(x, null), null));
			//
		} catch (final Throwable throwable) {
			// TODO Auto-generated catch block
			throwable.printStackTrace();
		} // try
			//
		if (javaClass != null) {
			//
			try {
				//
				testAndAccept((a, b) -> a != null, fileJar, javaClass,
						(a, b) -> ZipUtil.addEntry(a,
								String.format("%1$s.class", StringUtils.replace(getClassName(b), ".", "/")),
								FileUtils.readFileToByteArray(file), cm));
				//
				setText(jtc, Boolean.toString(true));
				//
			} catch (final Exception e) {
				//
				setText(jtc, Boolean.toString(false));
				//
				// TODO Auto-generated catch block
				e.printStackTrace();
				//
			} // try
				//
		} // if
			//
	}

	private static String getClassName(final JavaClass instance) {
		return instance != null ? instance.getClassName() : null;
	}

	private static JavaClass parse(final ClassParser instance) throws IOException, ClassFormatException {
		return instance != null ? instance.parse() : null;
	}

	private static String getMimeType(final ContentInfo instance) {
		return instance != null ? instance.getMimeType() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static String getName(final File instance) {
		return instance != null ? instance.getName() : null;
	}

	private static boolean containsKey(final Multimap<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static <K, V> Collection<V> get(final Multimap<K, V> instance, final K key) {
		return instance != null ? instance.get(key) : null;
	}

	private static int intValue(final Number instance, final int defaultValue) {
		return instance != null ? instance.intValue() : defaultValue;
	}

	private static String getName(final ZipEntry instance) {
		return instance != null ? instance.getName() : null;
	}

	private static class ZipEntryCallbackImpl implements ZipEntryCallback {

		private Multimap<String, ZipEntry> entries = null;

		public void process(final InputStream in, final ZipEntry zipEntry) throws IOException {
			//
			final String name = getName(zipEntry);
			//
			put(entries = ObjectUtils.getIfNull(entries, LinkedListMultimap::create),
					StringUtils.defaultIfBlank(StringUtils.substringAfterLast(name, "/"), name), zipEntry);
			//
		}

		private static <K, V> void put(final Multimap<K, V> instance, final K key, final V value) {
			if (instance != null) {
				instance.put(key, value);
			}
		}

	}

}