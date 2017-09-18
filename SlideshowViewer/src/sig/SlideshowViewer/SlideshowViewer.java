package sig.SlideshowViewer;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.apache.commons.io.FileUtils;

public class SlideshowViewer {
	
	public static String slideshowFolderPath = "./slideshow/";
	public static String outputFilePath = "./slideshow_image.png";
	public static String[] slideshowDataFiles = null;
	public static JLabel slideshowpath_button = null;
	public static JLabel slideshowpath_label = null;
	public static JLabel outputpath_label = null;
	public static JButton outputpath_button = null;
	public static JButton button = null;
	public static JButton arrowbutton1 = null;
	public static JButton arrowbutton2 = null;
	public static JButton slideshowdir_button = null;
	public static JButton outputimage_button = null;
	public static JTextField delayAmtBox = null;
	public static JTextArea debugBox = null;
	public static String currentDisplayedFile = "";
	public static boolean randomness = true;
	public static Checkbox random = null;
	public static long currentTick = 0;
	public static long nextImageChange = 0;
	public static int slideshowDelay = 60;
	public static int slideshowMarker = 0;
	final public static String PROGRAM_VERSION = "1.1";
	public static List<String> debugqueue = new ArrayList<String>();
	static Timer programClock = new Timer(1000,new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			
			performStep();
			
		}
	});
	public static ActionListener buttonListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			if (button.getText().contains("Start")) {
				PrintToSystemAndAddToDebugBox("Running slideshow...");
				SelectImage();
				if (slideshowDataFiles.length>0) {
					button.setText("Stop Slideshow");  
				}
			} else {
				PrintToSystemAndAddToDebugBox("Stopping slideshow...");
				button.setText("Start Slideshow");
			}
        }
	};
	public static ActionListener decreaseDelayListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			int currentdelay = 60;
			try {
				currentdelay = Integer.parseInt(delayAmtBox.getText());
			} catch (NumberFormatException ex) {
				currentdelay = 60;
			}
			
			currentdelay = Math.max(currentdelay-1, 1);
			delayAmtBox.setText(Integer.toString(currentdelay));
			slideshowDelay = currentdelay;
        }
	};
	public static ActionListener increaseDelayListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			int currentdelay = 60;
			try {
				currentdelay = Integer.parseInt(delayAmtBox.getText());
			} catch (NumberFormatException ex) {
				currentdelay = 60;
			}
			
			currentdelay = Math.min(currentdelay+1, 7200);
			delayAmtBox.setText(Integer.toString(currentdelay));
			slideshowDelay = currentdelay;
        }
	};
	public static ActionListener slidershowdir_buttonListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			JFileChooser chooser = new JFileChooser();
			File path = new File(slideshowFolderPath);
			if (path.exists()) {
				chooser.setCurrentDirectory(path);
			} else {
				chooser.setCurrentDirectory(new File("."));
			}
			chooser.setDialogTitle("Load Slideshow Images Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				slideshowFolderPath = chooser.getSelectedFile().getAbsolutePath()+"/";
				try {
					slideshowpath_label.setText("Slideshow Folder: "+new File(slideshowFolderPath).getCanonicalPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
        }
	};
	public static ActionListener outputfile_buttonListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			JFileChooser chooser = new JFileChooser();
			File path = new File(outputFilePath);
			if (path.exists()) {
				chooser.setCurrentDirectory(path);
			} else {
				chooser.setCurrentDirectory(new File("."));
			}
			chooser.setDialogTitle("Load Slideshow Images Directory");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setSelectedFile(path);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				outputFilePath = chooser.getSelectedFile().getAbsolutePath()+"/";
				try {
					outputpath_label.setText("Output File: "+new File(outputFilePath).getCanonicalPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
        }
	};

	public static void SelectImage() {
		if (slideshowDataFiles.length>0) {
			String selectedFile = slideshowDataFiles[(slideshowMarker++)%slideshowDataFiles.length];
			if (random.getState()) {
				selectedFile = slideshowDataFiles[(int)(Math.random()*slideshowDataFiles.length)];
			}
			currentDisplayedFile = slideshowFolderPath+selectedFile;
			nextImageChange = currentTick + slideshowDelay;
			PrintToSystemAndAddToDebugBox("Selected image "+selectedFile+". Next change in "+slideshowDelay+" second"+((slideshowDelay!=1)?"s":"")+".");
			ChangeSlideshowImage();
		} else {
			try {
				PrintToSystemAndAddToDebugBox("\nCount not start slideshow! Please insert images into the "+new File(slideshowFolderPath).getCanonicalPath()+" directory or select a valid slideshow directory!");
				button.setText("Stop Slideshow");  
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  
	
	private static void ChangeSlideshowImage() {
		File filer = new File(outputFilePath);
		if (!filer.exists()) {
			try {
				filer.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileUtils.copyFile(new File(currentDisplayedFile), filer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		PrintToSystemAndAddToQueue("Slideshow Viewer v"+PROGRAM_VERSION+" Started.");
		
		programClock.start();
		
		File config_file = new File("config_slideshow.txt");
		File slideshowdirectory = null;
		if (!config_file.exists()) {
			PrintToSystemAndAddToQueue("\nConfiguration file does not exist...Creating one.");
			CreateAndSaveConfigurationFile();
		} else {
			LoadConfigurationFile();
		}
		slideshowdirectory = new File(slideshowFolderPath);
		
		if (slideshowdirectory!=null && slideshowdirectory.exists()) {
			PrintToSystemAndAddToQueue(" Loading slideshow data...");
			if (!LoadSlideshowData(slideshowdirectory)) {
				return;
			}
		} else {
			PrintToSystemAndAddToQueue("\nSlideshow data does not exist...Creating a slideshow directory...");
			slideshowdirectory = new File("./slideshow");
			slideshowdirectory.mkdirs();
			slideshowDataFiles = new String[]{};
			try {
				PrintToSystemAndAddToQueue("\nPlease insert files into the "+slideshowdirectory.getCanonicalPath()+" directory!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		JFrame f = new JFrame("SlideshowViewer v"+PROGRAM_VERSION);
		
		f.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				CreateAndSaveConfigurationFile();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		JPanel container = new JPanel();
		
		container.setLayout(new BoxLayout(container,BoxLayout.PAGE_AXIS));
		
		JPanel panel = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		
		panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        
        button = new JButton("Start Slideshow");
        button.addActionListener(buttonListener);
        
        JLabel label = new JLabel();
        label.setText("   Delay (sec): ");
        label.setMinimumSize(new Dimension(16,10));
        
        delayAmtBox = new JTextField(2);
        delayAmtBox.setMaximumSize(new Dimension(24,24));
        delayAmtBox.setText(Integer.toString(slideshowDelay));
        
        arrowbutton1 = new JButton("<");
        arrowbutton1.setMaximumSize(new Dimension(48,24));
        arrowbutton1.addActionListener(decreaseDelayListener);
        arrowbutton2 = new JButton(">");
        arrowbutton2.setMaximumSize(new Dimension(48,24));
        arrowbutton2.addActionListener(increaseDelayListener);
        slideshowpath_label = new JLabel("");
        
        try {
        	slideshowpath_label.setText("Slideshow Folder: "+new File(slideshowFolderPath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        outputpath_label = new JLabel("");
        
        try {
        	outputpath_label.setText("Output Folder: "+new File(outputFilePath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        slideshowdir_button = new JButton("Change");
        slideshowdir_button.setMaximumSize(new Dimension(32,24));
        slideshowdir_button.addActionListener(slidershowdir_buttonListener);
        outputpath_button = new JButton("Change");
        outputpath_button.setMaximumSize(new Dimension(32,24));
        outputpath_button.addActionListener(outputfile_buttonListener);
        
        JLabel label2 = new JLabel();
        label2.setText("    ");
        label2.setMinimumSize(new Dimension(32,10));
        
        random = new Checkbox("Random?",randomness);
        
        panel.add(button);
        //panel.add(Box.createRigidArea(new Dimension(0,2)));
        //panel.setMinimumSize(new Dimension(36,36));
        panel.setSize(480, 36);
        //panel.setBounds(0,0,72,36);
        
        debugBox = new JTextArea(8,32);
        debugBox.setEditable(false);
        debugBox.setLineWrap(true);
        debugBox.setAutoscrolls(true);
        for (String s : debugqueue) {
        	debugBox.setText(debugBox.getText()+s+"\n");
        }
        debugqueue.clear();
        debugBox.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(debugBox); 
        scrollPane.setPreferredSize(new Dimension(320,96));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        //f.add(panel);
        
        String[] options = new String[]{};

        panel.add(label);
        panel.add(arrowbutton1);
        panel.add(delayAmtBox);
        panel.add(arrowbutton2);
        panel.add(label2);
        panel.add(random);
        panel2.add(slideshowpath_label);
        panel2.add(slideshowdir_button);
        panel3.add(outputpath_label);
        panel3.add(outputpath_button);
        panel4.add(scrollPane);
        //panel4.add(debugBox);
        
        container.add(panel);
        container.add(panel2);
        container.add(panel3);
        container.add(panel4);
        
        f.add(container);
        //f.pack();
        f.setLocationRelativeTo(null);
        f.setBounds(0, 0, 480, 120);
        f.pack();
	}

	private static void PrintToSystemAndAddToQueue(String string) {
		debugqueue.add(string);
		System.out.println(string);
	}
	
	public static void PrintToSystemAndAddToDebugBox(String message) {
		if (debugBox!=null) {
			debugBox.setText((debugBox.getText().length()>5000?debugBox.getText().substring(debugBox.getText().length()-5000, debugBox.getText().length()-1):debugBox.getText())+message+"\n");
		} else {
			debugqueue.add(message);
		}
        System.out.println(message);
	}
	
	public static void FlushDebugQueue() {
		if (debugBox!=null) {
			for (String s : debugqueue) {
				debugBox.setText((debugBox.getText().length()>5000?debugBox.getText().substring(debugBox.getText().length()-5000, debugBox.getText().length()-1):debugBox.getText())+s+"\n");
			}
			debugqueue.clear();
		}
	}

	protected static void performStep() {
		DetectDirectoryChange();
		if (button.getText().contains("Stop")) {
			currentTick++;
			
			if (currentTick >= nextImageChange) {
				SelectImage();
			}
		}
		int currentdelay = 60;
		try {
			currentdelay = Integer.parseInt(delayAmtBox.getText());
		} catch (NumberFormatException ex) {
			currentdelay = -1;
		}
		if (currentdelay>7200 || currentdelay<=0) {
			currentdelay = 60;
			delayAmtBox.setText(Integer.toString(currentdelay));
		}
		if (debugqueue.size()>0) {
			FlushDebugQueue();
		}
		slideshowDelay = currentdelay;
	}

	private static void DetectDirectoryChange() {
		File dir = new File(slideshowFolderPath);
		String[] list = trimNonImageFiles(dir.list());
		if (list.length!=slideshowDataFiles.length) {
			PrintToSystemAndAddToDebugBox("  A change has been detected in the slideshow directory! Reloading images...");
			LoadSlideshowData(dir);
		}
	}

	private static boolean LoadSlideshowData(File dir) {
		String[] filelist = dir.list();
		filelist = trimNonImageFiles(filelist);
		if (filelist.length==0) {
			PrintToSystemAndAddToDebugBox("Could not find any files to load!");
			try {
				PrintToSystemAndAddToDebugBox("\nPlease insert files into the "+dir.getCanonicalPath()+" directory!");
			} catch (IOException e) {
				e.printStackTrace();
			}
			//return false;
			slideshowDataFiles = new String[]{};
			return true;
		}
		slideshowDataFiles = filelist;
		System.out.print("Found and loaded "+filelist.length+" files!");
		return true;
	}

	private static String[] trimNonImageFiles(String[] files) {
		List<String> finallist = new ArrayList<String>();
		for (String file : files) {
			File f = new File(slideshowFolderPath+file);
			try {
				if (!f.isDirectory() && !f.getCanonicalPath().equalsIgnoreCase(new File(outputFilePath).getCanonicalPath())) {
					finallist.add(file);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return finallist.toArray(new String[finallist.size()]);
	}

	private static void CreateAndSaveConfigurationFile() {
		File configFile = new File("config_slideshow.txt");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		configFile.delete();
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sig.SlideshowViewer.FileUtils.logToFile("SLIDESHOW_PATH="+slideshowFolderPath, "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("OUTPUT_PATH="+outputFilePath, "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("RANDOM="+Boolean.toString((random!=null)?random.getState():true)+"", "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("DELAY="+slideshowDelay+"", "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("MARKER="+slideshowMarker+"", "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("VERSION="+PROGRAM_VERSION+"", "config_slideshow.txt");
	}
	
	private static void LoadConfigurationFile() {
		File configFile = new File("config_slideshow.txt");
		if (configFile.exists()) {
			String[] data = sig.SlideshowViewer.FileUtils.readFromFile("config_slideshow.txt");
			if (data.length==5) {
				System.out.println("\nConfig does not have correct number of data points! Set it up as version 1.0!");
				sig.SlideshowViewer.FileUtils.logToFile("VERSION=1.0", "config_slideshow.txt");
			}
			data = sig.SlideshowViewer.FileUtils.readFromFile("config_slideshow.txt");
			int counter = 0;
			for (String line : data) {
				String split = line.split("=")[1];
				switch (counter++) {
					case 0:{
						slideshowFolderPath = split;
					}break;
					case 1:{
						outputFilePath = split;
						File filer = new File(outputFilePath);
						if (!filer.isFile()) {
							try {
								filer.createNewFile();
							} catch (IOException e) {
								outputFilePath = "./slideshow_image.png";
								e.printStackTrace();
							}
						}
					}break;
					case 2:{
						randomness = Boolean.parseBoolean(split);
					}break;
					case 3:{
						slideshowDelay = Integer.parseInt(split);
					}break;
					case 4:{
						slideshowMarker = Integer.parseInt(split);
					}break;
					case 5:{
						String version = split;
						if (version.equalsIgnoreCase("1.0")) {
							System.out.println("Upgrading to version 1.1...");
							outputFilePath = "./slideshow_image.png";
						}
					}break;
					default:
						return;
				}
			}
		} else {
			PrintToSystemAndAddToDebugBox("Could not load from Config file!");
		}
	}
}
