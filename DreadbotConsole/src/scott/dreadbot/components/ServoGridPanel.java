package scott.dreadbot.components;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ServoGridPanel extends JPanel {
	private static final long serialVersionUID = -5357952170521867015L;
	private HashMap<String, ServoStatusPanel> servoPanels;
	private static final int NUM_ROWS = 4;
	private static final int NUM_COLS = 3;

	public ServoGridPanel() {
		super();
		servoPanels = new HashMap<String, ServoStatusPanel>();
		setLayout(new GridLayout(4, 3));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(SpringUtils
						.getSimpleMessage("servo.grid.panel.title")),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		int counter = 1;
		for (int i = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++) {
				String label = SpringUtils.getMessageWithArgs(
						"servo.panel.title", new Object[] { counter },
						Locale.getDefault());
				ServoStatusPanel panel = new ServoStatusPanel(label);
				servoPanels.put("servo" + counter, panel);
				add(panel);
				counter++;
			}
		}
	}

	public HashMap<String, ServoStatusPanel> getServoPanels() {
		return servoPanels;
	}

}
