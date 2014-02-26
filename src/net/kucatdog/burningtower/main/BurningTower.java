package net.kucatdog.burningtower.main;

import java.util.Iterator;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class BurningTower extends GameScreen implements Screen {

	private final BurningTower self = this;

	public Texture[] fire = new Texture[2];

	public final int GRIDPIXELSIZE = 40;
	// TODO: Read it from config file

	public static boolean dragLock = false;

	private Music bgm;

	private int level;

	private BitmapFont scoreFont;
	private BitmapFont timerFont;
	private Label scoreLabel;
	private Label timerLabel;

	private CountdownTimer fireTimer;
	private Thread timerThread;

	public Array<GameObject> gameObjects = new Array<GameObject>();
	private Array<StoreyObject> storeys = new Array<StoreyObject>();

	private JsonValue levelData;

	private PyroActor pyro;

	public BurningTower(MainMenu game, int level) {
		super(game);

		this.level = level;

		GameObject.range = 80;
		Texture.setEnforcePotImages(false);

		levelData = new JsonReader().parse(game.levelFile);

		System.out.println(levelData); // print parsed level.json

		scoreFont = new BitmapFont();
		timerFont = new BitmapFont();

		for (int i = 0; i < fire.length; i++)
			fire[i] = new Texture(Gdx.files.internal("data/image/fire"
					+ (i + 1) + ".png"));

		bgm = Gdx.audio.newMusic(Gdx.files.internal("data/audio/fire.ogg"));

		fireTimer = new CountdownTimer(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		gameObjects.clear();
		storeys.clear();
		scoreFont.dispose();
		timerFont.dispose();

		for (int i = 0; i < fire.length; i++) {
			fire[i].dispose();
		}
	}

	@Override
	public void show() {
		super.show();

		Iterator<JsonValue> levelIterator = levelData.get(
				Integer.toString(level)).iterator();

		StoreyObject storey = new StoreyObject(this);
		storey.setBounds(60, 10, 600, 300);
		stage.addActor(storey);
		storeys.add(storey);

		storey = new StoreyObject(this);
		storey.setBounds(60, 310, 600, 300);
		stage.addActor(storey);
		storeys.add(storey);

		Actor roof = new Actor() {
			Texture texture = new Texture(
					Gdx.files.internal("data/image/roof.png"));

			@Override
			public void draw(Batch batch, float alpha) {
				super.draw(batch, alpha);

				batch.draw(texture, this.getX(), this.getY(), this.getWidth(),
						this.getHeight());
			}

			@Override
			public void act(float deltaTime) {
			}
		};

		roof.setBounds(40, 610, 640, 100);
		stage.addActor(roof);

		while (levelIterator.hasNext()) {
			JsonValue objects = levelIterator.next();

			final GameObject object = new GameObject(this);

			object.setObjType(objects.get("type").asString(),
					objects.get("leaveRuin").asBoolean());
			object.setResist(objects.get("resist").asInt());
			object.setX(objects.get("locationX").asFloat());
			object.setY(objects.get("locationY").asFloat());
			object.setFlameCnt(objects.get("flammable").asInt());

			if (objects.get("width") != null)
				object.setWidth(objects.get("width").asInt());
			if (objects.get("height") != null)
				object.setHeight(objects.get("height").asInt());

			if (objects.get("isMovable") == null
					|| objects.get("isMovable").asBoolean()) {
				object.addListener(new DragListener() {
					float deltax;
					float deltay;
					float firstx;
					float firsty;
					float firstgrep_x;
					float firstgrep_y;
					float original_x;
					float original_y;

					StoreyObject thisStorey = null;

					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						System.out.println("CLICK");

						for (StoreyObject obj : storeys) {
							if (object.getX() + object.getWidth() > obj.getX()
									&& object.getX() < obj.getX()
											+ obj.getWidth()
									&& object.getY() + object.getHeight() > obj
											.getY()
									&& object.getY() < obj.getY()
											+ obj.getHeight()) {
								thisStorey = obj;
								System.out.println(thisStorey.toString());
								break;
							}
						}

						object.setX((int) (object.getX() - thisStorey.getX())
								/ GRIDPIXELSIZE * GRIDPIXELSIZE
								+ thisStorey.getX());
						object.setY((int) (object.getY() - thisStorey.getY())
								/ GRIDPIXELSIZE * GRIDPIXELSIZE
								+ thisStorey.getY());

						deltax = 0;
						deltay = 0;
						firstx = object.getX();
						firsty = object.getY();

						firstgrep_x = x;
						firstgrep_y = y;

						original_x = firstx;
						original_y = firsty;

						return true;
					}

					@Override
					public void touchDragged(InputEvent event, float x,
							float y, int pointer) {

						if (!BurningTower.dragLock) {
							object.setOrigin(Gdx.input.getX(), Gdx.input.getY());

							deltax = x - firstgrep_x;
							deltay = y - firstgrep_y;

							if (Math.abs(deltax) >= 40) {
								object.setX(firstx + (int) deltax
										/ GRIDPIXELSIZE * GRIDPIXELSIZE);

								firstx = object.getX();
							}

							if (Math.abs(deltay) >= 40) {

								object.setY(firsty + (int) deltay
										/ GRIDPIXELSIZE * GRIDPIXELSIZE);

								firsty = object.getY();
							}
						}
					}

					@Override
					public void touchUp(InputEvent event, float x, float y,
							int pointer, int button) {
						// Check object collapses.

						for (GameObject obj : gameObjects) {

							if (obj.equals(object))
								continue;

							if (object.getX() + object.getWidth() > obj.getX()
									&& object.getX() < obj.getX()
											+ obj.getWidth()
									&& object.getY() + object.getHeight() > obj
											.getY()
									&& object.getY() < obj.getY()
											+ obj.getHeight()) {
								System.out.println("COLLIDE! with "
										+ obj.getObjectType());
								object.setPosition(original_x, original_y);
							}
						}

						if (!(object.getX() + object.getWidth() > thisStorey
								.getX()
								&& object.getX() < thisStorey.getX()
										+ thisStorey.getWidth()
								&& object.getY() + object.getHeight() > thisStorey
										.getY() && object.getY() < thisStorey
								.getY() + thisStorey.getHeight())) {
							System.out.println("You cannot leave storey!");
							object.setPosition(original_x, original_y);
						}
					}
				});
			}

			stage.addActor(object);
			gameObjects.add(object);

		}

		FireActor fireactor = new FireActor(this);
		stage.addActor(fireactor);

		pyro = new PyroActor(this);

		pyro.setPosition(700, 10);
		pyro.setFirePt(130, 10);
		pyro.setScale((float) 1.5);
		stage.addActor(pyro);

		final Image fireButton = new Image();
		fireButton
				.setDrawable(new TextureRegionDrawable(new TextureRegion(
						new Texture(Gdx.files
								.internal("data/image/fire_button.png")))));
		fireButton.setX(0);
		fireButton.setY(0);

		fireButton.setWidth(50);
		fireButton.setHeight(50);

		fireButton.addListener(new DragListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				System.out.println("FIRE START");

				self.startFire();

				return true;
			}
		});

		stage.addActor(fireButton);

		timerFont.setScale(2);
		scoreFont.setScale(2);

		timerLabel = new Label("Aiya, ambar!", new Label.LabelStyle(timerFont,
				Color.WHITE));
		timerLabel.setPosition(260, 1240);
		stage.addActor(timerLabel);

		scoreLabel = new Label("Aiya, ambar!", new Label.LabelStyle(scoreFont,
				Color.WHITE));
		scoreLabel.setPosition(260, 1200);
		stage.addActor(scoreLabel);

		while (timerThread != null && timerThread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		timerThread = new Thread(fireTimer);

		fireTimer.clearTerminate();
		fireTimer.resume();
		timerThread.start();
	}

	@Override
	public void hide() {
		super.hide();
		fireTimer.terminate(); // Terminate fire timer.
		gameObjects.clear();
		storeys.clear();
	}

	@Override
	public void pause() {
		super.pause();
		fireTimer.pause();
	}

	@Override
	public void resume() {
		super.resume();
		fireTimer.resume();
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		int notburn = 0;
		boolean isBurning = false;

		for (GameObject obj : gameObjects) {
			if (obj.isBurning()) {
				isBurning = true;
			} else if (!(obj.isBurning() || obj.isBurnt())) {
				notburn++;
			}
		}

		for (StoreyObject obj : storeys) {
			if (obj.isBurning()) {
				isBurning = true;
			} else if (!(obj.isBurning() || obj.isBurnt())) {
				notburn++;
			}
		}

		if ((!isBurning) && dragLock) {
			dragLock = false;
			this.stopBurning();
		}

		if (gameObjects.size != 0) {
			timerLabel.setText(fireTimer.getTimerStr());
			scoreLabel.setText("Burnt " + 100
					* (gameObjects.size + storeys.size - 1 - notburn)
					/ (gameObjects.size + storeys.size - 1) + "%");
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	public void stopBurning() {
		int notburn = 0;

		stopBGM();

		for (GameObject obj : gameObjects) {
			if (!(obj.isBurning() || obj.isBurnt())) {
				notburn++;
			}
		}

		game.scoreScreen.setScore(100 * (gameObjects.size - notburn)
				/ gameObjects.size);
		game.setScreen(game.scoreScreen);
	}

	public void playBGM() {
		bgm.setLooping(true);
		bgm.play();
	}

	public void stopBGM() {
		bgm.stop();
	}

	public boolean isBGMPlaying() {
		return bgm.isPlaying();
	}

	public void startFire() {
		pyro.burnIt();
		fireTimer.setTime(0);
	}
}
