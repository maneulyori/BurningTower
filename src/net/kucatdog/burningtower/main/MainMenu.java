package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Game;

public class MainMenu extends Game {

	BurningTower gameMain;
	SplashScreen splash;
	
	@Override
	public void create() {
		
		gameMain = new BurningTower(this, 1); //TODO: get level from user. 
		splash = new SplashScreen(this);
		
		setScreen(splash);
	}
	
	@Override
	public void dispose() {
		gameMain.dispose();
	}
}
