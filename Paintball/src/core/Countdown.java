package core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class Countdown {

	private int time;
	private String message;
	private Consumer<?> callback;
	final private Timer timer;

	public Countdown(String message, int duration, Consumer<?> callback) {
		this(message, duration);
		this.callback = callback;
	}

	public Countdown(String message, int duration) {
		timer = new Timer();
		this.message = message;
		time = duration;

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				System.out.println(time--);

				if (time < 0) {
					timer.cancel();
					time = 0;

					if (callback != null) {
						callback.accept(null);
					}
				}
			}
		}, 0, 1000);
	}

	public int getTime() {
		return (this.time);
	}

	public void cancel() {
		callback = null;
		timer.cancel();
	}

	public String getTimeMessage() {
		if (message == null) {
			return ("");
		}

		if (time == 0) {
			return (String.format("%s: %s", message, "expired"));
		}

		return (String.format("%s: %s", message, time));
	}
}
