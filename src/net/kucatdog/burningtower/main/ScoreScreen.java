package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ScoreScreen extends GameScreen implements Screen {

	private int score = 0;

	private BitmapFont font;

	ScoreScreen(MainMenu game) {
		super(game);
		
		font = new BitmapFont();
		font.scale(3);
	}

	@Override
	public void show() {
		super.show();

		Label scoreLabel = new Label("You burnt " + score + "%!",
				new Label.LabelStyle(font, Color.WHITE));
		scoreLabel.setPosition(200, 640);
		stage.addActor(scoreLabel);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (font != null)
			font.dispose();
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
		if (Gdx.input.justTouched()) // use your own criterion here
			game.setScreen(game.splash);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void resume() {
		super.resume();
	}

	public void setScore(int score) {
		this.score = score;
	}

}
