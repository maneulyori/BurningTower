package net.kucatdog.burningtower.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class BurningTowerScreen extends GameScreen implements Screen {

	private final BurningTowerScreen self = this;

	public Texture[] fire = new Texture[2];

	public final int GRIDPIXELSIZE = 32;
	// TODO: Read it from config file

	float fireRange;
	float gameTick;
	float distinguish_x, distinguish_y;

	private boolean dragLock = false;

	private String level;

	private BitmapFont bitmapFont;
	private Label scoreLabel;
	private Label timerLabel;
	private Label counterLabel;
	private final Image fireButton = new Image();
	private final Image crosshair = new Image();
	private int moveCnt;

	CountdownTimer fireTimer;
	private Thread timerThread;

	public Array<GameObject> gameObjects = new Array<GameObject>();
	Array<StoreyObject> storeys = new Array<StoreyObject>();

	private JsonValue jsonData;
	private JsonValue levelData;

	PyroActor pyro;
	FireActor fireactor;

	ObjectDisplayer objectDisplayer;

	class ObjectDisplayer implements Runnable {

		int delay = 0;
		ArrayList<Actor> actorList = new ArrayList<Actor>();
		Thread timer;
		boolean skip = false;

		@Override
		public void run() {
			for (Actor actor : actorList) {

				if (actor instanceof StoreyObject) {
					stage.addActor(actor);

					if (!skip) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

			for (Actor actor : actorList) {
				if (!(actor instanceof StoreyObject)) {
					stage.addActor(actor);
				}
			}

			drawUI();

			timer.start();

		}

		public void setDelay(int delay) {
			this.delay = delay;
		}

		public void addActor(Actor actor) {
			actorList.add(actor);
		}

		public void setTimerThread(Thread timer) {
			this.timer = timer;
		}

		public void setSkip() {
			skip = true;
		}

		public boolean getSkip() {
			return skip;
		}
	}

	public BurningTowerScreen(BurningTower game, String level) {
		super(game);

		this.level = level;

		Texture.setEnforcePotImages(false);

		jsonData = new JsonReader().parse(game.levelFile);

		fireRange = jsonData.get("defaultRange").asFloat();
		gameTick = jsonData.get("gameTick").asFloat();
		distinguish_x = jsonData.getFloat("distinguish_x");
		distinguish_y = jsonData.getFloat("distinguish_y");

		System.out.println(jsonData); // print parsed level.json

		bitmapFont = new BitmapFont();
		bitmapFont.setScale(2);

		for (int i = 0; i < fire.length; i++)
			fire[i] = new Texture(Gdx.files.internal("data/image/fire"
					+ (i + 1) + ".png"));

		fireTimer = new CountdownTimer(this);
		objectDisplayer = new ObjectDisplayer();
	}

	void overrideObjectDisplayer(ObjectDisplayer objectDisplayer) {
		this.objectDisplayer = objectDisplayer;
	}

	void drawUI() {

		fireButton.setPosition(200, 1200);
		fireButton.setSize(50, 50);
		stage.addActor(fireButton);

		timerLabel.setPosition(260, 1240);
		stage.addActor(timerLabel);

		scoreLabel.setPosition(260, 1200);
		stage.addActor(scoreLabel);

		counterLabel.setPosition(260, 1160);
		stage.addActor(counterLabel);

		crosshair.setSize(64, 64);
		crosshair.setPosition(64, 64);

		stage.addActor(crosshair);

	}

	@Override
	public void dispose() {
		super.dispose();
		gameObjects.clear();
		storeys.clear();
		bitmapFont.dispose();

		for (int i = 0; i < fire.length; i++) {
			fire[i].dispose();
		}
	}

	@Override
	public void show() {
		super.show();

		game.stopAllAudio();
		game.playAudio("gameplay");

		Thread objectDisplayerThread = new Thread(objectDisplayer);

		objectDisplayer.setDelay(500);

		levelData = jsonData.get(level);
		Iterator<JsonValue> levelIterator = levelData.iterator();

		moveCnt = levelData.getInt("moveCount");

		JsonValue buildingData = levelData.get("building");
		JsonValue storeyData = buildingData.get("storey");

		StoreyObject storey = null;
		int previousHeight = storeyData.getInt("locationY");

		for (int i = 0; i < buildingData.getInt("floorcnt"); i++) {
			storey = new StoreyObject(this);
			storey.setFlammable(storeyData.getInt("flammable"));
			storey.setResist(storeyData.getInt("resist"));
			storey.setBounds(storeyData.getInt("locationX"), previousHeight,
					storeyData.getInt("width"), storeyData.getInt("height"));

			previousHeight += storeyData.getInt("height")
					+ storey.getFloorHeight();
			objectDisplayer.addActor(storey);
			storeys.add(storey);
		}

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

		roof.setBounds(storey.getX() - 40, storey.getY() + storey.getHeight(),
				storey.getWidth() + 80, 100);
		objectDisplayer.addActor(roof);

		while (levelIterator.hasNext()) {
			JsonValue objects = levelIterator.next();

			if (objects.name.startsWith("object")) {

				final GameObject object = new GameObject(this);

				object.setObjType(objects.getString("type"),
						objects.getBoolean("leaveRuin"));
				object.setResist(objects.getInt("resist"));
				object.setX(objects.getFloat("locationX"));
				object.setY(objects.getFloat("locationY"));
				object.setFlameCnt(objects.getInt("flammable"));

				if (objects.get("width") != null)
					object.setWidth(objects.getInt("width"));
				if (objects.get("height") != null)
					object.setHeight(objects.getInt("height"));
				if (objects.get("property") != null)
					object.setProp(objects.getString("property"));
				if (objects.get("placelocation") != null)
					object.setPlaceLocation(objects.getString("placelocation"));

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
								if (object.getX() + object.getWidth() > obj
										.getX()
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

							System.out.println(thisStorey);
							object.setX((int) (object.getX() - thisStorey
									.getX())
									/ GRIDPIXELSIZE
									* GRIDPIXELSIZE
									+ thisStorey.getX());
							object.setY((int) (object.getY() - thisStorey
									.getY())
									/ GRIDPIXELSIZE
									* GRIDPIXELSIZE
									+ thisStorey.getY());

							deltax = 0;
							deltay = 0;
							firstx = object.getX();
							firsty = object.getY();

							firstgrep_x = x;
							firstgrep_y = y;

							original_x = firstx;
							original_y = firsty;

							if (moveCnt <= 0)
								return false;

							if (!dragLock)
								return true;
							else
								return false;
						}

						@Override
						public void touchDragged(InputEvent event, float x,
								float y, int pointer) {

							if (!dragLock) {
								object.setOrigin(Gdx.input.getX(),
										Gdx.input.getY());

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

							System.out.println("Object "
									+ object.getObjectType() + " X: "
									+ object.getX() + " Y: " + object.getY());

							for (GameObject obj : gameObjects) {

								if (obj.equals(object))
									continue;

								if (object.getX() + object.getWidth() > obj
										.getX()
										&& object.getX() < obj.getX()
												+ obj.getWidth()
										&& object.getY() + object.getHeight() > obj
												.getY()
										&& object.getY() < obj.getY()
												+ obj.getHeight()) {
									System.out.println("COLLIDE! with "
											+ obj.getObjectType());
									object.setPosition(original_x, original_y);

									return;
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

								return;
							}

							switch (object.getPlaceLocation()) {
							case FLOOR:
								if (Math.abs(thisStorey.getY() - object.getY()) > 5) {
									System.out
											.println("You cannot make this object fly!");
									object.setPosition(original_x, original_y);
									return;
								}
								break;
							case CEILING:
								if (Math.abs(thisStorey.getY()
										+ thisStorey.getHeight()
										- object.getY() - object.getHeight()) > 5) {
									System.out
											.println("You cannot detach this object from ceiling");
									object.setPosition(original_x, original_y);
									return;
								}
								break;
							case WALL:
								// DO NOTHING
								break;
							default:
								System.out.println("Are you sane?");

							}

							moveCnt--;
						}
					});
				}

				objectDisplayer.addActor(object);
				gameObjects.add(object);

			}
		}

		fireactor = new FireActor(this);
		objectDisplayer.addActor(fireactor);

		pyro = new PyroActor(this);

		pyro.setPosition(700, 10);
		pyro.setFirePt(130, 10);
		pyro.setScale((float) 1.5);
		objectDisplayer.addActor(pyro);

		fireButton
				.setDrawable(new TextureRegionDrawable(new TextureRegion(
						new Texture(Gdx.files
								.internal("data/image/fire_button.png")))));

		fireButton.addListener(new DragListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				System.out.println("FIRE START");

				self.startFire();

				return true;
			}
		});

		crosshair.setDrawable(new TextureRegionDrawable(new TextureRegion(
				new Texture(Gdx.files.internal("data/image/crosshair.png")))));

		crosshair.addListener(new DragListener() {

			float deltax;
			float deltay;
			float firstx;
			float firsty;
			float firstgrep_x;
			float firstgrep_y;
			float original_x;
			float original_y;

			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {

				deltax = 0;
				deltay = 0;
				firstx = crosshair.getX();
				firsty = crosshair.getY();

				firstgrep_x = x;
				firstgrep_y = y;

				original_x = firstx;
				original_y = firsty;

				return true;
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y,
					int pointer) {

				crosshair.setOrigin(Gdx.input.getX(), Gdx.input.getY());

				deltax = x - firstgrep_x;
				deltay = y - firstgrep_y;

				crosshair.setX(firstx + deltax);
				firstx = crosshair.getX();
				crosshair.setY(firsty + deltay);
				firsty = crosshair.getY();
			}

			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				if (crosshair.getY() > pyro.getHeight() + pyro.getY()) {
					crosshair.setPosition(original_x, original_y);
					return;
				}

				pyro.setFirePt(crosshair.getX() + crosshair.getWidth() / 2,
						crosshair.getY() + crosshair.getHeight() / 2);
			}

		});

		timerLabel = new Label("PLACEHOLDER", new Label.LabelStyle(bitmapFont,
				Color.WHITE));
		scoreLabel = new Label("PLACEHOLDER", new Label.LabelStyle(bitmapFont,
				Color.WHITE));
		counterLabel = new Label("PLACEHOLDER", new Label.LabelStyle(
				bitmapFont, Color.WHITE));

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
		objectDisplayer.setTimerThread(timerThread);
		objectDisplayerThread.start();
	}

	@Override
	public void hide() {
		super.hide();
		fireTimer.terminate(); // Terminate fire timer.
		gameObjects.clear();
		storeys.clear();
		objectDisplayer.actorList.clear();
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

		if (Gdx.input.justTouched()) {
			if (!objectDisplayer.getSkip())
				objectDisplayer.setSkip();
		}

		int notburn = 0;
		int distinguisher = 0;
		boolean isBurning = false;

		for (GameObject obj : gameObjects) {
			if (obj.isBurning()) {
				isBurning = true;
			}

			if (!obj.isBurnt()
					&& obj.getProp() != GameObject.ObjectProp.DISTINGUISHER) {
				notburn++;
			} else if (obj.getProp() == GameObject.ObjectProp.DISTINGUISHER) {
				distinguisher++;
			}
		}

		for (StoreyObject obj : storeys) {
			if (obj.isBurning()) {
				isBurning = true;
			}
		}

		if ((!isBurning) && dragLock) {
			dragLock = false;
			this.stopBurning();
		}

		if (gameObjects.size != 0) {
			timerLabel.setText(fireTimer.getTimerStr());
			scoreLabel.setText("Burnt " + 100
					* (gameObjects.size - notburn - distinguisher)
					/ (gameObjects.size - distinguisher) + "%");
			counterLabel.setText(moveCnt + " moves left.");
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	public void stopBurning() {
		int notburn = 0;
		int distinguisher = 0;

		game.stopAudio("fire");
		game.playAudio("gameplay");

		for (GameObject obj : gameObjects) {
			if (!obj.isBurnt()
					&& obj.getProp() != GameObject.ObjectProp.DISTINGUISHER) {
				notburn++;
			} else if (obj.getProp() == GameObject.ObjectProp.DISTINGUISHER) {
				distinguisher++;
			}
		}

		game.scoreScreen.setScore(100
				* (gameObjects.size - notburn - distinguisher)
				/ (gameObjects.size - distinguisher));

		final int score = 100 * (gameObjects.size - notburn - distinguisher)
				/ (gameObjects.size - distinguisher);

		ScheduledExecutorService worker = Executors
				.newSingleThreadScheduledExecutor();

		final Texture windowBg = new Texture(
				Gdx.files.internal("data/image/side_block.png"));

		worker.schedule(new Runnable() {

			@Override
			public void run() {
				WindowStyle windowstyle = new Window.WindowStyle();
				windowstyle.background = new TiledDrawable(new TextureRegion(
						windowBg));
				windowstyle.titleFont = bitmapFont;
				windowstyle.titleFontColor = Color.BLACK;
				Window window = new Window("Game completed!", windowstyle);

				window.pad(30);

				window.setPosition(200, VIRTUAL_HEIGHT / 2);

				Label label = new Label(score
						+ "% burned.\nTouch to go main screen",
						new Label.LabelStyle(bitmapFont, Color.BLACK));

				window.add(label);

				window.pack();

				window.addListener(new DragListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						game.setScreen(game.scoreScreen);

						return true;
					}
				});

				window.setPosition((VIRTUAL_WIDTH - window.getWidth()) / 2,
						VIRTUAL_HEIGHT / 2);

				stage.addActor(window);
			}
		}, 2000, TimeUnit.MILLISECONDS);
	}

	public void startFire() {
		pyro.burnIt();
		fireTimer.setTime(0);
	}

	public void setDragLock() {
		dragLock = true;
	}
}
