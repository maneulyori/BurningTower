package net.kucatdog.burningtower.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class BurningThread implements Runnable {

	Array<GameObject> gameObjs;
	GameContext context;

	public static int burningTick = 50;
	public int fire_x;
	public int fire_y;

	public BurningThread(Array<GameObject> objects, GameContext context) {
		this.gameObjs = objects;
		this.context = context;
	}

	public void setFire(int fire_x, int fire_y) {
		GameObject firePoint = new GameObject();
		
		firePoint.setX(fire_x);
		firePoint.setY(fire_y);
		firePoint.setWidth(0);
		firePoint.setHeight(0);

		for (GameObject obj : gameObjs) {
			if (obj.isItNear(firePoint)) {
				System.out.println("Setting fire on object");
				obj.flameCnt = 0;
				obj.isBurning = true;
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(burningTick);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					boolean isBurning = false;

					Array<GameObject> tmpObjList = new Array<GameObject>(gameObjs);
					
					for (GameObject obj : gameObjs) {
						if (obj.isBurnt) // Skip burnt object.
							continue;

						if (obj.isBurning) {
							if (!context.isBGMPlaying())
								context.playBGM();
							
							isBurning = true;

							obj.resist--;
							obj.flameSpread++;

							for (GameObject checkObj : tmpObjList) {
								if (checkObj.isItNear(obj)
										&& checkObj.flameCnt > 0) {
									checkObj.flameCnt--;
								}
							}
						}
					}
					

					if (!isBurning)
						context.stopBGM();

					for (GameObject obj : gameObjs) {
						if (obj.isBurnt) // Skip burnt object.
							continue;

						if (obj.flameCnt <= 0)
							obj.isBurning = true;
						if (obj.resist <= 0) {
							obj.isBurning = false;
							obj.isBurnt = true;
						}
					}
				}
			});
		}
	}
}
