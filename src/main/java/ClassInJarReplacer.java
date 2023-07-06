import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.zip.ZipEntry;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.apache.bcel.classfile.ClassParser;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

import net.miginfocom.swing.MigLayout;

public class ClassInJarReplacer extends JFrame implements DropTargetListener, ActionListener {

	private static final long serialVersionUID = -2027349858511726609L;

	private JPanel jpFileJar, jpFile = null;

	private DropTarget dtFileJar, dtFile = null;

	private File fileJar, file = null;

	private JComboBox<?> jcbCompressionLevel = null;

	private JTextComponent jtfFileJar, jtfFile, jtfResult = null;

	private AbstractButton jcbAuto, jbExecute = null;

	private ClassInJarReplacer() {
	}

	private void init() throws TooManyListenersException {
		//
		final int span = 4;
		//
		add(new JLabel("Jar"), String.format("span %1$s", span));
		//
		final String wrap = "wrap";
		//
		add(new JLabel("File"), wrap);
		//
		// File Drop Area
		//
		final String growx = "growx";
		//
		final int width = 300;
		//
		final int height = 300;
		//
		add(jpFileJar = new JPanel(), String.format("wmin %1$s,hmin %2$s,%3$s,span %4$s", width, height, growx, span));
		//
		final Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
		//
		jpFileJar.setBorder(border);
		//
		jpFileJar.setDropTarget(dtFileJar = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null));
		//
		dtFileJar.addDropTargetListener(this);
		//
		add(jpFile = new JPanel(), String.format("wmin %1$s,hmin %2$s,%3$s,%4$s", width, height, growx, wrap));
		//
		jpFile.setBorder(border);
		//
		jpFile.setDropTarget(dtFile = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null));
		//
		dtFile.addDropTargetListener(this);
		//
		// File Path
		//
		add(jtfFileJar = new JTextField(), String.format("span %1$s,%2$s", span, growx));
		//
		add(jtfFile = new JTextField(), String.format("%1$s,%2$s", growx, wrap));
		//
		// Button
		//
		add(jcbAuto = new JCheckBox("Auto"));
		//
		add(jcbCompressionLevel = new JComboBox<>(
				Arrays.stream(getCompressionMethods()).boxed().toArray(Integer[]::new)));
		//
		jcbCompressionLevel.setToolTipText("Compression Method");
		//
		add(jbExecute = new JButton("Execute"));
		//
		jbExecute.addActionListener(this);
		//
		add(jtfResult = new JTextField(), String.format("%1$s,wmin %2$s", growx, 50));
		//
		setEditable(false, jtfFileJar, jtfFile, jtfResult);
		//
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
			method = (javaClass = new ClassParser(is, null).parse()) != null
					? javaClass.getMethod(ZipEntry.class.getDeclaredMethod("setMethod", Integer.TYPE))
					: null;
			//
		} catch (final IOException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
			//
		final MethodGen mg = new MethodGen(method, null,
				new ConstantPoolGen(javaClass != null ? javaClass.getConstantPool() : null));
		//
		final InstructionList il = mg.getInstructionList();
		//
		final Instruction[] ins = il != null ? il.getInstructions() : null;
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
				final ConstantPushInstruction cpi = i > 1 ? cast(ConstantPushInstruction.class, ins[i - 1]) : null;
				//
				final Number value = cpi != null ? cpi.getValue() : null;
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
		final ClassInJarReplacer instance = new ClassInJarReplacer();
		//
		instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		instance.setLayout(new MigLayout());
		//
		instance.init();
		//
		instance.pack();
		//
		instance.setVisible(true);
		//
	}

	public void dragEnter(final DropTargetDragEvent dtde) {
	}

	public void dragOver(final DropTargetDragEvent dtde) {
	}

	public void dropActionChanged(final DropTargetDragEvent dtde) {
	}

	public void dragExit(final DropTargetEvent dte) {
	}

	public void drop(final DropTargetDropEvent dtde) {
		//
		final Object source = dtde != null ? dtde.getSource() : null;
		//
		if (Objects.equals(source, dtFileJar)) {
			//
			acceptDrop(dtde, DnDConstants.ACTION_COPY_OR_MOVE);
			//
			final File file = getFile(getList(getTransferable(dtde)));
			//
			final String absolutePath = file != null ? file.getAbsolutePath() : null;
			//
			if (!file.exists()) {
				//
				JOptionPane.showMessageDialog(null, String.format("%1$s not exist", absolutePath));
				//
				return;
				//
			} else if (!file.isFile()) {
				//
				JOptionPane.showMessageDialog(null, String.format("%1$s is not a regular file", absolutePath));
				//
				return;
				//
			} // if
				//
			setText(jtfFileJar, getAbsolutePath(fileJar = file));
			//
		} else if (Objects.equals(source, dtFile)) {
			//
			acceptDrop(dtde, DnDConstants.ACTION_COPY_OR_MOVE);
			//
			final File file = getFile(getList(getTransferable(dtde)));
			//
			final String absolutePath = file != null ? file.getAbsolutePath() : null;
			//
			if (!file.exists()) {
				//
				JOptionPane.showMessageDialog(null, String.format("%1$s not exist", absolutePath));
				//
				return;
				//
			} else if (!file.isFile()) {
				//
				JOptionPane.showMessageDialog(null, String.format("%1$s is not a regular file", absolutePath));
				//
				return;
				//
			} // if
				//
			setText(jtfFile, getAbsolutePath(this.file = file));
			//
			if (jcbAuto != null && jcbAuto.isSelected()) {
				//
				updateZipEntry(fileJar, jtfResult, file, cast(Number.class, getSelectedItem(jcbCompressionLevel)));
				//
			} // if
				//
		} // if
			//
	}

	private static Object getSelectedItem(final JComboBox<?> instance) {
		return instance != null ? instance.getSelectedItem() : null;
	}

	private static String getAbsolutePath(final File instance) {
		return instance != null ? instance.getAbsolutePath() : null;
	}

	private static File getFile(final List<?> list) {
		//
		if (list == null || list.isEmpty()) {
			//
			JOptionPane.showMessageDialog(null, "Pleaes drop a file");
			//
			return null;
			//
		} else if (list.size() > 1) {
			//
			JOptionPane.showMessageDialog(null, "Only one file should be dropped");
			//
			return null;
			//
		} // if
			//
		final Object object = list.get(0);
		//
		if (object == null) {
			//
			JOptionPane.showMessageDialog(null, "Please drop null");
			//
			return null;
			//
		} else if (!(object instanceof File)) {
			//
			JOptionPane.showMessageDialog(null, String.format("You have dropped %1$s", object.getClass()));
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
		return instance != null ? instance.getTransferable() : null;
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
				} catch (final UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		if (Objects.equals(evt != null ? evt.getSource() : null, this.jbExecute)) {
			//
			updateZipEntry(fileJar, jtfResult, file, cast(Number.class, getSelectedItem(jcbCompressionLevel)));
			//
		} // if
			//
	}

	private static void updateZipEntry(final File fileJar, final JTextComponent jtc, final File file,
			final Number compressionMethod) {
		//
		setText(jtc, null);
		//
		if (fileJar == null || !fileJar.exists() || !fileJar.isFile()) {
			//
			JOptionPane.showMessageDialog(null, "Please drap a Jar File");
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
		} else if (!Objects.equals(ci.getMimeType(), "application/zip")) {
			//
			JOptionPane.showMessageDialog(null, "Please drop a ZIP file");
			//
			return;
			//
		} // if
			//
		if (file == null || !file.exists() || !file.isFile()) {
			//
			JOptionPane.showMessageDialog(null, "Please drap a File");
			//
			return;
			//
		} // if
			//
		final ZipEntryCallbackImpl zecb = new ZipEntryCallbackImpl();
		//
		ZipUtil.iterate(fileJar, zecb);
		//
		final Multimap<String, ZipEntry> names = zecb.entries;
		//
		final Collection<ZipEntry> collection = names != null && names.containsKey(file.getName())
				? names.get(file.getName())
				: null;
		//
		final int cm = intValue(compressionMethod, 0);
		//
		if (collection == null || collection.isEmpty()) {
			//
			try {
				//
				ci = new ContentInfoUtil().findMatch(file);
				//
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
				//
			if (Objects.equals(ci != null ? ci.getMimeType() : null, "application/x-java-applet")) {
				//
				JavaClass javaClass = null;
				//
				try (final InputStream is = new FileInputStream(file)) {
					//
					javaClass = new ClassParser(is, null).parse();
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
						ZipUtil.addEntry(fileJar,
								String.format("%1$s.class", StringUtils.replace(javaClass.getClassName(), ".", "/")),
								FileUtils.readFileToByteArray(file), cm);
						//
						setText(jtc, Boolean.toString(true));
						//
					} catch (final IOException e) {
						//
						setText(jtc, Boolean.toString(false));
						//
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // try
						//
				} // if
					//
			} // if
				//
		} else if (collection.size() == 1) {
			//
			try {
				//
				setText(jtc, Boolean.toString(ZipUtil.replaceEntry(fileJar, getName(IterableUtils.get(collection, 0)),
						FileUtils.readFileToByteArray(file), cm)));
				//
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
				//
		} // if
			//
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
			if ((entries = ObjectUtils.getIfNull(entries, () -> LinkedListMultimap.create())) != null) {
				//
				final String name = zipEntry != null ? zipEntry.getName() : null;
				//
				entries.put(StringUtils.defaultIfBlank(StringUtils.substringAfterLast(name, "/"), name), zipEntry);
				//
			} // if
				//
		}

	}

}