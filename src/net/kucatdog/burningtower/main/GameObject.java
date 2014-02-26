package net.kucatdog.burningtower.main;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class GameObject extends Image {
	BurningTower context;
	
	public static float range;

	public String objectType;
	public Texture texture;
	public TextureRegionDrawable drawable;
	public TextureRegionDrawable ashDrawable;
	public int resist;
	public int flameCnt;

	public int flameSpread = 0;
	public boolean objectPlaceable = false;
	public PlaceLocation placeLocation;

	public boolean isBurning = false;
	public boolean isBurnt = false;

	private int prevFlameSpread = 0;

	float deltaTime = 0;

	public ArrayList<Point> firepts = new ArrayList<Point>();

	public GameObject(BurningTower context) {
		this.context = context;
	}
	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);
	}

	@Override
	public void act(float delta) {

		deltaTime += delta;

		if (this.isBurnt)
			this.setDrawable(ashDrawable);

		if (this.isBurning && this.flameSpread / 20 != prevFlameSpread) {
			prevFlameSpread = this.flameSpread / 20;
			Point p = new Point();
			p.x = (int) (this.getX() + Math.random() * this.getWidth());
			p.y = (int) (this.getY() + Math.random() * this.getHeight());

			firepts.add(p);
		}

		if (deltaTime > 50.0 / 1000.0) {
			deltaTime = 0;

			if (this.isBurning) {
				this.resist--;
				this.flameSpread++;

				for (GameObject obj : context.gameObjects) {
					if (obj == this)
						continue;

					if (obj.isBurnt) // Skip burnt object.
						continue;

					if (obj.isItNear(this) && obj.flameCnt > 0) {
						obj.flameCnt--;
					}
				}
			}

			if (this.flameCnt <= 0)
				this.isBurning = true;
			if (this.resist <= 0) {
				this.isBurning = false;
				this.isBurnt = true;
			}
		}
	}

	public void setObjType(String objectType, boolean leaveRuin) {

		texture = new Texture(Gdx.files.internal("data/image/" + objectType
				+ ".png"));

		drawable = new TextureRegionDrawable(new TextureRegion(texture));

		if (leaveRuin)
			this.ashDrawable = new TextureRegionDrawable(new TextureRegion(
					new Texture(Gdx.files.internal("data/image/" + objectType
							+ "_burn.png"))));
		else
			this.ashDrawable = new TextureRegionDrawable(new TextureRegion(
					new Texture(Gdx.files.internal("data/image/ashes.png"))));

		this.setDrawable(drawable);

		this.setWidth(texture.getWidth());
		this.setHeight(texture.getHeight());

		this.objectType = objectType;
	}

	public boolean isItNear(GameObject obj) {
		float x = obj.getX();
		float y = obj.getY();
		float width = obj.getWidth();
		float height = obj.getHeight();

		if ((x + width + range > this.getX() && x - range < this.getX()
				&& y + height + range > this.getY() && y - range < this.getY()))
			return true;

		return false;
	}
}
