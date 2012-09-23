package scott.dreadbot.components;

//import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_ANY;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
	private static final int DELAY = 100;
	private static final Dimension PANEL_SIZE = new Dimension(640, 480);
	private static final int CIRCLE_SIZE = 40;
	private static final int LINES_LEN = 60;
	private AtomicBoolean isRunning;
	private BufferedImage image = null; // current webcam snap
	private BufferedImage im = null;
	private BufferedImage crosshairs;
	private AtomicReference<OpenCVFrameGrabber> grabber;
	private Point cogPoint = null;

	public CanvasPanel() {
		isRunning = new AtomicBoolean(); // defaults to false
		setPreferredSize(PANEL_SIZE);
		crosshairs = SpringUtils.getBufferedImageFromResource(SpringUtils
				.getSimpleMessage("image.crosshairs"));
	}

	@Override
	public void run() {
		getLogger().debug("Starting frame grabber...");
		JCVMotionDetector md = null;
		Point pt;
		grabber = new AtomicReference<OpenCVFrameGrabber>(
				new OpenCVFrameGrabber(1)); // CV_CAP_ANY
		try {
			grabber.get().start();
		} catch (Exception e) {
			getLogger().fatal(e);
		}
		isRunning.set(true);
		while (isRunning.get()) {
			try {
				IplImage iplImage = grabber.get().grab();
				if (iplImage != null) {
					im = iplImage.getBufferedImage();
					if (md == null) {
						md = new JCVMotionDetector(im);
					}
					md.calcMove(im);
					pt = md.getCOG();
					if (pt != null) {
						cogPoint = pt;
					}
					image = im;
					repaint();
					try {
						Thread.sleep(DELAY);
					} catch (InterruptedException e) {
						getLogger().debug(e);
					}
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
		if (cogPoint != null)
			drawCrosshairs(g2, cogPoint.x, cogPoint.y); // positioned at COG
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
				cogPoint = null;
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

	private void drawCrosshairs(Graphics2D g2, int xCenter, int yCenter)
	// draw crosshairs graphic or make one from lines and a circle
	{
		if (crosshairs != null)
			g2.drawImage(crosshairs, xCenter - crosshairs.getWidth() / 2,
					yCenter - crosshairs.getHeight() / 2, this);
		else {
			// draw thick red circle and cross-hairs in center
			g2.setColor(Color.RED);
			g2.drawOval(xCenter - CIRCLE_SIZE / 2, yCenter - CIRCLE_SIZE / 2,
					CIRCLE_SIZE, CIRCLE_SIZE);
			g2.drawLine(xCenter, yCenter - LINES_LEN / 2, xCenter, yCenter
					+ LINES_LEN / 2); // vertical line
			g2.drawLine(xCenter - LINES_LEN / 2, yCenter, xCenter + LINES_LEN
					/ 2, yCenter); // horizontal line
		}
	} // end of drawCrosshairs()

}
