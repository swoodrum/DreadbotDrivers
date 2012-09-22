package scott.dreadbot.components;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class CanvasPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = -442188894858504211L;
	private static final Dimension PANEL_SIZE = new Dimension(640, 480);
	private AtomicBoolean isRunning;
	private BufferedImage image = null; // current webcam snap
	private BufferedImage im = null;
	private AtomicReference<OpenCVFrameGrabber> grabber;

	public CanvasPanel() {
		isRunning = new AtomicBoolean(); // defaults to false
		setPreferredSize(PANEL_SIZE);
	}

	@Override
	public void run() {
		getLogger().debug("Starting frame grabber...");
		grabber = new AtomicReference<OpenCVFrameGrabber>(
				new OpenCVFrameGrabber(CV_CAP_ANY));
		try {
			grabber.get().start();
		} catch (Exception e) {
			getLogger().fatal(e);
		}
		//BufferedImage im = null;
		isRunning.set(true);
		while (isRunning.get()) {
			try {
				IplImage iplImage = grabber.get().grab();
				if (iplImage != null) {
					im = iplImage.getBufferedImage();
					image = im;
					repaint();
				}

			} catch (Exception e) {
				getLogger().debug(e);
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

	public void closeDown() {
		try {
			if (grabber != null) {
				isRunning.getAndSet(false);
				grabber.get().stop();
				grabber.getAndSet(null);
				image = null;
				im = null;
				grabber = null;
				repaint();
			}
		} catch (Exception e) {
			getLogger().debug(e);
		}
	}

	public void startUp() {
		new Thread(this, "CanvasPanel Grabber Thread").start();
	}

	private static Logger getLogger() {
		return Logger.getLogger(CanvasPanel.class);
	}

}
