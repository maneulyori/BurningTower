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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

public class BurningTower implements ApplicationListener, GameContext {

	private GameObject background;
	public static final int nOfFireImages = 2;
	public static Texture[] fire = new Texture[nOfFireImages];
	private Stage stage;

	private Music bgm;

	private int levelCnt;

	private Array<GameObject> gameObjects = new Array<GameObject>();
	private SpriteBatch batch;
	private Sprite backgroundSprite;
	private FileHandle levelFile;
	private JsonValue levelData;

	private BurningThread burner = new BurningThread(gameObjects, this);
	private Thread burningThread = new Thread(burner);

	private InputMultiplexer inputMultiplexer;

	public BurningTower() {
	}

	@Override
	public void create() {
		GameObject.range = 50;
		Texture.setEnforcePotImages(false);

		levelFile = Gdx.files.internal("data/levelData/level.json");
		levelData = new JsonReader().parse(levelFile);

		System.out.println(levelData);
		batch = new SpriteBatch();

		background = new GameObject();
		background.setObjType("building", false);
		background.setPosition(0, 0);
		/*
		 * for (String str : objectStr) objTextures.put(str, new
		 * Texture(Gdx.files.internal("data/image/" + str + ".png")));
		 */
		DragAndDrop dragAndDrop = new DragAndDrop();
		stage = new Stage();

		stage.addActor(background);

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

			object.addListener(new DragListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y,
						int pointer, int button) {
					System.out.println("CLICK");
					return true;
				}

				@Override
				public void touchDragged(InputEvent event, float x, float y,
						int pointer) {
					object.setOrigin(Gdx.input.getX(), Gdx.input.getY());
					object.setPosition(object.getX() - object.getWidth() / 2 + x, object.getY() - object.getHeight() / 2 + y);
				}
			});
			stage.addActor(object);
			gameObjects.add(object);
		}

		inputMultiplexer = new InputMultiplexer(stage);
		// inputMultiplexer.addProcessor(1, stage);
		Gdx.input.setInputProcessor(inputMultiplexer);

		/**** TEST CODE ****/

		burner.setFire(130, 10);

		ScheduledExecutorService worker = Executors
				.newSingleThreadScheduledExecutor();

		worker.schedule(burningThread, 10, TimeUnit.SECONDS);
		// burningThread.start();
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

		batch.begin();

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);

		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
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
