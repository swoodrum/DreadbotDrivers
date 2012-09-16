package scott.dreadbot;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import scott.dreadbot.components.DreadbotUtils;
import scott.dreadbot.components.ExitHandler;
import scott.dreadbot.components.GamePadController;
import scott.dreadbot.components.ServoGridPanel;
import scott.dreadbot.components.SpringUtils;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class DreadbotConsole {

	private static volatile boolean isCapRunning = false;

	private static final int DELAY = 40;
	private static GamePadController controller;
	private static JFrame frame;
	private static ServoGridPanel servoGridPanel;
	private static Timer pollTimer;

	// private static JMenuBar menuBar;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			controller = new GamePadController();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.getMessage(),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		System.out.println("Starting OpenCV...");
		try {
			CanvasFrame canvas = new CanvasFrame("Camera Capture");
			canvas.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					isCapRunning = false;
				}
			});
			getLogger().debug("Starting frame grabber...");
			OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
			grabber.start();
			isCapRunning = true;
			getLogger().debug("Frame grabber started...");
			IplImage frame;
			while (isCapRunning) {
				if ((frame = grabber.grab()) == null)
					break;
				canvas.showImage(frame);
			}
			getLogger().debug("Stopping frame grabber...");
			grabber.stop();
			getLogger().debug("Frame grabber stopped...");
			canvas.dispose();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static void createAndShowGUI() {

		MyExitHandler exitHandler = new MyExitHandler();
		frame = new JFrame(SpringUtils.getSimpleMessage("frame.title"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout(5, 5));
		frame.addWindowListener(exitHandler);
		JMenuBar menuBar = new JMenuBar();
		// build menus
		JMenu fileMenu = new JMenu(SpringUtils.getSimpleMessage("menu.file"));
		JMenuItem exitItem = new JMenuItem(
				SpringUtils.getSimpleMessage("menu.file.item.exit"));
		exitItem.setIcon(SpringUtils.getIconFromResource(SpringUtils
				.getSimpleMessage("menu.file.item.exit.icon")));
		exitItem.addActionListener(exitHandler);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);
		JMenu toolMenu = new JMenu(SpringUtils.getSimpleMessage("menu.tools"));
		JMenu serialMenu = new JMenu(
				SpringUtils.getSimpleMessage("menu.tools.menu.serial"));
		serialMenu.setIcon(SpringUtils.getIconFromResource(SpringUtils
				.getSimpleMessage("menu.tools.menu.serial.icon")));
		ArrayList<String> ports = DreadbotUtils.getCOMPorts();
		for (Iterator<String> portsIter = ports.iterator(); portsIter.hasNext();) {
			JCheckBoxMenuItem mItem = new JCheckBoxMenuItem(portsIter.next());
			serialMenu.add(mItem);
		}
		toolMenu.add(serialMenu);
		menuBar.add(toolMenu);
		// Build panel components
		servoGridPanel = new ServoGridPanel();
		frame.add(BorderLayout.SOUTH, servoGridPanel);

		// Add components to the frame
		frame.setJMenuBar(menuBar);

		// initialize and start the Timer
		pollTimer = new Timer(DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.poll();
				int compassDir = controller.getHatDir();
				if (compassDir != GamePadController.NONE)
					// getLogger().debug("Compass direction: " + compassDir);
					switch (compassDir) {
					case GamePadController.EAST:
						getLogger().debug("Compass direction: EAST");
						break;
					case GamePadController.WEST:
						getLogger().debug("Compass direction: WEST");
						break;
					case GamePadController.NORTH:
						getLogger().debug("Compass direction: NORTH");
						break;
					case GamePadController.SOUTH:
						getLogger().debug("Compass direction: SOUTH");
						break;
					case GamePadController.NW:
						getLogger().debug("Compass direction: NORTHWEST");
						break;
					case GamePadController.NE:
						getLogger().debug("Compass direction: NORTHEAST");
						break;
					case GamePadController.SW:
						getLogger().debug("Compass direction: SOUTHWEST");
						break;
					case GamePadController.SE:
						getLogger().debug("Compass direction: SOUTHEAST");
						break;
					}
				int xyDir = controller.getXYStickDir();
				if (xyDir != GamePadController.NONE)
					switch (xyDir) {
					case GamePadController.EAST:
						getLogger().debug("XY direction: EAST");
						break;
					case GamePadController.WEST:
						getLogger().debug("XY direction: WEST");
						break;
					case GamePadController.NORTH:
						getLogger().debug("XY direction: NORTH");
						break;
					case GamePadController.SOUTH:
						getLogger().debug("XY direction: SOUTH");
						break;
					case GamePadController.NW:
						getLogger().debug("XY direction: NORTHWEST");
						break;
					case GamePadController.NE:
						getLogger().debug("XY direction: NORTHEAST");
						break;
					case GamePadController.SW:
						getLogger().debug("XY direction: SOUTHWEST");
						break;
					case GamePadController.SE:
						getLogger().debug("XY direction: SOUTHEAST");
						break;
					}
				int zrzDir = controller.getZRZStickDir();
				if (zrzDir != GamePadController.NONE)
					switch (zrzDir) {
					case GamePadController.EAST:
						getLogger().debug("ZRZ direction: EAST");
						break;
					case GamePadController.WEST:
						getLogger().debug("ZRZ direction: WEST");
						break;
					case GamePadController.NORTH:
						getLogger().debug("ZRZ direction: NORTH");
						break;
					case GamePadController.SOUTH:
						getLogger().debug("ZRZ direction: SOUTH");
						break;
					case GamePadController.NW:
						getLogger().debug("ZRZ direction: NORTHWEST");
						break;
					case GamePadController.NE:
						getLogger().debug("ZRZ direction: NORTHEAST");
						break;
					case GamePadController.SW:
						getLogger().debug("ZRZ direction: SOUTHWEST");
						break;
					case GamePadController.SE:
						getLogger().debug("ZRZ direction: SOUTHEAST");
						break;
					}
				boolean[] buttons = controller.getButtons();
				for (int i = 0; i < GamePadController.NUM_BUTTONS; i++) {
					if (buttons[i] == true) {
						getLogger().debug("Button " + (i + 1) + " pressed");
					}
				}
			}
		});

		pollTimer.start();

		// Display the window.
		frame.pack();
		frame.setVisible(true);

	}

	private static Logger getLogger() {
		return Logger.getLogger(DreadbotConsole.class);
	}

	private static class MyExitHandler extends ExitHandler {

		@Override
		public void actionPerformed(ActionEvent e) {
			handleEvent();
		}

		@Override
		public void windowClosing(WindowEvent e) {
			handleEvent();

		}

		private void handleEvent() {
			isCapRunning = false;
			getLogger()
					.debug(SpringUtils.getSimpleMessage("app.timer.exiting"));
			pollTimer.stop();
			getLogger().debug(SpringUtils.getSimpleMessage("app.exiting"));
			frame.dispose();
			System.exit(0);
		}

	}

}
