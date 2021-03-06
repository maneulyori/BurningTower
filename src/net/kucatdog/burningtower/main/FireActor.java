package net.kucatdog.burningtower.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FireActor extends Actor {

	private BurningTowerScreen context;
	private boolean setFireForever = false;
	Texture fireToDraw;
	private float deltaTime = 0;

	public FireActor(BurningTowerScreen context) {
		this.context = context;
		fireToDraw = context.fire[0];
	}

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);

		for (GameObject obj : context.gameObjects) {
			if (obj.isBurning() || setFireForever || !obj.isLeaveRuin()) {

				for (FirePoint pt : obj.firepts) {
					fireToDraw = context.fire[(int) ((pt.time * 250 + deltaTime) * 1000)
							/ 500 % context.fire.length];

					if (obj.isBurning() || setFireForever)
						batch.draw(fireToDraw, pt.x, pt.y);
					else
						batch.draw(fireToDraw, pt.x, pt.y,
								fireToDraw.getWidth() / 2,
								fireToDraw.getHeight() / 2);
				}

				if (obj.isExploding()) {
					batch.draw(fireToDraw, obj.getX(), obj.getY(),
							obj.getWidth(), obj.getHeight());
				}
			}
		}
	}

	@Override
	public void act(float delta) {
		deltaTime += delta;
	}

	public void setFireForever() {
		setFireForever = true;
	}

	public boolean getFireForever() {
		return setFireForever;
	}
}
