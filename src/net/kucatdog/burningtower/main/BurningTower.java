package net.kucatdog.burningtower.main;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class BurningTower implements ApplicationListener, GameContext {

	private final BurningTower self = this;

	private final int VIRTUAL_WIDTH = 768;
	private final int VIRTUAL_HEIGHT = 1280;
	// private GameObject background;
	public static final int nOfFireImages = 2;
	public static Texture[] fire = new Texture[nOfFireImages];

	public final int GRIDPIXELSIZE = 40;
	// TODO: Read it from config file

	public static boolean dragLock = false;

	private Stage stage;

	private Music bgm;

	private int levelCnt;

	private BitmapFont scoreFont;
	private BitmapFont timerFont;

	private CountdownTimer fireTimer;

	public static Array<GameObject> gameObjects = new Array<GameObject>();
	private Array<StoreyObject> storeys = new Array<StoreyObject>();

	private SpriteBatch batch;
	private FileHandle levelFile;
	private JsonValue levelData;

	private InputMultiplexer inputMultiplexer;

	private OrthographicCamera cam;

	private PyroActor pyro;

	public BurningTower() {
	}

	@Override
	public void create() {
		GameObject.range = 80;
		Texture.setEnforcePotImages(false);
		cam = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		levelFile = Gdx.files.internal("data/levelData/level.json");
		levelData = new JsonReader().parse(levelFile);

		System.out.println(levelData);
		batch = new SpriteBatch();

		scoreFont = new BitmapFont();
		timerFont = new BitmapFont();

		// TODO: Add roof

		stage = new Stage();
		stage.setCamera(cam);

		StoreyObject storey = new StoreyObject();
		storey.setBounds(60, 10, 600, 300);
		stage.addActor(storey);
		storeys.add(storey);

		storey = new StoreyObject();
		storey.setBounds(60, 310, 600, 300);
		stage.addActor(storey);
		storeys.add(storey);

		for (int i = 0; i < nOfFireImages; i++)
			fire[i] = new Texture(Gdx.files.internal("data/image/fire"
					+ (i + 1) + ".png"));

		bgm = Gdx.audio.newMusic(Gdx.files.internal("data/audio/fire.ogg"));

		levelCnt = levelData.get("levelCnt").asInt();

		Iterator<JsonValue> levelIterator = levelData.get("1").iterator();

		while (levelIterator.hasNext()) {
			JsonValue objects = levelIterator.next();

			final GameObject object = new GameObject();

			object.setObjType(objects.get("type").asString(),
					objects.get("leaveRuin").asBoolean());
			object.resist = objects.get("resist").asInt();
			object.setX(objects.get("locationX").asFloat());
			object.setY(objects.get("locationY").asFloat());
			object.flameCnt = objects.get("flammable").asInt();

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
										+ obj.objectType);
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

		FireActor fireactor = new FireActor();
		stage.addActor(fireactor);

		// Pyro!
		// burner.setFire(130, 10);

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

		fireTimer = new CountdownTimer(this);

		Thread timerThread = new Thread(fireTimer);

		timerThread.start();

		inputMultiplexer = new InputMultiplexer(stage);
		// inputMultiplexer.addProcessor(1, stage);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void dispose() {
		stage.dispose();
		batch.dispose();
	}

	@Override
	public void pause() {

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		int notburn = 0;
		boolean isBurning = false;

		batch.begin();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

		batch.end();

		for (GameObject obj : gameObjects) {
			if (!(obj.isBurning || obj.isBurnt)) {
				notburn++;
			}
			if (obj.isBurning) {
				isBurning = true;
			}
		}

		if ((!isBurning) && dragLock)
			this.stopBGM();

		batch.begin();

		float scale = 1 / cam.zoom * 2;

		timerFont.setScale(scale);
		scoreFont.setScale(scale);

		timerFont.draw(batch, fireTimer.getTimerStr(),
				Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() - 10
						* scale);
		scoreFont.draw(batch, "Burnt " + 100 * (gameObjects.size - notburn)
				/ gameObjects.size + "%", Gdx.graphics.getWidth() / 2 - 50,
				Gdx.graphics.getHeight() - 25 * scale);

		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportHeight = height; // set the viewport
		cam.viewportWidth = width;
		if (VIRTUAL_WIDTH / cam.viewportWidth < VIRTUAL_HEIGHT
				/ cam.viewportHeight) {
			// sett the right zoom direct
			cam.zoom = VIRTUAL_HEIGHT / cam.viewportHeight;
		} else {
			// sett the right zoom direct
			cam.zoom = VIRTUAL_WIDTH / cam.viewportWidth;
		}
		cam.position.set(cam.zoom * cam.viewportWidth / 2.0f, cam.zoom
				* cam.viewportHeight / 2.0f, 0);
		cam.update();
	}

	@Override
	public void resume() {

	}

	@Override
	public void playBGM() {
		bgm.setLooping(true);
		bgm.play();
	}

	@Override
	public void stopBGM() {
		bgm.stop();
	}

	@Override
	public boolean isBGMPlaying() {
		return bgm.isPlaying();
	}

	@Override
	public void startFire() {
		pyro.burnIt();
		fireTimer.setTime(0);
	}
}
