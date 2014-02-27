package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class StoreyObject extends Actor {

	BurningTowerScreen context;

	private Texture wall_right;
	private Texture wall_left;
	private Texture wall_back;
	private Texture floor;
	private Texture[] floorFire = new Texture[3];
	private Texture floorFire_draw;

	private float deltaTime = 0;
	private float deltaTime_consist = 0;
	private int cnt = 0;
	private int fire_start = -1, fire_end = -1;

	private Boolean fireFlag = false;

	private int flameCnt = 100;
	private int resist = 100;
	private float gameTick;

	StoreyObject(BurningTowerScreen context) {
		this.context = context;

		for (int i = 0; i < floorFire.length; i++) {
			floorFire[i] = new Texture(
					Gdx.files.internal("data/image/floorFire" + (1 + i)
							+ ".png"));
			floorFire[i].setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}

		gameTick = context.gameTick;

		floorFire_draw = floorFire[0];

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
	public void act(float delta) {

		deltaTime += delta;
		deltaTime_consist += delta;

		floorFire_draw = floorFire[((int) (deltaTime_consist * 1000) / 500 % floorFire.length)];

		if (deltaTime > gameTick / 1000.0) {
			deltaTime = 0;

			// Decrease flameCnt.
			if (fire_start == -1 && fire_end == -1) {
				for (GameObject obj : context.gameObjects) {

					if (obj.getY() + obj.getHeight() + context.fireRange >= this
							.getY()
							&& obj.getY() <= this.getY()
							&& obj.isBurning()) {
						this.flameCnt--;

						if (flameCnt <= 0) {
							fireFlag = true;
							fire_start = (int) obj.getX();
							fire_end = (int) obj.getX() + floorFire_draw.getWidth();
							break;
						}
					}
				}
			}

			if (fireFlag) {
				resist--;

				if (resist <= 0)
					fireFlag = false;

				for (GameObject obj : context.gameObjects) {
					if (obj.isBurnt()) // Skip burnt object.
						continue;

					if (obj.getY() > this.getY()
							&& obj.getY() < this.getY() + context.fireRange) {
						if (obj.getX() > fire_start && obj.getX() < fire_end) {
							obj.decreaseFlameCnt();
						}
					}
				}

				cnt++;

				if (cnt >= 20) {
					cnt = 0;

					if (fire_start - floorFire[0].getWidth() > this.getX()) {
						fire_start -= floorFire[0].getWidth();
					}
					if (fire_end < this.getX() + this.getWidth()) {
						fire_end += floorFire[0].getWidth();
					}
				}
			}
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

		if (fireFlag || context.fireactor.getFireForever()) {
			batch.draw(floorFire_draw, fire_start, this.getY(), fire_end - floorFire_draw.getWidth()
					- fire_start, floorFire_draw.getHeight(), 0, 1,
					(fire_end - floorFire_draw.getWidth() - fire_start) / floorFire_draw.getWidth(), 0);
		}
	}

	public boolean isBurning() {
		return fireFlag;
	}

	public boolean isBurnt() {
		return resist <= 0;
	}
	
	public void setResist(int resist) {
		this.resist = resist;
	}
	
	public void setFlammable(int flammable) {
		this.flameCnt = flammable;
	}
	
	public void distinguish() {
		this.fireFlag = false;
	}
}
