package net.kucatdog.burningtower.main;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.TimeUtils;

public class FireActor extends Actor{

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);

		for (GameObject obj : BurningTower.gameObjects) {
			if (obj.isBurning) {
				Texture fireToDraw = BurningTower.fire[(int) (TimeUtils.millis() / 500 % BurningTower.nOfFireImages)];
				
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
