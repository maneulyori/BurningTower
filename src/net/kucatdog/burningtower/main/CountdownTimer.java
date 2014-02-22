package net.kucatdog.burningtower.main;

public class CountdownTimer implements Runnable {

	GameContext context;
	int timer = 0;

	CountdownTimer(GameContext context) {
		this.context = context;
	}

	@Override
	public void run() {
		for (timer = 60; timer > 0; timer--) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		context.startFire();
	}
	
	public String getTimerStr() {
		return "TIME LEFT: " + timer + "sec.";
	}
}
