package scott.dreadbot.components;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class ServoStatusPanel extends JPanel {

	private static final long serialVersionUID = 8978577951957470131L;
	private JSlider slider;

	public ServoStatusPanel(String borderTitle) {
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(borderTitle),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		
		slider = new JSlider(JSlider.HORIZONTAL, 0, 3000, 1500);
		slider.setMajorTickSpacing(1500);
		slider.setMinorTickSpacing(100);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));
		add(slider);
	}

	public JSlider getSlider() {
		return slider;
	}

}
