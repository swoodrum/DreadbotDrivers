package scott.dreadbot.components;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class CanvasPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -442188894858504211L;

	public CanvasPanel() {
		super();
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(SpringUtils.getSimpleMessage("canvas.panel.title")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	}

}
