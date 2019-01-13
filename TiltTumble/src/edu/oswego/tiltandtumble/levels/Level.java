package edu.oswego.tiltandtumble.levels;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;

import edu.oswego.tiltandtumble.collisionListener.OurCollisionListener;
import edu.oswego.tiltandtumble.data.Score;
import edu.oswego.tiltandtumble.worldObjects.Audible;
import edu.oswego.tiltandtumble.worldObjects.Ball;
import edu.oswego.tiltandtumble.worldObjects.MapRenderable;
import edu.oswego.tiltandtumble.worldObjects.WorldObject;
import edu.oswego.tiltandtumble.worldObjects.WorldUpdateable;

public class Level implements Disposable, Audible {

	// TODO: Setting default score to 1000, figure out a good value for this...
	private static final int DEFAULT_SCORE = 1000;

	private final World world = new World(new Vector2(0, 0), true);
	private final ContactListener contactListener;
	private final TiledMap map;
	private final Ball ball;
	private final BallController ballController;

	// NOTE: 1/64 means 1px end up being about 1.6cm in world physics
	private final UnitScale scale = new UnitScale(1f/64f);

	private final int level;
	private boolean failed = false;
	private State currentState;

	private final Score score;
	//times are in milliseconds
	private long startTime;
	private long pauseTime = 0;
	private final int baseScore;

	private final int mapWidth;
	private final int mapHeight;

	private boolean playSound;
	private final Sound failSound;

	private final Collection<Disposable> disposableObjects;
	private final Collection<MapRenderable> renderableObjects;
	private final Collection<WorldUpdateable> updateableObjects;
	private final Collection<Audible> audibleObjects;

	public Level(int level, String filename, BallController ballController,
			WorldPopulator populator, AssetManager assetManager) {
		this.level = level;
		this.ballController = ballController;

		currentState = State.NOT_STARTED;

		map = loadMap(filename);

		mapWidth = map.getProperties().get("width", Integer.class)
				* map.getProperties().get("tilewidth", Integer.class);
		mapHeight = (map.getProperties().get("height", Integer.class)
				* map.getProperties().get("tileheight", Integer.class))
				+ 32; // adding extra for HUD

		baseScore = map.getProperties().get("score", DEFAULT_SCORE, Integer.class);
		score = new Score(baseScore, 0);

		disposableObjects = new LinkedList<Disposable>();
		renderableObjects = new LinkedList<MapRenderable>();
		updateableObjects = new LinkedList<WorldUpdateable>();
		audibleObjects = new LinkedList<Audible>();

		audibleObjects.add(this);

		ball = populator.populateWorldFromMap(this, map, world, scale);
		this.ballController.setBall(ball);

		playSound = true;
		String soundFile = "data/soundfx/failure-2.ogg";
		if (!assetManager.isLoaded(soundFile)) {
			assetManager.load(soundFile, Sound.class);
			assetManager.finishLoading();
		}
		failSound = assetManager.get(soundFile, Sound.class);

		contactListener = new OurCollisionListener();
		world.setContactListener(contactListener);
	}

	public TiledMap getMap() {
		return map;
	}

	public World getWorld() {
		return world;
	}

	public UnitScale getScale() {
		return scale;
	}

	public Ball getBall() {
		return ball;
	}

	public Score getScore() {
		return score;
	}

	public BallController getBallController() {
		return ballController;
	}

	public Collection<Audible> getAudibles() {
		return audibleObjects;
	}

	public boolean hasNotStarted() {
		return currentState == State.NOT_STARTED;
	}

	public void addWorldObject(WorldObject obj) {
		if (obj instanceof Disposable) {
			disposableObjects.add((Disposable)obj);
		}
		if (obj instanceof WorldUpdateable) {
			updateableObjects.add((WorldUpdateable)obj);
		}
		if (obj instanceof MapRenderable) {
			renderableObjects.add((MapRenderable)obj);
		}
		if (obj instanceof Audible) {
			audibleObjects.add((Audible)obj);
		}
		// we don't care about other object types at this time.
	}

	public void start() {
		currentState.start(this);
	}

	public boolean isStarted() {
		return currentState == State.STARTED;
	}

	public void win() {
		currentState.end(this, false);
	}

	public void fail() {
		currentState.end(this, true);
	}

	public void exit() {
		currentState.finish(this);
	}

	public boolean isFailed() {
		return failed;
	}

	public boolean hasFinished() {
		return currentState == State.FINISHED;
	}

	private void updateScore() {
		// the "difference" is the difference in seconds and for every 1 second the time elapses, 10 points will be taken off
		long difference = (TimeUtils.millis() - startTime) / 1000;
		if (failed || (baseScore - difference) < 0) {
			score.setPoints(0);
		}
		else {
			score.setPoints((int)(baseScore - difference));
		}
		score.setTime((int)difference);
	}

	private TiledMap loadMap(String file) {
		if (Gdx.files.internal("data/" + file).exists()) {
			return new TmxMapLoader().load("data/" + file);
		}
		throw new RuntimeException("data/" + file + " does not exist");
	}

	public int getLevelNumber() {
		return level;
	}

	@Override
	public void setPlaySound(boolean value) {
		playSound = value;
	}

	@Override
	public void playSound() {
		if (playSound) {
			if (isFailed()) {
				failSound.play();
			}
		}
	}

	@Override
	public void endSound() {
		failSound.stop();
	}

	public void draw(float delta, SpriteBatch batch) {
		for (MapRenderable m : renderableObjects) {
			m.drawBeforeBall(delta, batch);
		}
		ball.draw(delta, batch);
		for (MapRenderable m : renderableObjects) {
			m.drawAfterBall(delta, batch);
		}
	}

	public void update(float delta) {
		currentState.update(this, delta);
	}

	private boolean isBallOutsideLevel() {
		return ball.getMapX() < 0
				|| ball.getMapX() > mapWidth
				|| ball.getMapY() < 0
				|| ball.getMapY() > mapHeight;
	}

	@Override
	public void dispose() {
		world.dispose();
		for (Disposable d : disposableObjects) {
			d.dispose();
		}
		failSound.dispose();
	}

	public void pause() {
		currentState.pause(this);
	}

	public void resume() {
		currentState.resume(this);
	}

	private void changeState(State state) {
		currentState = state;
	}

	private static enum State {
        NOT_STARTED {
    		@Override
    		public void start(Level l) {
    			l.startTime = TimeUtils.millis();
    			l.changeState(STARTED);
    		}
        },
        STARTED {
    		@Override
    		public void pause(Level l) {
    			l.pauseTime = TimeUtils.millis();
    			l.changeState(PAUSED);
    		}

    		@Override
    		public void end(Level l, boolean fail) {
    			l.failed = fail;
    			l.updateScore();
    			l.changeState(ENDING);
    			for (Audible a : l.audibleObjects) {
    				a.endSound();
    			}
    		}

    		@Override
    		public void update(Level l, float delta) {
    			if (l.isBallOutsideLevel()) {
    				l.fail();
    				l.exit();
    				return;
    			}
    			l.ballController.update(delta);
    			for (WorldUpdateable w : l.updateableObjects) {
    				w.update(delta);
    			}

    			l.updateScore();

    			// world.step(1/60f, 6, 2);
    			l.world.step(1 / 45f, 10, 8);
    		}
        },
        PAUSED {
    		@Override
    		public void resume(Level l) {
    			l.startTime += (TimeUtils.millis() - l.pauseTime);
    			l.changeState(STARTED);
    		}
        },
        ENDING {
    		@Override
			public void finish(Level l) {
    			l.changeState(FINISHED);
    			l.playSound();
    		}
        },
        FINISHED;

        public void start(Level l) {}
		public void pause(Level l) {}
		public void resume(Level l) {}
		public void end(Level l, boolean fail) {}
		public void finish(Level l) {}
		public void update(Level l, float delta) {}
    };
}
