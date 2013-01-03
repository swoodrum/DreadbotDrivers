package scott.dreadbot;

import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jfxtras.labs.scene.control.gauge.SimpleBattery;
import jfxtras.labs.scene.control.gauge.SimpleIndicator;
import jfxtras.labs.scene.control.gauge.SimpleIndicatorBuilder;

import org.apache.log4j.Logger;

import scott.dreadbot.components.CanvasPanel;
import scott.dreadbot.components.DreadbotUtils;
import scott.dreadbot.components.ExitHandler;
import scott.dreadbot.components.GamePadController;
import scott.dreadbot.components.SerialPortBroker;
import scott.dreadbot.components.SpringUtils;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

public class DreadbotConsole {

	private static final int DELAY = 40;
	private static CanvasPanel canvasPanel;
	private static GamePadController controller;
	private static JFrame frame;
	private static SerialPort serialPort;
	private static Timer pollTimer;
	private static ItemListener serialPortItemHandler;
	private static double servoBatteryCharge;
	private static double cpuBatteryCharge;
	private final static SimpleBattery servoBattery = new SimpleBattery();
	private final static SimpleBattery cpuBattery = new SimpleBattery();
	private static SimpleIndicator serialConnectedIndicator;
	private static SimpleIndicator servoControllerConnectedIndicator;
	private static ActorRef serialPortProxy;
	private static ActorRef messageReceiver;

	private static ActorSystem actorSystem;

	private static final Color[] STATE_COLORS = { Color.rgb(180, 180, 180),
			Color.rgb(180, 0, 0), Color.rgb(180, 180, 0), Color.rgb(0, 180, 0),
			Color.rgb(0, 0, 180) };

	private static final String[] STATE_TEXTS = { "System offline",
			"Mission critical", "Warning", "All systems nominal", "Undefined" };

	static MyExitHandler exitHandler = new MyExitHandler();
	private static TextField statusField;

	// private static JMenuBar menuBar;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			controller = new GamePadController();
			serialPortItemHandler = new SerialPortItemHandler();
			actorSystem = ActorSystem.create("DreadbotActors");
			messageReceiver = actorSystem.actorOf(new Props(
					new UntypedActorFactory() {
						private static final long serialVersionUID = 4741780784339054509L;

						@Override
						public Actor create() throws Exception {
							return new MessageReceiver();
						}
					}), "receiver");
			getLogger().debug(">>> Created message receiver: " + messageReceiver.path());

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.getMessage(),
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
			// System.exit(1);
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}

	private static void createAndShowGUI() {

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			getLogger().warn(e);
			// If Nimbus is not available, fall back to cross-platform
			try {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex) {
				getLogger().warn(ex);
			}
		}

		frame = new JFrame(SpringUtils.getSimpleMessage("frame.title"));
		frame.setIconImage(SpringUtils.getIconFromResource(
				SpringUtils.getSimpleMessage("window.icon.image")).getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.addWindowListener(exitHandler);
		buildMenus();
		createAndStartPollTimer();
		buildCamPanel();
		buildJfxPanel();
		buildStatusPanel();
		frame.pack();
		frame.setVisible(true);

	}

	private static void buildStatusPanel() {
		statusField = new TextField();
		statusField.setEditable(false);
		statusField.setBackground(frame.getBackground());
		frame.getContentPane().add(statusField);
	}

	private static void buildCamPanel() {
		JPanel camPanel = new JPanel();
		camPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(SpringUtils
						.getSimpleMessage("canvas.panel.title")), BorderFactory
				.createEmptyBorder(1, 1, 1, 1)));
		camPanel.setLayout(new BorderLayout(5, 5));
		canvasPanel = new CanvasPanel();
		camPanel.add(BorderLayout.CENTER, canvasPanel);
		frame.getContentPane().add(camPanel);

	}

	private static void createAndStartPollTimer() {
		pollTimer = new Timer(DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.poll();
				int compassDir = controller.getHatDir();
				if (compassDir != GamePadController.NONE)
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

	}

	private static void buildMenus() {
		JMenuBar menuBar = new JMenuBar();
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
			mItem.addItemListener(serialPortItemHandler);
			serialMenu.add(mItem);
		}
		toolMenu.add(serialMenu);
		JMenu videoMenu = new JMenu(
				SpringUtils.getSimpleMessage("menu.tools.menu.video"));
		videoMenu.setIcon(SpringUtils.getIconFromResource(SpringUtils
				.getSimpleMessage("menu.tools.menu.video.icon")));
		JCheckBoxMenuItem vStartItem = new JCheckBoxMenuItem(
				SpringUtils
						.getSimpleMessage("menu.tools.menu.video.item.start"));
		vStartItem.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					canvasPanel.startUp();
				} else {
					canvasPanel.closeDown();
				}

			}
		});
		videoMenu.add(vStartItem);
		toolMenu.add(videoMenu);
		menuBar.add(toolMenu);
		frame.setJMenuBar(menuBar);
	}

	private static void buildJfxPanel() {
		JPanel telemetryPanel = new JPanel();
		telemetryPanel.setLayout(new BorderLayout(5, 5));
		telemetryPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(SpringUtils
						.getSimpleMessage("telemetry.panel.title")),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		final JFXPanel jfxPanel = new JFXPanel();
		serialConnectedIndicator = SimpleIndicatorBuilder.create()
				.innerColor(STATE_COLORS[1].brighter())
				.outerColor(STATE_COLORS[1].darker()).glowVisible(false)
				.prefHeight(15).prefWidth(15).build();
		servoControllerConnectedIndicator = SimpleIndicatorBuilder.create()
				.innerColor(STATE_COLORS[1].brighter())
				.outerColor(STATE_COLORS[1].darker()).glowVisible(false)
				.prefHeight(15).prefWidth(15).build();
		Platform.runLater(new Runnable() {

			private GridPane gp = new GridPane();

			@Override
			public void run() {
				Label servBatLabel = new Label(SpringUtils
						.getSimpleMessage("servo.battery.label"));
				servoBattery.setPrefSize(50, 50);
				servoBattery.setRotate(270.0);
				servoBattery.setScaleX(.75);
				servoBattery.setScaleY(.75);
				cpuBattery.setPrefSize(50, 50);
				cpuBattery.setRotate(270.0);
				cpuBattery.setScaleX(.75);
				cpuBattery.setScaleY(.75);
				servoBattery.setChargingLevel(servoBatteryCharge);
				cpuBattery.setChargingLevel(cpuBatteryCharge);
				gp.setHgap(5);
				gp.setVgap(5);
				gp.add(servBatLabel, 0, 0);
				gp.add(servoBattery, 1, 0);
				Label cpuBatLabel = new Label(SpringUtils
						.getSimpleMessage("cpu.battery.label"));
				gp.add(cpuBatLabel, 0, 1);
				gp.add(cpuBattery, 1, 1);
				Label serConInd = new Label(SpringUtils
						.getSimpleMessage("serial.indicator.label"));
				gp.add(serConInd, 2, 0);
				gp.add(serialConnectedIndicator, 3, 0);
				Label servoConInd = new Label(SpringUtils
						.getSimpleMessage("servo.controller.label"));
				gp.add(servoConInd, 2, 1);
				gp.add(servoControllerConnectedIndicator, 3, 1);
				jfxPanel.setScene(SceneBuilder.create().root(gp).build());

			}
		});
		telemetryPanel.setPreferredSize(new Dimension(frame.getWidth(), 200));
		telemetryPanel.add(jfxPanel, BorderLayout.CENTER);
		frame.getContentPane().add(telemetryPanel);
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
			/*
			 * getLogger().debug("Closing serial port..."); if (serialPort !=
			 * null) { serialPort.removeEventListener(); serialPort.close(); }
			 */
			canvasPanel.closeDown();
			getLogger().debug("Stopping timer...");
			pollTimer.stop();
			getLogger().debug("Exiting application...");
			frame.dispose();
			actorSystem.stop(serialPortProxy);
			actorSystem.shutdown();
			System.exit(0);
		}

	}

	private static class MessageReceiver extends UntypedActor {

		@Override
		public void onReceive(Object arg0) throws Exception {
			getLogger().debug(arg0);
		}

	}

	private static class SerialPortItemHandler implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getItem();
				final String portId = item.getText();
				// serialPort = DreadbotUtils.getSerialPort(portId);
				serialPortProxy = actorSystem.actorOf(new Props(
						new UntypedActorFactory() {
							private static final long serialVersionUID = 4741780784339054509L;

							@Override
							public Actor create() throws Exception {
								return new SerialPortBroker(portId);
							}
						}), "serialPortProxy");
				getLogger().debug(serialPortProxy.path());
				/*
				 * InputStream in; try { in = serialPort.getInputStream();
				 * serialPort.addEventListener(new SerialReader(in));
				 * serialPort.notifyOnDataAvailable(true);
				 * serialConnectedIndicator.setInnerColor(STATE_COLORS[3]
				 * .brighter()); } catch (Exception e1) { getLogger().fatal(e1);
				 * e1.printStackTrace(); }
				 */

			} else {
				/*
				 * if (serialPort != null) { serialPort.removeEventListener();
				 * serialPort.close(); } serialPort = null;
				 * serialConnectedIndicator.setInnerColor(STATE_COLORS[1]
				 * .brighter());
				 */
				actorSystem.stop(serialPortProxy);
			}

		}

	}

}
