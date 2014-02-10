package net.kucatdog.burningtower.main;

import java.util.ArrayList;
import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameObject extends Image {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5752514252157397512L;
	public static float range;

	public String objectType;
	public Texture texture;
	public TextureRegionDrawable drawable;
	public TextureRegionDrawable ashDrawable;
	public int resist;
	public int flameCnt;

	public int flameSpread = 0;
	public boolean isBurning = false;
	public boolean isBurnt = false;

	private int prevFlameSpread = 0;

	public ArrayList<Point> firepts = new ArrayList<Point>();

	@Override
	public void draw(Batch batch, float alpha) {
		super.draw(batch, alpha);
	}

	@Override
	public void act(float deltaTime) {
		if (this.isBurnt)
			this.setDrawable(ashDrawable);

		if (this.isBurning && this.flameSpread / 20 != prevFlameSpread) {
			prevFlameSpread = this.flameSpread / 20;
			Point p = new Point();
			p.x = (int) (this.getX() + Math.random() * this.getWidth());
			p.y = (int) (this.getY() + Math.random() * this.getHeight());

			firepts.add(p);
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

		if (x - range < this.getX() && x + range + width > this.getX()
				&& y - range < this.getY() && y + range + height > this.getY())
			return true;

		return false;
	}
}
