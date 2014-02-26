package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SplashScreen implements Screen{

	MainMenu game;
	Texture splash;
	SpriteBatch batch;
	
	SplashScreen(MainMenu game) {
		this.game = game;
		
		batch = new SpriteBatch();
		
		splash = new Texture(Gdx.files.internal("data/image/start.png"));
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta) {
		batch.begin();
		
		batch.draw(splash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		batch.end();
		
		if (Gdx.input.justTouched()) // use your own criterion here
            game.setScreen(game.gameMain);
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose() {
		splash.dispose();
		batch.dispose();
	}

}
