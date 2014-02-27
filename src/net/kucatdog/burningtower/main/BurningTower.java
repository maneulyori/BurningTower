package net.kucatdog.burningtower.main;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class BurningTower extends Game {
	private final String[] audioFiles = { "gameplay", "fire", "woman_scream" };

	BurningTowerScreen gameMain;
	BurningTowerScreen splash;
	// SplashScreen splash;
	ScoreScreen scoreScreen;

	FileHandle levelFile;

	InputMultiplexer inputMultiplexer;

	private Hashtable<String, Music> sounds;

	public BurningTower() {
	}

	@Override
	public void create() {

		levelFile = Gdx.files.internal("data/levelData/level.json");

		List<String> audioFileList = Arrays.asList(audioFiles);
		sounds = new Hashtable<String, Music>();

		for (String filename : audioFileList) {
			System.out.println("Loading " + filename + ".ogg");
			Music music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/"
					+ filename + ".ogg"));
			sounds.put(filename, music);
		}

		setLoopAudio("gameplay", true);
		setLoopAudio("fire", true);

		playAudio("gameplay");

		gameMain = new BurningTowerScreen(this, "1"); // TODO: get level from
														// user.
		splash = new SplashScreen(this, "1");

		scoreScreen = new ScoreScreen(this);

		setScreen(splash);

		inputMultiplexer = new InputMultiplexer(gameMain.stage);
		inputMultiplexer.addProcessor(splash.stage);
		inputMultiplexer.addProcessor(scoreScreen.stage);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void dispose() {
		gameMain.dispose();
		splash.dispose();
		scoreScreen.dispose();

		for (Music music : sounds.values()) {
			music.dispose();
		}

		sounds.clear();
	}

	public void playAudio(String filename) {
		Music music = sounds.get(filename);

		music.play();
	}

	public void pauseAudio(String filename) {
		Music music = sounds.get(filename);

		music.pause();
	}

	public void stopAudio(String filename) {
		Music music = sounds.get(filename);

		music.stop();
	}

	public void stopAllAudio() {
		for (Music music : sounds.values()) {
			music.stop();
		}
	}

	public void pauseAllAudio() {
		for (Music music : sounds.values()) {
			music.pause();
		}
	}

	public void setLoopAudio(String filename, boolean loop) {
		Music music = sounds.get(filename);

		music.setLooping(loop);
	}

	public boolean isPlayingAudio(String filename) {
		Music music = sounds.get(filename);

		return music.isPlaying();
	}
}
