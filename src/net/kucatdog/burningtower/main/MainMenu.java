package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Game;

public class MainMenu extends Game {

	BurningTower gameMain;
	SplashScreen splash;
	
	@Override
	public void create() {
		// TODO Auto-generated method stub
		
		gameMain = new BurningTower(this);
		splash = new SplashScreen(this);
		
		setScreen(splash);
	}
	
	@Override
	public void dispose() {
		gameMain.dispose();
	}
}
