package net.kucatdog.burningtower.main;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class BurningTower extends Game {

	final int VIRTUAL_WIDTH = 768;
	final int VIRTUAL_HEIGHT = 1280;

	private final String[] audioFiles = { "gameplay", "fire", "woman_scream" };

	BurningTowerScreen level1;
	BurningTowerScreen level2;
	SplashScreen splash;
	ScoreScreen scoreScreen;

	FileHandle levelFile;

	InputMultiplexer inputMultiplexer;

	OrthographicCamera cam;

	private Hashtable<String, Music> sounds;

	float minZoom;

	float xlock;
	float ylock;

	public BurningTower() {
	}

	@Override
	public void create() {

		levelFile = Gdx.files.internal("data/levelData/level.json");

		cam = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		List<String> audioFileList = Arrays.asList(audioFiles);
		sounds = new Hashtable<String, Music>();

		for (String filename : audioFileList) {
			System.out.println("Loading " + filename + ".ogg");
			Music music = Gdx.audio.newMusic(Gdx.files.internal("data/audio/"
					+ filename + ".ogg"));
			sounds.put(filename, music);
		}

		if (VIRTUAL_WIDTH / Gdx.graphics.getWidth() < VIRTUAL_HEIGHT
				/ Gdx.graphics.getHeight()) {
			// set the right zoom direct
			minZoom = VIRTUAL_HEIGHT / Gdx.graphics.getHeight();
		} else {
			// set the right zoom direct
			minZoom = VIRTUAL_WIDTH / Gdx.graphics.getWidth();
		}

		setLoopAudio("gameplay", true);
		setLoopAudio("fire", true);

		playAudio("gameplay");

		level1 = new BurningTowerScreen(this, "1"); // TODO: get level from
		level2 = new BurningTowerScreen(this, "2");
														// user.
		splash = new SplashScreen(this, "splash"); // Load splash level

		scoreScreen = new ScoreScreen(this);

		setScreen(splash);

		GestureDetector gestureDetector = new GestureDetector(
				new GestureListener() {

					float zoom;

					@Override
					public boolean fling(float arg0, float arg1, int arg2) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean longPress(float arg0, float arg1) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean pan(float x, float y, float deltaX,
							float deltaY) {
						if (VIRTUAL_HEIGHT / 2 * cam.zoom < cam.position.y
								+ deltaY
								&& ylock - (VIRTUAL_HEIGHT / 2 * cam.zoom) > cam.position.y
										+ deltaY)
							cam.translate(0, deltaY);
						if (VIRTUAL_WIDTH / 2 * cam.zoom < cam.position.x
								- deltaX
								&& xlock - (VIRTUAL_WIDTH / 2 * cam.zoom) > cam.position.x
										- deltaX)
							cam.translate(-deltaX, 0);
						cam.update();

						return true;
					}

					@Override
					public boolean panStop(float arg0, float arg1, int arg2,
							int arg3) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean pinch(Vector2 initialPointer1,
							Vector2 initialPointer2, Vector2 pointer1,
							Vector2 pointer2) {

						float initialDst = Math.abs(initialPointer1
								.dst(initialPointer2));
						float nowDst = Math.abs(pointer1.dst(pointer2));

						if (zoom
								* (1 - (nowDst - initialDst)
										/ (float) Math
												.sqrt(VIRTUAL_HEIGHT
														* VIRTUAL_HEIGHT
														+ VIRTUAL_WIDTH
														* VIRTUAL_WIDTH)) > minZoom)
							return false;

						cam.zoom = zoom
								* (1 - (nowDst - initialDst)
										/ (float) Math
												.sqrt(VIRTUAL_HEIGHT
														* VIRTUAL_HEIGHT
														+ VIRTUAL_WIDTH
														* VIRTUAL_WIDTH));
						
						if (!(VIRTUAL_HEIGHT / 2 * cam.zoom < cam.position.y)) {
							cam.position.y = VIRTUAL_HEIGHT / 2 * cam.zoom;
						}
						if (!(ylock - (ylock / 2 * cam.zoom) > cam.position.y)) {
							cam.position.y = ylock - (ylock / 2 * cam.zoom);
						}
						if (!(VIRTUAL_WIDTH / 2 * cam.zoom < cam.position.x)) {
							cam.position.x = VIRTUAL_WIDTH / 2 * cam.zoom;
						}
						if (!(xlock - (xlock / 2 * cam.zoom) > cam.position.x)) {
							cam.position.x = xlock - (xlock / 2 * cam.zoom);
						}
						
						cam.update();
						
						return true;
					}

					@Override
					public boolean tap(float arg0, float arg1, int arg2,
							int arg3) {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean touchDown(float arg0, float arg1, int arg2,
							int arg3) {
						zoom = cam.zoom;
						return false;
					}

					@Override
					public boolean zoom(float arg0, float arg1) {
						// TODO Auto-generated method stub
						return false;
					}

				});

		inputMultiplexer = new InputMultiplexer(level1.stage);
		inputMultiplexer.addProcessor(level2.stage);
		inputMultiplexer.addProcessor(splash.stage);
		inputMultiplexer.addProcessor(scoreScreen.stage);
		inputMultiplexer.addProcessor(gestureDetector);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void dispose() {
		level1.dispose();
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

	public void setLock(float x, float y) {
		if(x < Gdx.graphics.getWidth())
			x = Gdx.graphics.getWidth();
		if(y < Gdx.graphics.getHeight())
			y = Gdx.graphics.getHeight();
		this.xlock = x;
		this.ylock = y;
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
