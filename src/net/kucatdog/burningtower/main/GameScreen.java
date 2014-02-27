package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GameScreen implements Screen {

	final int VIRTUAL_WIDTH = 768;
	final int VIRTUAL_HEIGHT = 1280;

	MainMenu game;
	OrthographicCamera cam;
	Stage stage;
	SpriteBatch batch;

	GameScreen(MainMenu game) {
		this.game = game;

		cam = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		stage = new Stage();
		stage.setCamera(cam);

		batch = new SpriteBatch();
	}

	@Override
	public void dispose() {
		stage.dispose();
		batch.dispose();
	}

	@Override
	public void hide() {
		stage.clear();
	}

	@Override
	public void pause() {

	}

	@Override
	public void render(float delta) {

		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());


		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportHeight = height; // set the viewport
		cam.viewportWidth = width;
		if (VIRTUAL_WIDTH / cam.viewportWidth < VIRTUAL_HEIGHT
				/ cam.viewportHeight) {
			// set the right zoom direct
			cam.zoom = VIRTUAL_HEIGHT / cam.viewportHeight;
		} else {
			// set the right zoom direct
			cam.zoom = VIRTUAL_WIDTH / cam.viewportWidth;
		}
		cam.position.set(cam.zoom * cam.viewportWidth / 2.0f, cam.zoom
				* cam.viewportHeight / 2.0f, 0);
		cam.update();

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

}
