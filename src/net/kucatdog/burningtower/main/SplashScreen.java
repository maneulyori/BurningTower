package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;

public class SplashScreen extends GameScreen implements Screen {

	Texture splash;

	SplashScreen(MainMenu game) {
		super(game);

		splash = new Texture(Gdx.files.internal("data/image/start.png"));
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void hide() {
		super.hide();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		batch.begin();

		batch.draw(splash, 0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		batch.end();

		if (Gdx.input.justTouched()) // use your own criterion here
			game.setScreen(game.gameMain);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void dispose() {
		super.dispose();
		splash.dispose();
	}

}
