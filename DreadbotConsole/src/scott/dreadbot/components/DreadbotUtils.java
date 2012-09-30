package scott.dreadbot.components;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

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

	public static SerialPort getSerialPort(String portId) {
		SerialPort port = null;
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier(portId);
			CommPort commPort = portIdentifier.open(SpringPropertyUtils
					.getProperty("application.name"), Integer
					.parseInt(SpringPropertyUtils.getProperty("port.open.timeout")));
			port = (SerialPort) commPort;
			port.setSerialPortParams(Integer.parseInt(SpringPropertyUtils
					.getProperty("serial.port.baud")), SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (Exception e) {
			getLogger().warn(e.getMessage(), e);
		}
		return port;
	}

	private static Logger getLogger() {
		return Logger.getLogger(DreadbotUtils.class);
	}

}
