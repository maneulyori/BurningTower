package net.kucatdog.burningtower.main;

public class CountdownTimer implements Runnable {

	BurningTower context;
	int timer = 0;

	private volatile boolean terminateFlag = false;
	private volatile boolean pauseFlag = false;

	CountdownTimer(BurningTower context) {
		this.context = context;
	}

	@Override
	public void run() {
		for (timer = 60; timer > 0 && !terminateFlag; timer--) {
			try {
				while (pauseFlag) {
					Thread.sleep(100);
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (terminateFlag)
			return;

		context.startFire();
	}

	public String getTimerStr() {
		if (timer <= 0)
			return "TIME OVER";

		return "TIME LEFT: " + timer + "sec.";
	}

	public void setTime(int timer) {
		this.timer = timer;
	}

	public void terminate() {
		terminateFlag = true;
	}

	public void clearTerminate() {
		terminateFlag = false;
	}

	public void pause() {
		pauseFlag = true;
	}

	public void resume() {
		pauseFlag = false;
	}
}
