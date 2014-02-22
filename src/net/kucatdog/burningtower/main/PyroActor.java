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
	Thread burningThread;

	PyroActor(Thread burningThread) {
		texture = new Texture(Gdx.files.internal("data/image/pyro.png"));

		this.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
		
		this.setWidth(texture.getWidth());
		this.setHeight(texture.getHeight());
		
		this.burningThread = burningThread;
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
			this.setX(this.getX() - deltaTime * 500);
		else if (this.getX() < 10000)
			this.setX(this.getX() + deltaTime * 500);

		if (this.getX() <= 70) {
			// TODO: Better animation for pyro
			toggleDirection = true;
			burningThread.start();
		}
	}

	public void burnIt() {
		burnit = true;
	}
}
