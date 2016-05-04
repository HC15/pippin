package pippin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MachineView extends Observable {
	private MachineModel model;
	private String defaultDir;
	private String sourceDir;
	private String executableDir;
	private Properties properties = null;
	
	private CodeViewPanel codeViewPanel;
	private MemoryViewPanel memoryViewPanel1;
	private MemoryViewPanel memoryViewPanel2;
	private MemoryViewPanel memoryViewPanel3;
	private ControlPanel controlPanel;
	private ProcessorViewPanel processorPanel;
	private MenuBarBuilder menuBuilder;
	private JFrame frame;
	
	private States state;
	private static final int TICK = 500; // timer tick = 1/2 second
	private boolean autoStepOn = false;
	
	private File currentlyExecutingFile = null;
	private boolean running = false;
	
	public MachineView(MachineModel model) {
		this.model = model;
		locateDefaultDirectory();
		loadPropertiesFile();
		createAndShowGUI();
	}

	public int getData(int index) {
		return model.getData(index);
	}

	public int getProgramCounter() {
		return model.getProgramCounter();
	}

	public int getAccumulator() {
		return model.getAccumulator();
	}

	public int getChangedIndex() {
		return model.getChangedIndex();
	}
	
	public States getState(){
		return this.state;
	}
	
	public Code getCode() {
		return model.getCode();
	}

	public void setRunning(boolean b) {
		running = b;
		if(running) {
			state = States.PROGRAM_LOADED_NOT_AUTOSTEPPING;
		} else {
			autoStepOn = false;
			state = States.PROGRAM_HALTED;
		}
		state.enter();
		setChanged();
		notifyObservers();
	}
	
	public void setAutoStepOn(boolean b) {
		autoStepOn = b;
		if(autoStepOn) {
			state = States.AUTO_STEPPING;
		} else {
			state = States.PROGRAM_LOADED_NOT_AUTOSTEPPING;
		}
		state.enter();
		setChanged();
		notifyObservers();
	}

	public void step() {
		if (model.isRunning()) {
			try {
				model.step();
			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (IndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (DivideByZeroException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			}
			setChanged();
			notifyObservers();
		}
		else {
			halt();
		}
	}
	
	public void halt() {
		setRunning(false);
	}

	public void exit() { // method executed when user exits the program
		int decision = JOptionPane.showConfirmDialog(
				frame, "Do you really wish to exit?",
				"Confirmation", JOptionPane.YES_NO_OPTION);
		if (decision == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	public void clearAll() {
		model.clear();
		state = States.NOTHING_LOADED;
		state.enter();
		setChanged();
		notifyObservers("Clear");
	}
	
	public void toggleAutoStep() {
		setAutoStepOn(!autoStepOn);
	}

	public void reload() {
		clearAll();
		finalLoad_ReloadStep();
	}
	
	/**
	 * Translate method reads a source "pasm" file and saves the
	 * file with the extension "pexe" by collecting the input and output
	 * files and calling Assembler.assemble. If the source has errors 
	 * 
	 */
	public void assembleFile() {
		File source = null;
		File outputExe = null;
		JFileChooser chooser = new JFileChooser(sourceDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Source Files", "pasm");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			source = chooser.getSelectedFile();
		}
		if(source != null && source.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			// WHICH WE WILL ALLOW TO BE DIFFERENT
			sourceDir = source.getAbsolutePath();
			sourceDir = sourceDir.replace('\\','/');
			int lastDot = sourceDir.lastIndexOf('.');
			String outName = sourceDir.substring(0, lastDot + 1) + "pexe";			
			int lastSlash = sourceDir.lastIndexOf('/');
			sourceDir = sourceDir.substring(0, lastSlash + 1);
			outName = outName.substring(lastSlash+1); 
			filter = new FileNameExtensionFilter(
					"Pippin Executable Files", "pexe");
			if(executableDir.equals(defaultDir)) {
				chooser = new JFileChooser(sourceDir);
			} else {
				chooser = new JFileChooser(executableDir);
			}
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(outName));
			int saveOK = chooser.showSaveDialog(null);
			if(saveOK == JFileChooser.APPROVE_OPTION) {
				outputExe = chooser.getSelectedFile();
			}
			if(outputExe != null) {
				executableDir = outputExe.getAbsolutePath();
				executableDir = executableDir.replace('\\','/');
				lastSlash = executableDir.lastIndexOf('/');
				executableDir = executableDir.substring(0, lastSlash + 1);
				try { 
					properties.setProperty("SourceDirectory", sourceDir);
					properties.setProperty("ExecutableDirectory", executableDir);
					properties.store(new FileOutputStream("propertyfile.txt"), 
							"File locations");
				} catch (Exception e) {
					System.out.println("Error writing properties file");
				}
				Map<Integer, String> errors = new TreeMap<>();
				boolean b = Assembler.assemble(source, outputExe, errors); 
				if (b) {
					JOptionPane.showMessageDialog(
							frame, 
							"The source was assembled to an executable",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					StringBuilder builder = new StringBuilder("Errors:\n");
					for(Integer i : errors.keySet()) {
						builder.append(errors.get(i));
						builder.append('\n');
					}
					JOptionPane.showMessageDialog(
							frame, 
							builder.toString(),
							"Failure",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else {// outputExe Still null
				JOptionPane.showMessageDialog(
						frame, 
						"The output file has problems.\n" +
								"Cannot assemble the program",
								"Warning",
								JOptionPane.OK_OPTION);
			}
		} else {// outputExe does not exist
			JOptionPane.showMessageDialog(
					frame, 
					"The source file has problems.\n" +
							"Cannot assemble the program",
							"Warning",
							JOptionPane.OK_OPTION);				
		}
	}

	public void loadFile() {
		JFileChooser chooser = new JFileChooser(executableDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Executable Files", "pexe");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			currentlyExecutingFile = chooser.getSelectedFile();
		}
		if(currentlyExecutingFile != null && currentlyExecutingFile.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			executableDir = currentlyExecutingFile .getAbsolutePath();
			executableDir = executableDir.replace('\\','/');
			int lastSlash = executableDir.lastIndexOf('/');
			executableDir = executableDir.substring(0, lastSlash + 1);
			try { 
				properties.setProperty("SourceDirectory", sourceDir);
				properties.setProperty("ExecutableDirectory", executableDir);
				properties.store(new FileOutputStream("propertyfile.txt"), 
						"File locations");
			} catch (Exception e) {
				System.out.println("Error writing properties file");
			}			
		}
		finalLoad_ReloadStep();
	}
	
	public void finalLoad_ReloadStep() {
		Code code = new Code();
		try {
			clearAll();
			Loader.load(model, code, currentlyExecutingFile);
			model.setCode(code);
			model.setRunning(true);
			setAutoStepOn(false);
			setChanged();
			notifyObservers("Load Code");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					frame,
					"The file being selected has problems.\n" +
							"Cannot load the program",
							"Warning",
							JOptionPane.OK_OPTION);
		}
	}

	public void execute() {
		while (model.isRunning()) {
			try {
				model.step();
			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (IndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			} catch (DivideByZeroException e) {
				JOptionPane.showMessageDialog(
						frame,
						"Program error from line " + getProgramCounter() + "\n"
								+ "Exception message: " + e.getMessage(),
								"Run time error",
								JOptionPane.OK_OPTION);
			}

		}
		setChanged();
		notifyObservers();
	}
	
	private void locateDefaultDirectory() {
		//CODE TO DISCOVER THE ECLIPSE DEFAULT DIRECTORY:
		File temp = new File("propertyfile.txt");
		if(!temp.exists()) {
			PrintWriter out;
			try {
				out = new PrintWriter(temp);
				out.close();
				defaultDir = temp.getAbsolutePath();
				temp.delete();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			defaultDir = temp.getAbsolutePath();
		}
		// change to forward slashes
		defaultDir = defaultDir.replace('\\','/');
		int lastSlash = defaultDir.lastIndexOf('/');
		defaultDir  = defaultDir.substring(0, lastSlash + 1);
	}
	
	private void loadPropertiesFile() {
		try { // load properties file "propertyfile.txt", if it exists
			properties = new Properties();
			properties.load(new FileInputStream("propertyfile.txt"));
			sourceDir = properties.getProperty("SourceDirectory");
			executableDir = properties.getProperty("ExecutableDirectory");
			// CLEAN UP ANY ERRORS IN WHAT IS STORED:
			if (sourceDir == null || sourceDir.length() == 0 
					|| !new File(sourceDir).exists()) {
				sourceDir = defaultDir;
			}
			if (executableDir == null || executableDir.length() == 0 
					|| !new File(executableDir).exists()) {
				executableDir = defaultDir;
			}
		} catch (Exception e) {
			// PROPERTIES FILE DID NOT EXIST
			sourceDir = defaultDir;
			executableDir = defaultDir;
		}		
	}
	
	/**
	 * Method that sets up the whole GUI and locates the individual
	 * components into place. Also sets up the Menu bar. Starts a 
	 * swing timer ticking.
	 */
	private void createAndShowGUI() {
		codeViewPanel = new CodeViewPanel(this);
		memoryViewPanel1 = new MemoryViewPanel(this, 0, 160);
		memoryViewPanel2 = new MemoryViewPanel(this, 160, 240);
		memoryViewPanel3 = new MemoryViewPanel(this, 240, Memory.DATA_SIZE);
		controlPanel = new ControlPanel(this);
		processorPanel = new ProcessorViewPanel(this);
		menuBuilder = new MenuBarBuilder(this);
		frame = new JFrame("Pippin Simulator");
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout(1,1));
		content.setBackground(Color.BLACK);
		frame.setSize(1200,600);
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1,3));

		frame.add(codeViewPanel.createCodeDisplay(),BorderLayout.LINE_START);
		frame.add(center,BorderLayout.CENTER);
		center.add(memoryViewPanel1.createMemoryDisplay());
		center.add(memoryViewPanel2.createMemoryDisplay());
		center.add(memoryViewPanel3.createMemoryDisplay());
		frame.add(controlPanel.createControlDisplay(),BorderLayout.PAGE_END);
		frame.add(processorPanel.createProcessorDisplay(),BorderLayout.PAGE_START);

		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(menuBuilder.createFileMenu());
		bar.add(menuBuilder.createExecuteMenu());

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
		frame.addWindowListener(null); // needs work
		state = States.NOTHING_LOADED;
		state.enter();
		setChanged();
		notifyObservers();
		javax.swing.Timer timer = new javax.swing.Timer(TICK, e -> {if(autoStepOn) step();});
		timer.start();
		frame.setVisible(true);
	}
	
	/**
	 * Main method that drives the whole simulator
	 * @param args command line arguments are not used
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MachineView(new MachineModel(true));
			}
		});
	}
}