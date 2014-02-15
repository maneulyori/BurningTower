package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.TimeUtils;

public class WallObject extends GameObject {

	private int prevFlameSpread = 0;
	private int flame = 0;

	private final static Texture[] wallFiretexture = new Texture[] {
			new Texture(Gdx.files.internal("data/image/wall_fire1.png")),
			new Texture(Gdx.files.internal("data/image/wall_fire2.png")) };

	private final static Texture burntWallTexture = new Texture(
			Gdx.files.internal("data/image/wall_burn.png"));

	private Texture wallFire;

	@Override
	public void draw(Batch batch, float alpha) {
		//super.draw(batch, alpha);

		wallFire = wallFiretexture[(int) (TimeUtils.millis() / 500 % wallFiretexture.length)];

		if (this.isBurnt) {
			for (int i = 0; i * burntWallTexture.getHeight() < this.getHeight(); i++) {
				batch.draw(burntWallTexture, this.getX(), this.getY() + i
						* burntWallTexture.getHeight());
			}
		}

		if (this.isBurning) {
			for (int i = 0; i < flame
					&& i * wallFiretexture[0].getHeight() < this.getHeight(); i++) {
				batch.draw(wallFire, this.getX() -25,
						this.getY() + i * wallFire.getHeight());
			}
		}
	}
	
	@Override
	public void setObjType(String objectType, boolean leaveRuin) {
		//Do nothing.
	}

	@Override
	public void act(float deltaTime) {
		// if (this.isBurnt)
		// this.setDrawable(ashDrawable);

		if (this.isBurning && this.flameSpread / 20 != prevFlameSpread) {
			prevFlameSpread = this.flameSpread / 20;
			flame++;
		}
	}
}
