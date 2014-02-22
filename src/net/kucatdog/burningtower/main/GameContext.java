package net.kucatdog.burningtower.main;

public interface GameContext {
	public abstract void playBGM();
	public abstract void stopBGM();
	public abstract boolean isBGMPlaying();
	public abstract void startFire();
}
