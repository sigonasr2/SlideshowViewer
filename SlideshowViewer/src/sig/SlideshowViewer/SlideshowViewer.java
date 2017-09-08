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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.apache.commons.io.FileUtils;

public class SlideshowViewer {
	
	public static String slideshowFolderPath = "./slideshow/";
	public static String outputFolderPath = "./";
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
	public static String currentDisplayedFile = "";
	public static boolean randomness = true;
	public static Checkbox random = null;
	public static long currentTick = 0;
	public static long nextImageChange = 0;
	public static int slideshowDelay = 5;
	public static int slideshowMarker = 0;
	static Timer programClock = new Timer(1000,new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			
			performStep();
			
		}
	});
	public static ActionListener buttonListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			if (button.getText().contains("Start")) {
				System.out.println("Running slideshow...");
				SelectImage();
				button.setText("Stop Slideshow");  
			} else {
				System.out.println("Stopping slideshow...");
				button.setText("Start Slideshow");
			}
        }
	};
	public static ActionListener decreaseDelayListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			int currentdelay = 5;
			try {
				currentdelay = Integer.parseInt(delayAmtBox.getText());
			} catch (NumberFormatException ex) {
				currentdelay = 5;
			}
			
			currentdelay = Math.max(currentdelay-1, 1);
			delayAmtBox.setText(Integer.toString(currentdelay));
			slideshowDelay = currentdelay*60;
        }
	};
	public static ActionListener increaseDelayListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			int currentdelay = 5;
			try {
				currentdelay = Integer.parseInt(delayAmtBox.getText());
			} catch (NumberFormatException ex) {
				currentdelay = 5;
			}
			
			currentdelay = Math.min(currentdelay+1, 120);
			delayAmtBox.setText(Integer.toString(currentdelay));
			slideshowDelay = currentdelay*60;
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
	public static ActionListener outputdir_buttonListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			JFileChooser chooser = new JFileChooser();
			File path = new File(outputFolderPath);
			if (path.exists()) {
				chooser.setCurrentDirectory(path);
			} else {
				chooser.setCurrentDirectory(new File("."));
			}
			chooser.setDialogTitle("Load Slideshow Images Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				outputFolderPath = chooser.getSelectedFile().getAbsolutePath()+"/";
				try {
					outputpath_label.setText("Output Location: "+new File(outputFolderPath).getCanonicalPath());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
        }
	};

	public static void SelectImage() {
		String selectedFile = slideshowDataFiles[(slideshowMarker++)%slideshowDataFiles.length];
		if (random.getState()) {
			selectedFile = slideshowDataFiles[(int)(Math.random()*slideshowDataFiles.length)];
		}
		currentDisplayedFile = slideshowFolderPath+selectedFile;
		nextImageChange = currentTick + slideshowDelay;
		System.out.println("Selected image "+selectedFile+". Next change in "+slideshowDelay+" seconds.");
		ChangeSlideshowImage();
	}  
	
	private static void ChangeSlideshowImage() {
		File filer = new File("./slideshow_image.png");
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
		System.out.println("Slideshow Viewer v1.0 Started.");
		
		programClock.start();
		
		File config_file = new File("config_slideshow.txt");
		File slideshowdirectory = null;
		if (!config_file.exists()) {
			System.out.println("\nConfiguration file does not exist...Creating one.");
			CreateAndSaveConfigurationFile();
		} else {
			LoadConfigurationFile();
		}
		slideshowdirectory = new File(slideshowFolderPath);
		
		if (slideshowdirectory!=null && slideshowdirectory.exists()) {
			System.out.println(" Loading slideshow data...");
			if (!LoadSlideshowData(slideshowdirectory)) {
				return;
			}
		} else {
			System.out.println("\nSlideshow data does not exist...Creating a slideshow directory...");
			slideshowdirectory = new File("./slideshow");
			slideshowdirectory.mkdirs();
			slideshowDataFiles = new String[]{};
			try {
				System.out.println("\nPlease insert files into the "+slideshowdirectory.getCanonicalPath()+" directory!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		JFrame f = new JFrame("SlideshowViewer v1.0");
		
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
		
		panel.setLayout(new BoxLayout(panel,BoxLayout.LINE_AXIS));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        
        button = new JButton("Start Slideshow");
        button.addActionListener(buttonListener);
        
        JLabel label = new JLabel();
        label.setText("   Delay (min): ");
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
        	outputpath_label.setText("Output Folder: "+new File(outputFolderPath).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        slideshowdir_button = new JButton("Change");
        slideshowdir_button.setMaximumSize(new Dimension(32,24));
        slideshowdir_button.addActionListener(slidershowdir_buttonListener);
        outputpath_button = new JButton("Change");
        outputpath_button.setMaximumSize(new Dimension(32,24));
        outputpath_button.addActionListener(outputdir_buttonListener);
        
        JLabel label2 = new JLabel();
        label2.setText("    ");
        label2.setMinimumSize(new Dimension(32,10));
        
        random = new Checkbox("Random?",randomness);
        
        panel.add(button);
        //panel.add(Box.createRigidArea(new Dimension(0,2)));
        //panel.setMinimumSize(new Dimension(36,36));
        panel.setSize(480, 36);
        //panel.setBounds(0,0,72,36);
        
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
        
        container.add(panel);
        container.add(panel2);
        container.add(panel3);
        
        f.add(container);
        //f.pack();
        f.setLocationRelativeTo(null);
        f.setBounds(0, 0, 480, 120);
        f.pack();
	}

	protected static void performStep() {
		DetectDirectoryChange();
		if (button.getText().contains("Stop")) {
			currentTick++;
			
			if (currentTick >= nextImageChange) {
				SelectImage();
			}
		}
		int currentdelay = 5;
		try {
			currentdelay = Integer.parseInt(delayAmtBox.getText());
		} catch (NumberFormatException ex) {
			currentdelay = -1;
		}
		if (currentdelay>120 || currentdelay<=0) {
			currentdelay = 5;
			delayAmtBox.setText(Integer.toString(currentdelay));
		}
		slideshowDelay = currentdelay*60;
	}

	private static void DetectDirectoryChange() {
		File dir = new File(slideshowFolderPath);
		if (dir.list().length!=slideshowDataFiles.length) {
			System.out.println("  A change has been detected in the slideshow directory! Reloading images...");
			LoadSlideshowData(dir);
		}
	}

	private static boolean LoadSlideshowData(File dir) {
		String[] filelist = dir.list();
		if (filelist.length==0) {
			System.out.println("Could not find any files to load!");
			try {
				System.out.println("\nPlease insert files into the "+dir.getCanonicalPath()+" directory!");
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
		sig.SlideshowViewer.FileUtils.logToFile("OUTPUT_PATH="+outputFolderPath, "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("RANDOM="+Boolean.toString((random!=null)?random.getState():true)+"", "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("DELAY="+slideshowDelay+"", "config_slideshow.txt");
		sig.SlideshowViewer.FileUtils.logToFile("MARKER="+slideshowMarker+"", "config_slideshow.txt");
	}
	
	private static void LoadConfigurationFile() {
		File configFile = new File("config_slideshow.txt");
		if (configFile.exists()) {
			String[] data = sig.SlideshowViewer.FileUtils.readFromFile("config_slideshow.txt");
			int counter = 0;
			for (String line : data) {
				String split = line.split("=")[1];
				switch (counter++) {
					case 0:{
						slideshowFolderPath = split;
					}break;
					case 1:{
						outputFolderPath = split;
					}break;
					case 2:{
						randomness = Boolean.parseBoolean(split);
					}break;
					case 3:{
						slideshowDelay = Integer.parseInt(split)/60;
					}break;
					case 4:{
						slideshowMarker = Integer.parseInt(split);
					}break;
					default:
						return;
				}
			}
		} else {
			System.out.println("Could not load from Config file!");
		}
	}
}
