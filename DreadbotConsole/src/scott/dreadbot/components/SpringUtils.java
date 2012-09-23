package scott.dreadbot.components;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class SpringUtils {
	static ClassPathXmlApplicationContext context;
	static {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	public static String getSimpleMessage(String message) {
		return context.getMessage(message, null, Locale.getDefault());
	} 
	
	public static ImageIcon getIconFromResource(String location) {
		ImageIcon icon = null;
		Resource resource = context.getResource(location);
		try {
			icon = new ImageIcon(resource.getURL());
			getLogger().debug("Loaded icon from URL: " + resource.getURL());
		} catch (IOException e1) {
			getLogger().warn(e1.getMessage());
		}
		return icon;
	}
	
	public static BufferedImage getBufferedImageFromResource(String location) {
		BufferedImage image = null;
		Resource resource = context.getResource(location);
		try {
			File file = resource.getFile();
			image = ImageIO.read(file);
			getLogger().debug("Loaded image from FILE: " + file);
		} catch (IOException e) {
			getLogger().warn(e.getMessage());
		}
		return image;
	}
	
	private static Logger getLogger() {
		return Logger.getLogger(SpringUtils.class);
	}
}
