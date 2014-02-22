package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class StoreyObject extends Actor {

	Texture wall_right;
	Texture wall_left;
	Texture wall_back;
	Texture floor;

	StoreyObject() {
		wall_right = new Texture(
				Gdx.files.internal("data/image/side_block.png"));
		wall_left = new Texture(Gdx.files.internal("data/image/side_con.png"));
		wall_back = new Texture(Gdx.files.internal("data/image/back_con.png"));
		floor = new Texture(Gdx.files.internal("data/image/floor.png"));

		wall_right.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		wall_left.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		wall_back.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		floor.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	}

	@Override
	public void act(float deltaTime) {
		// TODO: Burn part of floor when burning object is just below celing
		for (GameObject obj : BurningTower.gameObjects) {
			//
		}
	}

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);

		batch.draw(wall_right, this.getX() + this.getWidth(), this.getY(),
				wall_right.getWidth(), this.getHeight(), 0, this.getHeight()
						/ wall_right.getHeight(), 1, 0);
		batch.draw(wall_left, this.getX() - wall_right.getWidth(), this.getY(),
				wall_left.getWidth(), this.getHeight(), 0, this.getHeight()
						/ wall_left.getHeight(), 1, 0);
		batch.draw(wall_back, this.getX(), this.getY(), this.getWidth(),
				this.getHeight(), 0, this.getHeight() / wall_back.getHeight(),
				this.getWidth() / wall_back.getWidth(), 0);
		batch.draw(floor, this.getX(), this.getY() - floor.getHeight(),
				this.getWidth(), floor.getHeight(), 0, 1, this.getWidth()
						/ floor.getWidth(), 0);
	}

}
