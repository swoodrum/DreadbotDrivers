package scott.dreadbot.components;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;

public class CanvasPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = -442188894858504211L;
	private static final Dimension PANEL_SIZE = new Dimension(640, 480);
	private volatile boolean isRunning;
	// private JFrame parent;
	private BufferedImage image = null; // current webcam snap
	private OpenCVFrameGrabber grabber;

	public CanvasPanel() {
		// this.parent = parent;
		//setBackground(Color.white);
		setPreferredSize(PANEL_SIZE);
		new Thread(this).start();
	}

	@Override
	public void run() {
		getLogger().debug("Starting frame grabber...");
		grabber = new OpenCVFrameGrabber(CV_CAP_ANY);

		try {
			grabber.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferedImage im = null;
		isRunning = true;
		while (isRunning) {
			try {
				im = grabber.grab().getBufferedImage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (im == null) {
				getLogger().debug("problem getting image...");
			} else {
				image = im;
				repaint();
			}

		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if (image != null)
			g2.drawImage(image, 0, 0, this);
	}

	public void closeDown()
	{
		getLogger().debug("Stopping frame grabber...");
		isRunning = false;
		try {
			grabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	private static Logger getLogger() {
		return Logger.getLogger(CanvasPanel.class);
	}

}
