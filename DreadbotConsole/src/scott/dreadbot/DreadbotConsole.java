package scott.dreadbot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jfxtras.labs.scene.control.gauge.SimpleBattery;
import jfxtras.labs.scene.control.gauge.SimpleIndicator;
import jfxtras.labs.scene.control.gauge.SimpleIndicatorBuilder;

import org.apache.log4j.Logger;

import scala.concurrent.duration.Duration;
import scott.dreadbot.components.CanvasPanel;
import scott.dreadbot.components.DreadbotUtils;
import scott.dreadbot.components.ExitHandler;
import scott.dreadbot.components.GamePadControllerBroker;
import scott.dreadbot.components.SerialPortBroker;
import scott.dreadbot.components.SpringUtils;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.google.common.base.Optional;

public class DreadbotConsole {

	private static final int TICK = 40;
	private static final int CHECK_GAMEPAD_AFTER = 3000;
	private static CanvasPanel canvasPanel;
	private static JFrame frame;
	private static ItemListener serialPortItemHandler;
	private static double servoBatteryCharge;
	private static double cpuBatteryCharge;
	private final static SimpleBattery servoBattery = new SimpleBattery();
	private final static SimpleBattery cpuBattery = new SimpleBattery();
	private static SimpleIndicator serialConnectedIndicator;
	private static SimpleIndicator servoControllerConnectedIndicator;
	private static SimpleIndicator gamepadConnectedIndicator;
	private static ActorRef serialPortProxy;
	private static ActorRef messageReceiver;
	private static ActorRef gamepadProxy;

	private static ActorSystem actorSystem;

	private static final Color[] STATE_COLORS = { Color.rgb(180, 180, 180),
			Color.rgb(180, 0, 0), Color.rgb(180, 180, 0), Color.rgb(0, 180, 0),
			Color.rgb(0, 0, 180) };

	static MyExitHandler exitHandler = new MyExitHandler();
	private static TextField statusField;

	// private static JMenuBar menuBar;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
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
			getLogger().debug(
					">>> Created message receiver: " + messageReceiver.path());
			gamepadProxy = actorSystem.actorOf(new Props(
					new UntypedActorFactory() {
						private static final long serialVersionUID = 7854797553034288076L;

						@Override
						public Actor create() throws Exception {
							return new GamePadControllerBroker();
						}
					}), "gamepad");
			getLogger().debug(
					">>> Created gamepad proxy: " + gamepadProxy.path());
			actorSystem.scheduler().schedule(Duration.Zero(),
					Duration.create(TICK, TimeUnit.MILLISECONDS), gamepadProxy,
					"tick", actorSystem.dispatcher());
			actorSystem.scheduler().scheduleOnce(
					Duration.create(CHECK_GAMEPAD_AFTER, TimeUnit.MILLISECONDS),
					gamepadProxy, "gamepad up?", actorSystem.dispatcher());

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
		gamepadConnectedIndicator = SimpleIndicatorBuilder.create()
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
				Label gamepadConInd = new Label(SpringUtils
						.getSimpleMessage("gamepad.indicator.label"));
				gp.add(gamepadConInd, 2, 2);
				gp.add(gamepadConnectedIndicator, 3, 2);
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
			canvasPanel.closeDown();
			getLogger().debug("Stopping timer...");
			getLogger().debug("Exiting application...");
			frame.dispose();
			if (Optional.fromNullable(serialPortProxy).isPresent()) {
				actorSystem.stop(serialPortProxy);
			}
			if (Optional.fromNullable(gamepadProxy).isPresent()) {
				actorSystem.stop(gamepadProxy);
			}
			actorSystem.shutdown();
			System.exit(0);
		}

	}

	private static class MessageReceiver extends UntypedActor {

		LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		@Override
		public void onReceive(Object message) throws Exception {
			// log.debug(message.toString());
			if (message.equals("serialport up")) {
				serialConnectedIndicator.setInnerColor(STATE_COLORS[3]
						.brighter());
			} else if (message.equals("serialport down")) {
				serialConnectedIndicator.setInnerColor(STATE_COLORS[1]
						.brighter());
			} else if (message.equals("gamepad up")) {
				log.debug(message.toString());
				if (Optional.fromNullable(gamepadConnectedIndicator)
						.isPresent()) {
					gamepadConnectedIndicator.setInnerColor(STATE_COLORS[3]
							.brighter());
				}

			}
		}

	}

	private static class SerialPortItemHandler implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getItem();
				final String portId = item.getText();
				serialPortProxy = actorSystem.actorOf(new Props(
						new UntypedActorFactory() {
							private static final long serialVersionUID = 4741780784339054509L;

							@Override
							public Actor create() throws Exception {
								return new SerialPortBroker(portId);
							}
						}), "serialPortProxy");
				getLogger().debug(serialPortProxy.path());
			} else {
				actorSystem.stop(serialPortProxy);
			}

		}

	}

}
