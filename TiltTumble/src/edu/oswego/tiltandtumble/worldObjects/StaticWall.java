package edu.oswego.tiltandtumble.worldObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Disposable;

import edu.oswego.tiltandtumble.collisionListener.BallCollisionListener;
import edu.oswego.tiltandtumble.levels.Level;
import edu.oswego.tiltandtumble.worldObjects.graphics.GraphicComponent;

public class StaticWall extends AbstractWorldObject implements WorldUpdateable,
		MapRenderable, BallCollisionListener, Audible, Disposable {
    public static final float FRICTION = 0.5f;
    public static final float DENSITY = 5.0f;
    public static final float RESTITUTION = 0.0f;
    public static final BodyType BODY_TYPE = BodyType.StaticBody;

	private boolean collidingWithBall = false;
	private Ball ball;
    private final Level level;

	private final GraphicComponent deathGraphic;
	private boolean death = false;
	private boolean playSound;
	private final Sound deathSound;

	public StaticWall(Body body, Level level, GraphicComponent deathGraphic,
			AssetManager assetManager) {
        super(body);
		this.level = level;
		this.deathGraphic = deathGraphic;
		playSound = true;
		String soundFile = "data/soundfx/popping.ogg";
		if (!assetManager.isLoaded(soundFile)) {
			assetManager.load(soundFile, Sound.class);
			assetManager.finishLoading();
		}
		deathSound = assetManager.get(soundFile, Sound.class);
    }

	@Override
	public void handleBeginCollision(Contact contact, Ball ball) {
		collidingWithBall = true;
		this.ball = ball;
		ball.playSound();
	}

	@Override
	public void handleEndCollision(Contact contact, Ball ball) {
		collidingWithBall = false;
		this.ball = null;
	}

	@Override
	public void drawBeforeBall(float delta, SpriteBatch batch) {
	}

	@Override
	public void drawAfterBall(float delta, SpriteBatch batch) {
		if (death) {
			if (deathGraphic.isFinished()) {
				level.exit();
			}
			deathGraphic.draw(delta, batch);
		}
	}

	@Override
	public void update(float delta) {
		if (collidingWithBall) {
			if (body.getFixtureList().get(0).testPoint(ball.getBody().getPosition())) {
				Gdx.app.log("StaticWall", "Wall SMASH Ball!");
				level.fail();
				ball.hide();
				deathGraphic.setPosition(ball.getMapX(), ball.getMapY());
				playSound();
				deathGraphic.start();
				death = true;
			}
		}
	}

	@Override
	public void dispose() {
		deathGraphic.dispose();
	}

	@Override
	public void setPlaySound(boolean value) {
		playSound = value;
	}

	@Override
	public void playSound() {
		if (playSound) {
			deathSound.play();
		}
	}

	@Override
	public void endSound() {
		deathSound.stop();
	}
}
