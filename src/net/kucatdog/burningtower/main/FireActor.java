package net.kucatdog.burningtower.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.TimeUtils;

public class FireActor extends Actor {

	BurningTowerScreen context;

	public FireActor(BurningTowerScreen context) {
		this.context = context;
	}

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);

		for (GameObject obj : context.gameObjects) {
			if (obj.isBurning()) {
				Texture fireToDraw = context.fire[(int) (TimeUtils.millis() / 500 % context.fire.length)];

				for (Point pt : obj.firepts) {
					batch.draw(fireToDraw, pt.x, pt.y);
				}

				if (obj.isExploding()) {
					batch.draw(fireToDraw, obj.getX() - context.fireRange,
							obj.getY() - context.fireRange, obj.getWidth()
									+ context.fireRange, obj.getHeight()
									+ context.fireRange);
				}
			}
		}
	}

	@Override
	public void act(float deltaTime) {
	}
}
