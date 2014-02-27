package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PyroActor extends Image {
	Texture texture;
	boolean toggleDirection = false;
	boolean burnit = false;
	BurningTower context;

	GameObject fireobj;

	PyroActor(BurningTower context) {
		texture = new Texture(Gdx.files.internal("data/image/pyro.png"));

		this.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));

		this.setWidth(texture.getWidth());
		this.setHeight(texture.getHeight());
		this.context = context;
		this.fireobj = new GameObject(context);
		fireobj.setPosition(130, 10);
	}

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);
	}

	@Override
	public void act(float deltaTime) {

		if (!burnit)
			return;

		if (!toggleDirection)
			this.setX(this.getX() - deltaTime * 400);
		else if (this.getX() < 10000)
			this.setX(this.getX() + deltaTime * 400);

		if (this.getX() <= 70) {
			// TODO: Better animation for pyro
			toggleDirection = true;

			for (GameObject obj : context.gameObjects) {
				if (obj.isItNear(fireobj)) {
					obj.setFire();
				}
			}
			context.dragLock = true;
			context.playBGM();
		}
	}

	public void setFirePt(float x, float y) {
		fireobj.setPosition(x, y);
		fireobj.setY(y);
	}

	public void burnIt() {
		burnit = true;
	}
}
