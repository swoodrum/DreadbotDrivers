package scott.dreadbot.components;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class DreadbotUtils {
	@SuppressWarnings("unchecked")
	public static ArrayList<String> getCOMPorts() {
		ArrayList<String> ports = new ArrayList<String>();
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
				.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier portId = portEnum.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				getLogger().debug("Found serial port: " + portId.getName());
				ports.add(portId.getName());
			}
		}
		return ports;
	}
	
	private static Logger getLogger() {
		return Logger.getLogger(DreadbotUtils.class);
	}
}
