package scott.dreadbot.components;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

public class SerialPortBroker extends UntypedActor implements
		SerialPortEventListener {

	private SerialPort serialPort;
	private String portId;

	private byte[] buffer = new byte[1024];

	private InputStream inputStream;

	public SerialPortBroker(String portId) {
		this.portId = portId;
	}

	@Override
	public void preStart() {
		serialPort = DreadbotUtils.getSerialPort(portId);
		try {
			inputStream = serialPort.getInputStream();
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e1) {
			getLogger().fatal(e1);
			e1.printStackTrace();
		}
	}

	private Logger getLogger() {
		return Logger.getLogger(SerialPortBroker.class);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStop() {
		serialPort.close();
		getLogger().debug("Serial port closed from Akka...");
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		int data;

		try {
			int len = 0;
			while ((data = inputStream.read()) > -1) {
				if (data == '\n') {
					break;
				}
				buffer[len++] = (byte) data;
			}
			getLogger().debug(new String(buffer, 0, len));
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().debug(e);
		}

	}

}
