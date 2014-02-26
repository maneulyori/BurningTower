package net.kucatdog.burningtower.main;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GameObject extends Image {

	public enum PlaceLocation {
		FLOOR, WALL, CEILING
	}

	public enum ObjectProp {
		EXPLOSIVE, DISTINGUISHER, NORMAL
	}

	BurningTower context;

	private float range;

	private String objectType;
	private Texture texture;
	private TextureRegionDrawable drawable;
	private TextureRegionDrawable ashDrawable;
	private int resist;
	private int flameCnt;

	private int flameSpread = 0;
	private PlaceLocation placeLocation = PlaceLocation.WALL;
	private ObjectProp objectProp = ObjectProp.NORMAL;

	private boolean burningFlag = false;
	private boolean burntFlag = false;

	private int prevFlameSpread = 0;

	float deltaTime = 0;
	int cnt = 0;

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
		range = context.fireRange;

		deltaTime += delta;

		if (this.burntFlag)
			this.setDrawable(ashDrawable);

		if (this.burningFlag && this.flameSpread / 20 != prevFlameSpread) {
			prevFlameSpread = this.flameSpread / 20;
			Point p = new Point();
			p.x = (int) (this.getX() + Math.random() * this.getWidth());
			p.y = (int) (this.getY() + Math.random() * this.getHeight());

			firepts.add(p);
		}

		if (deltaTime > context.gameTick / 1000.0) {
			deltaTime = 0;

			if (cnt != -1 && objectProp != ObjectProp.NORMAL)
				cnt++;

			if (cnt < 100) {
				switch (objectProp) {
				case DISTINGUISHER:
					break;
				case EXPLOSIVE:
					range *= 2;
					break;
				default:
					System.out.println("are you sane?");
				}
			} else {
				cnt = -1;
			}

			if (this.burningFlag) {
				this.resist--;
				this.flameSpread++;

				for (GameObject obj : context.gameObjects) {
					if (obj.equals(this))
						continue;

					if (obj.burntFlag) // Skip burnt object.
						continue;

					if (obj.isItNear(this) && obj.flameCnt > 0) {
						obj.flameCnt--;
					}
				}
			}

			if (this.flameCnt <= 0)
				this.burningFlag = true;
			if (this.resist <= 0) {
				this.burningFlag = false;
				this.burntFlag = true;
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

	public void setPlaceLocation(PlaceLocation location) {
		this.placeLocation = location;
	}

	public void setObjectProp(ObjectProp prop) {
		this.objectProp = prop;
	}

	public void setResist(int resist) {
		this.resist = resist;
	}

	public void setFlameCnt(int flameCnt) {
		this.flameCnt = flameCnt;
	}

	public String getObjectType() {
		return objectType;
	}

	public ObjectProp getObjectProp() {
		return objectProp;
	}

	public PlaceLocation getPlaceLocation() {
		return placeLocation;
	}

	public boolean isBurning() {
		return burningFlag;
	}

	public boolean isBurnt() {
		return burntFlag;
	}

	public void decreaseFlameCnt() {
		this.flameCnt--;
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

	public void setFire() {
		burningFlag = true;
	}
}
