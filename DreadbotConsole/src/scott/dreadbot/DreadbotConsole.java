package scott.dreadbot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DreadbotConsole {

	private static ApplicationContext context;
	private static JFrame frame;

	// private static JMenuBar menuBar;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}

	private static void createAndShowGUI() {

		frame = new JFrame(getSimpleMessage("frame.title"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		// build menus
		JMenu fileMenu = new JMenu(getSimpleMessage("menu.file"));
		menuBar.add(fileMenu);
		JMenuItem exitItem = new JMenuItem(
				getSimpleMessage("menu.file.item.exit"));
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.getLogger(this.getClass()).info(
						getSimpleMessage("app.exiting"));
				System.exit(0);
			}
		});
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		// Add components to the frame in proper order
		frame.setJMenuBar(menuBar);
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private static String getSimpleMessage(String message) {
		return context.getMessage(message, null, Locale.getDefault());
	}

}
