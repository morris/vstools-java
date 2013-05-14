package vstools;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.util.JmeFormatter;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.*;

public class GUI {

    public static void main(String[] args) {
	GUI gui = new GUI();
	gui.start();
    }

    public void start() {
	JmeFormatter formatter = new JmeFormatter();

	Handler consoleHandler = new ConsoleHandler();
	consoleHandler.setFormatter(formatter);

	Logger.getLogger("").removeHandler(
		Logger.getLogger("").getHandlers()[0]);
	Logger.getLogger("").addHandler(consoleHandler);

	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	createCanvas();

	try {
	    Thread.sleep(500);
	} catch (InterruptedException ex) {

	}

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		createFrame();

		canvasPanel.add(canvas, BorderLayout.CENTER);
		frame.pack();
		startApp();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	    }
	});
    }

    private void createFrame() {
	frame = new JFrame("VSTOOLS Viewer");
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		storeConfig();
		viewer.stop();
	    }
	});

	canvasPanel = new JPanel();
	canvasPanel.setLayout(new BorderLayout());

	frame.getContentPane().add(canvasPanel);

	createAnimationPanel();

	createMenu();
    }

    private void createCanvas() {
	AppSettings settings = new AppSettings(true);
	settings.setWidth(640);
	settings.setHeight(480);
	settings.setFrameRate(60);

	config = Util.getConfig();

	viewer = new Viewer();
	viewer.setPauseOnLostFocus(false);
	viewer.setSettings(settings);
	viewer.createCanvas();
	viewer.startCanvas();

	context = (JmeCanvasContext) viewer.getContext();
	canvas = context.getCanvas();
	canvas.setSize(settings.getWidth(), settings.getHeight());
    }

    private void createMenu() {
	JMenuBar menuBar = new JMenuBar();
	frame.setJMenuBar(menuBar);

	JMenu menuFile = new JMenu("File");
	menuBar.add(menuFile);

	final JMenuItem itemOpen = new JMenuItem("Open");
	menuFile.add(itemOpen);
	itemOpen.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser(config
			.getProperty("currentDirectory"));
		if (e.getSource() == itemOpen) {
		    int returnVal = fc.showOpenDialog(frame);

		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
			    config.setProperty("currentDirectory", fc
				    .getCurrentDirectory().getCanonicalPath());
			} catch (IOException e1) {
			    e1.printStackTrace();
			}
			File file = fc.getSelectedFile();
			open(file);
		    }
		}
	    }
	});

	JMenuItem itemExit = new JMenuItem("Exit");
	menuFile.add(itemExit);
	itemExit.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		storeConfig();
		frame.dispose();
		viewer.stop();
	    }
	});
    }

    private void createAnimationPanel() {
	JPanel animationPanel = new JPanel();
	animationPanel.setLayout(new BoxLayout(animationPanel,
		BoxLayout.LINE_AXIS));
	JLabel info = new JLabel("Animations ");
	animationPanel.add(info);
	final JButton prev = new JButton("<");
	prev.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == prev) {
		    viewer.prevAnim();
		}
	    }
	});
	animationPanel.add(prev);

	final JButton next = new JButton(">");
	next.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == next) {
		    viewer.nextAnim();
		}
	    }
	});

	animationPanel.add(next);
	canvasPanel.add(animationPanel, BorderLayout.SOUTH);
    }

    private void startApp() {
	viewer.startCanvas();
	viewer.enqueue(new Callable<Void>() {
	    public Void call() {
		viewer.getFlyByCamera().setDragToRotate(true);

		if (!config.getProperty("openZND").equals("")) {
		    open(new File(config.getProperty("openZND")));
		}
		
		if (!config.getProperty("open").equals("")) {
		    open(new File(config.getProperty("open")));
		}
		
		if (!config.getProperty("openSEQ").equals("")) {
		    open(new File(config.getProperty("openSEQ")));
		}
		
		return null;
	    }
	});

    }

    private void storeConfig() {
	Util.storeConfig();
    }

    public void open(File file) {
	String ext = Util.ext(file);

	if (ext.equals("shp")) {

	    viewer.openSHP(file);
	} else if (ext.equals("wep")) {
	    viewer.openWEP(file);
	} else if (ext.equals("seq")) {
	    viewer.openSEQ(file);
	} else if (ext.equals("mpd")) {
	    viewer.openMPD(file);
	} else if (ext.equals("zud")) {
	    viewer.openZUD(file);
	} else if (ext.equals("znd")) {
	    viewer.openZND(file);
	} else if (ext.equals("arm")) {
	    viewer.openARM(file);
	}

	if (ext.equals("seq")) {
	    config.put("openSEQ", file.getAbsolutePath());
	} else if (ext.equals("zud")) {
	    config.put("openSEQ", "");
	} else if (ext.equals("znd")) {
	    config.put("openZND", file.getAbsolutePath());
	} else {
	    config.put("open", file.getAbsolutePath());
	}
    }

    private JmeCanvasContext context;
    private Canvas canvas;
    private Properties config;
    private Viewer viewer;
    private JFrame frame;
    private Container canvasPanel;
}
