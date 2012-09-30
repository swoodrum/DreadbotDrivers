package scott.dreadbot.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class SpringPropertyUtils extends PropertyPlaceholderConfigurer {
	private static Map<String, String> properties = new HashMap<String, String>();

	@Override
	protected void loadProperties(Properties props) throws IOException {
		super.loadProperties(props);
		for (final Object key : props.keySet()) {
			properties.put((String) key, props.getProperty((String) key));
		}
	}

	public static String getProperty(final String name) {
		return properties.get(name);
	}

}
