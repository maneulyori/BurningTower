package net.kucatdog.burningtower.main;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;

public class BurningTower implements ApplicationListener, GameContext {

	private final int VIRTUAL_WIDTH = 768;
	private final int VIRTUAL_HEIGHT = 1280;
	private GameObject background;
	public static final int nOfFireImages = 2;
	public static Texture[] fire = new Texture[nOfFireImages];
	private Stage stage;

	private Music bgm;

	private int levelCnt;

	public static Array<GameObject> gameObjects = new Array<GameObject>();
	private SpriteBatch batch;
	private Sprite backgroundSprite;
	private FileHandle levelFile;
	private JsonValue levelData;

	private BurningThread burner = new BurningThread(gameObjects, this);
	private Thread burningThread = new Thread(burner);

	private InputMultiplexer inputMultiplexer;

	private OrthographicCamera cam;

	public BurningTower() {
	}

	@Override
	public void create() {
		GameObject.range = 50;
		Texture.setEnforcePotImages(false);
		cam = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		levelFile = Gdx.files.internal("data/levelData/level.json");
		levelData = new JsonReader().parse(levelFile);

		System.out.println(levelData);
		batch = new SpriteBatch();

		background = new GameObject();
		background.setObjType("building", false);
		background.setPosition(0, 0);

		DragAndDrop dragAndDrop = new DragAndDrop();
		stage = new Stage();
		stage.setCamera(cam);

		stage.addActor(background);

		for (int i = 0; i < nOfFireImages; i++)
			fire[i] = new Texture(Gdx.files.internal("data/image/fire"
					+ (i + 1) + ".png"));

		bgm = Gdx.audio.newMusic(Gdx.files.internal("data/audio/fire.ogg"));

		levelCnt = levelData.get("levelCnt").asInt();

		Iterator<JsonValue> levelIterator = levelData.get("1").iterator();

		while (levelIterator.hasNext()) {
			JsonValue objects = levelIterator.next();
			
			GameObject object_tmp;
			
			if(objects.get("type").asString().equals("wall")) {
				object_tmp = new WallObject();
			}
			else {
				object_tmp = new GameObject();
			}
			
			final GameObject object = object_tmp;

			//

			object.setObjType(objects.get("type").asString(),
					objects.get("leaveRuin").asBoolean());
			object.resist = objects.get("resist").asInt();
			object.setX(objects.get("locationX").asFloat());
			object.setY(objects.get("locationY").asFloat());
			object.flameCnt = objects.get("flammable").asInt();
			
			if(objects.get("width") != null)
				object.setWidth(objects.get("width").asInt());
			if(objects.get("height") != null)
				object.setHeight(objects.get("height").asInt());
			

			if (!objects.get("type").asString().equals("wall"))
				object.addListener(new DragListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						System.out.println("CLICK");
						return true;
					}

					@Override
					public void touchDragged(InputEvent event, float x,
							float y, int pointer) {
						object.setOrigin(Gdx.input.getX(), Gdx.input.getY());
						object.setPosition(object.getX() - object.getWidth()
								/ 2 + x, object.getY() - object.getHeight() / 2
								+ y);
					}
				});

			stage.addActor(object);
			gameObjects.add(object);
		}

		FireActor fireactor = new FireActor();
		stage.addActor(fireactor);

		inputMultiplexer = new InputMultiplexer(stage);
		// inputMultiplexer.addProcessor(1, stage);
		Gdx.input.setInputProcessor(inputMultiplexer);

		/**** TEST CODE ****/

		burner.setFire(130, 10);

		// ScheduledExecutorService worker = Executors
		// .newSingleThreadScheduledExecutor();

		// worker.schedule(burningThread, 10, TimeUnit.SECONDS);
		burningThread.start();
	}

	@Override
	public void dispose() {
		stage.dispose();
		batch.dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		batch.begin();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		batch.end();

		batch.begin();

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
		// TODO Auto-generated method stub

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
}
