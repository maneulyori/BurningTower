package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

public class GameScreen implements Screen {

	BurningTower game;
	OrthographicCamera cam;
	Stage stage;
	SpriteBatch batch;
	GestureDetector pinchToZoom;
	float zoom;

	GameScreen(BurningTower game) {
		this.game = game;
		this.cam = game.cam;

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
		if (game.VIRTUAL_WIDTH / cam.viewportWidth < game.VIRTUAL_HEIGHT
				/ cam.viewportHeight) {
			// set the right zoom direct
			cam.zoom = game.VIRTUAL_HEIGHT / cam.viewportHeight;
		} else {
			// set the right zoom direct
			cam.zoom = game.VIRTUAL_WIDTH / cam.viewportWidth;
		}
		
		zoom = cam.zoom;
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
