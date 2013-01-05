package scott.dreadbot.components;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class GamePadControllerBroker extends UntypedActor {
	
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	private GamePadController controller;
	
	private LoggingAdapter getLogger() {
		//return Logger.getLogger(SerialPortBroker.class);
		return log;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message.equals("tick")) {
			controller.poll();
			int compassDir = controller.getHatDir();
			if (compassDir != GamePadController.NONE)
				switch (compassDir) {
				case GamePadController.EAST:
					getLogger().debug("Compass direction: EAST");
					break;
				case GamePadController.WEST:
					getLogger().debug("Compass direction: WEST");
					break;
				case GamePadController.NORTH:
					getLogger().debug("Compass direction: NORTH");
					break;
				case GamePadController.SOUTH:
					getLogger().debug("Compass direction: SOUTH");
					break;
				case GamePadController.NW:
					getLogger().debug("Compass direction: NORTHWEST");
					break;
				case GamePadController.NE:
					getLogger().debug("Compass direction: NORTHEAST");
					break;
				case GamePadController.SW:
					getLogger().debug("Compass direction: SOUTHWEST");
					break;
				case GamePadController.SE:
					getLogger().debug("Compass direction: SOUTHEAST");
					break;
				}
			int xyDir = controller.getXYStickDir();
			if (xyDir != GamePadController.NONE)
				switch (xyDir) {
				case GamePadController.EAST:
					getLogger().debug("XY direction: EAST");
					break;
				case GamePadController.WEST:
					getLogger().debug("XY direction: WEST");
					break;
				case GamePadController.NORTH:
					getLogger().debug("XY direction: NORTH");
					break;
				case GamePadController.SOUTH:
					getLogger().debug("XY direction: SOUTH");
					break;
				case GamePadController.NW:
					getLogger().debug("XY direction: NORTHWEST");
					break;
				case GamePadController.NE:
					getLogger().debug("XY direction: NORTHEAST");
					break;
				case GamePadController.SW:
					getLogger().debug("XY direction: SOUTHWEST");
					break;
				case GamePadController.SE:
					getLogger().debug("XY direction: SOUTHEAST");
					break;
				}
			int zrzDir = controller.getZRZStickDir();
			if (zrzDir != GamePadController.NONE)
				switch (zrzDir) {
				case GamePadController.EAST:
					getLogger().debug("ZRZ direction: EAST");
					break;
				case GamePadController.WEST:
					getLogger().debug("ZRZ direction: WEST");
					break;
				case GamePadController.NORTH:
					getLogger().debug("ZRZ direction: NORTH");
					break;
				case GamePadController.SOUTH:
					getLogger().debug("ZRZ direction: SOUTH");
					break;
				case GamePadController.NW:
					getLogger().debug("ZRZ direction: NORTHWEST");
					break;
				case GamePadController.NE:
					getLogger().debug("ZRZ direction: NORTHEAST");
					break;
				case GamePadController.SW:
					getLogger().debug("ZRZ direction: SOUTHWEST");
					break;
				case GamePadController.SE:
					getLogger().debug("ZRZ direction: SOUTHEAST");
					break;
				}
			boolean[] buttons = controller.getButtons();
			for (int i = 0; i < GamePadController.NUM_BUTTONS; i++) {
				if (buttons[i] == true) {
					getLogger().debug("Button " + (i + 1) + " pressed");
				}
			}
			ActorRef main = getContext().actorFor("akka://DreadbotActors/user/receiver");
			main.tell("gamepad", getSelf());
		} else  {
			unhandled(message);
		}

	}

	@Override
	public void preStart() {
		try {
			controller = new GamePadController();
		} catch (Exception e) {
			getLogger().warning(e.getMessage());
			e.printStackTrace();
		}
	}

}
