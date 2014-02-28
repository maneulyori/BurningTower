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

	BurningTowerScreen context;

	private float range;

	private String objectType;
	private Texture texture;
	private Texture ashTexture;
	private TextureRegionDrawable drawable;
	private TextureRegionDrawable ashDrawable;
	private Texture waterSpray;
	private int resist;
	int flameCnt;

	private int flameSpread = 0;
	private PlaceLocation placeLocation = PlaceLocation.WALL;
	private ObjectProp objectProp = ObjectProp.NORMAL;

	private boolean burningFlag = false;
	private boolean burntFlag = false;
	private boolean exploding = false;
	private boolean distinguishing = false;
	private boolean leaveRuin = false;

	private int prevFlameSpread = 0;

	private float deltaTime = 0;
	int cnt = 0;
	private float gameTick;

	public ArrayList<FirePoint> firepts = new ArrayList<FirePoint>();

	public GameObject(BurningTowerScreen context) {
		this.context = context;
		this.gameTick = context.gameTick;

		waterSpray = new Texture(
				Gdx.files.internal("data/image/water_spray.png"));
	}

	@Override
	public void draw(Batch batch, float alpha) {
		if (objectType.equals("springkler") && isDistinguishing()) {
			batch.draw(waterSpray, this.getX(),
					this.getY() - waterSpray.getWidth());
		}

		super.draw(batch, alpha);
	}

	@Override
	public void act(float delta) {
		if (this.flameCnt <= 0)
			this.burningFlag = true;
		if (this.resist <= 0) {
			this.burntFlag = true;
			this.burningFlag = false;
		}
		
		range = context.fireRange;

		deltaTime += delta;

		if (this.burntFlag)
			this.setDrawable(ashDrawable);

		if (this.burningFlag && this.flameSpread / 20 != prevFlameSpread
				&& objectProp != ObjectProp.DISTINGUISHER) {
			prevFlameSpread = this.flameSpread / 20;
			FirePoint p = new FirePoint();
			p.x = (int) (this.getX() + Math.random() * this.getWidth());
			p.y = (int) (this.getY() + Math.random() * this.getHeight());
			p.time = deltaTime;

			firepts.add(p);
		}

		if (deltaTime > gameTick / 1000.0) {
			deltaTime = 0;

			if (cnt != -1 && objectProp != ObjectProp.NORMAL && burningFlag)
				cnt++;

			if (cnt < 80 && burningFlag) {
				switch (objectProp) {
				case DISTINGUISHER:
					// TODO: play sound.
					distinguishing = true;
					break;
				case EXPLOSIVE:
					// Double range, double speed.
					// TODO: play sound.
					range *= 2;
					gameTick = context.gameTick / 2;
					exploding = true;
					break;
				case NORMAL:
					// Do nothing.
					break;
				default:
					System.out.println("are you sane?");
				}
			} else {
				exploding = false;
				// distinguishing = false;
				gameTick = context.gameTick;
				cnt = -1;
			}

			if (distinguishing) {
				float x1, y1;
				float x2, y2;

				x1 = this.getX();
				x2 = this.getX() + context.distinguish_x;
				y1 = this.getY() - context.distinguish_y;
				y2 = this.getY();

				for (GameObject obj : context.gameObjects) {
					if (obj.equals(this))
						continue;

					if ((obj.getX() + obj.getWidth() >= x1 && obj.getX() <= x2)
							|| (obj.getY() + obj.getHeight() >= y1 && obj
									.getY() <= y2)) {
						obj.distinguish();
					}
				}

				for (StoreyObject obj : context.storeys) {
					if (obj.getY() >= y1 && obj.getY() <= y2)
						obj.distinguish();
				}
			}

			if (this.burningFlag) {
				this.resist--;
				if (objectProp != ObjectProp.DISTINGUISHER) {
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
			}
		}
	}

	public void setObjType(String objectType, boolean leaveRuin) {
		this.leaveRuin = leaveRuin;
		texture = new Texture(Gdx.files.internal("data/image/" + objectType
				+ ".png"));

		drawable = new TextureRegionDrawable(new TextureRegion(texture));

		if (leaveRuin)
			ashTexture = new Texture(Gdx.files.internal("data/image/"
					+ objectType + "_burn.png"));
		else
			ashTexture = texture;// new
									// Texture(Gdx.files.internal("data/image/ashes.png"));

		this.ashDrawable = new TextureRegionDrawable(new TextureRegion(
				ashTexture));

		this.setDrawable(drawable);

		this.setWidth(texture.getWidth());
		this.setHeight(texture.getHeight());

		this.objectType = objectType;
	}

	@Override
	public void setWidth(float width) {
		super.setWidth(((int) width + context.GRIDPIXELSIZE / 2)
				/ context.GRIDPIXELSIZE * context.GRIDPIXELSIZE);
	}

	@Override
	public void setHeight(float height) {
		super.setHeight(((int) height + context.GRIDPIXELSIZE / 2)
				/ context.GRIDPIXELSIZE * context.GRIDPIXELSIZE);
	}

	public void setPlaceLocation(String location) {
		location = location.toLowerCase();

		switch (location) {
		case "floor":
			this.placeLocation = PlaceLocation.FLOOR;
			break;
		case "ceiling":
			this.placeLocation = PlaceLocation.CEILING;
			break;
		case "wall":
			this.placeLocation = PlaceLocation.WALL;
			break;
		default:
			System.out.println("Are you sane?");
		}
	}

	public void setProp(String prop) {
		prop = prop.toLowerCase();
		switch (prop) {
		case "explosive":
			this.objectProp = ObjectProp.EXPLOSIVE;
			break;
		case "distinguisher":
			this.objectProp = ObjectProp.DISTINGUISHER;
			break;
		case "normal":
			this.objectProp = ObjectProp.NORMAL;
			break;
		default:
			System.out
					.println("Are you sane? Setting " + prop + " to default.");
			this.objectProp = ObjectProp.NORMAL;
		}
	}

	public void setResist(int resist) {
		this.resist = resist;
	}

	public void setFlameCnt(int flameCnt) {
		this.flameCnt = flameCnt;
	}

	public void distinguish() {
		this.burningFlag = false;
	}

	public String getObjectType() {
		return objectType;
	}

	public ObjectProp getProp() {
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

	public boolean isExploding() {
		return exploding;
	}

	public boolean isDistinguishing() {
		return distinguishing;
	}

	public boolean isLeaveRuin() {
		return leaveRuin;
	}

	public void decreaseFlameCnt() {
		this.flameCnt--;
	}

	public boolean isItNear(GameObject obj) {
		float x = obj.getX();
		float y = obj.getY();
		float width = obj.getWidth();
		float height = obj.getHeight();

		if ((x + width + range > this.getX()
				&& x - range < this.getX() + this.getWidth()
				&& y + height + range > this.getY() && y - range < this.getY()
				+ this.getHeight()))
			return true;

		return false;
	}

	public void setFire() {
		burningFlag = true;
	}
}
