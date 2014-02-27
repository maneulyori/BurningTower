package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;

public class BurningTower extends Game {
	
	BurningTowerScreen gameMain;
	BurningTowerScreen splash;
	//SplashScreen splash;
	ScoreScreen scoreScreen;

	FileHandle levelFile;

	InputMultiplexer inputMultiplexer;

	@Override
	public void create() {

		levelFile = Gdx.files.internal("data/levelData/level.json");

		gameMain = new BurningTowerScreen(this, "1"); // TODO: get level from user.
		splash = new BurningTowerScreen(this, "1");
		splash.setSplash();
		
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
	}
}
