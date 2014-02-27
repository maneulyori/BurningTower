package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SplashScreen extends BurningTowerScreen {

	private BurningTower game;

	class ObjectDisplayer extends BurningTowerScreen.ObjectDisplayer {

		@Override
		public void run() {
			for (Actor actor : actorList) {

				stage.addActor(actor);

				if (!skip) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			startFire();

			displayLogoText();
		}
	}

	BitmapFont logoText;
	ObjectDisplayer objectDisplayer;

	SplashScreen(BurningTower game, String level) {
		super(game, level);
		this.game = game;

		objectDisplayer = new ObjectDisplayer();
		overrideObjectDisplayer(objectDisplayer);

		logoText = new BitmapFont();
		logoText.scale(10);
		
		this.gameTick = 10;
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
	void drawUI() {
		//Do NOTHING
	}

	@Override
	public void render(float delta) {

		if (Gdx.input.justTouched()) {
			if (objectDisplayer.getSkip())
				game.setScreen(game.gameMain);
		}

		super.render(delta);
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
	}

	@Override
	public void stopBurning() {
		fireactor.setFireForever();
	}
	
	@Override
	public void startFire() {
		pyro.burnIt();
		fireTimer.setTime(0);
	}

	private void displayLogoText() {
		Label label = new Label("Burning\nTower", new Label.LabelStyle(
				logoText, Color.RED));

		label.setPosition(100, 850);
		stage.addActor(label);
	}
}
