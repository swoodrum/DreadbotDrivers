package scott.dreadbot.components;

public enum Servo {
	LEFTFRONTLOW(0),
	LEFTFRONTHIGH(1),
	LEFTMIDLOW(2),
	LEFTMIDHIGH(3),
	LEFTREARLOW(4),
	LEFTREARHIGH(5),
	RIGHTFRONTLOW(6),
	RIGHTFRONTHIGH(7),
	RIGHTMIDLOW(8),
	RIGHTMIDHIGH(9),
	RIGHTREARLOW(10),
	RIGHTREARHIGH(11),
	SWEEPER(12),
	PANNER(13);
	
	private int channel;
	Servo(int channel) {
		this.channel = channel;
	}
	public int channel() {
		return channel;
	}
}
