package net.kucatdog.burningtower.main;

import com.badlogic.gdx.ApplicationListener;

public interface GameContext {
	public abstract void playBGM();
	public abstract void stopBGM();
	public abstract boolean isBGMPlaying();
}
