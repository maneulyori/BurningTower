package net.kucatdog.burningtower.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.TimeUtils;

public class FireActor extends Actor {

	BurningTower context;

	public FireActor(BurningTower context) {
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
			}
		}
	}

	@Override
	public void act(float deltaTime) {
	}
}
