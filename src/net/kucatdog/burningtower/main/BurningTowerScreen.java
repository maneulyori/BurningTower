package net.kucatdog.burningtower.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.badlogic.gdx.scenes.scene2d.ui.Window;
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

	private Music bgm;

	private String level;

	private BitmapFont bitmapFont;
	private Label scoreLabel;
	private Label timerLabel;
	private Label counterLabel;
	private int moveCnt;

	private CountdownTimer fireTimer;
	private Thread timerThread;

	public Array<GameObject> gameObjects = new Array<GameObject>();
	private Array<StoreyObject> storeys = new Array<StoreyObject>();

	private JsonValue jsonData;
	private JsonValue levelData;

	private PyroActor pyro;
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

		bgm = Gdx.audio.newMusic(Gdx.files.internal("data/audio/fire.ogg"));

		fireTimer = new CountdownTimer(this);
		objectDisplayer = new ObjectDisplayer();
	}

	void overrideObjectDisplayer(ObjectDisplayer objectDisplayer) {
		this.objectDisplayer = objectDisplayer;
	}

	void drawUI() {
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

		timerLabel.setPosition(260, 1240);
		stage.addActor(timerLabel);

		scoreLabel.setPosition(260, 1200);
		stage.addActor(scoreLabel);

		counterLabel.setPosition(260, 1160);
		stage.addActor(counterLabel);
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

		Thread objectDisplayerThread = new Thread(objectDisplayer);

		objectDisplayer.setDelay(500);

		levelData = jsonData.get(level);
		Iterator<JsonValue> levelIterator = levelData.iterator();

		moveCnt = levelData.getInt("moveCount");

		StoreyObject storey = new StoreyObject(this);
		storey.setBounds(60, 10, 600, 250);
		objectDisplayer.addActor(storey);
		storeys.add(storey);

		storey = new StoreyObject(this);
		storey.setBounds(60, 260, 600, 250);
		objectDisplayer.addActor(storey);
		storeys.add(storey);

		storey = new StoreyObject(this);
		storey.setBounds(60, 510, 600, 250);
		objectDisplayer.addActor(storey);
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

		roof.setBounds(40, 760, 640, 100);
		objectDisplayer.addActor(roof);

		while (levelIterator.hasNext()) {
			JsonValue objects = levelIterator.next();

			if (objects.name.startsWith("object")) {

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
				if (objects.get("property") != null)
					object.setProp(objects.get("property").asString());

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
							// Check object collapses.

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

		timerLabel = new Label("PLACEHOLDER", new Label.LabelStyle(bitmapFont,
				Color.WHITE));
		scoreLabel = new Label("PLACEHOLDER", new Label.LabelStyle(bitmapFont,
				Color.WHITE));
		counterLabel = new Label("PLACEHOLDER", new Label.LabelStyle(
				bitmapFont, Color.WHITE));

		drawUI();

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
		boolean isBurning = false;

		for (GameObject obj : gameObjects) {
			if (obj.isBurning()) {
				isBurning = true;
			}

			if (!obj.isBurnt()) {
				notburn++;
			}
		}

		for (StoreyObject obj : storeys) {
			if (obj.isBurning()) {
				isBurning = true;
			}

			if (!obj.isBurnt()) {
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
					* (gameObjects.size + storeys.size - notburn)
					/ (gameObjects.size + storeys.size - 1) + "%");
			counterLabel.setText(moveCnt + " moves left.");
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
			if (!obj.isBurnt()) {
				notburn++;
			}
		}

		for (StoreyObject obj : storeys) {
			if (!obj.isBurnt()) {
				notburn++;
			}
		}

		game.scoreScreen.setScore(100
				* (gameObjects.size + storeys.size - notburn)
				/ (gameObjects.size + storeys.size - 1));

		final int score = 100 * (gameObjects.size + storeys.size - notburn)
				/ (gameObjects.size + storeys.size - 1);

		ScheduledExecutorService worker = Executors
				.newSingleThreadScheduledExecutor();

		final Texture windowBg = new Texture(
				Gdx.files.internal("data/image/side_block.png"));

		worker.schedule(new Runnable() {

			@Override
			public void run() {
				Window.WindowStyle windowstyle = new Window.WindowStyle();
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

				System.out.println(window.getWidth());

				window.setPosition((VIRTUAL_WIDTH - window.getWidth()) / 2,
						VIRTUAL_HEIGHT / 2);

				System.out.println(window.getX());

				stage.addActor(window);
			}
		}, 2000, TimeUnit.MILLISECONDS);
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

	public void setDragLock() {
		dragLock = true;
	}
}
