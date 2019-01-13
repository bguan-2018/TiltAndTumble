package edu.oswego.tiltandtumble.worldObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Disposable;

import edu.oswego.tiltandtumble.collisionListener.BallCollisionListener;
import edu.oswego.tiltandtumble.levels.Level;

public class FinishLine extends AbstractWorldObject
		implements BallCollisionListener, Disposable, Audible  {
    public static final BodyType BODY_TYPE = BodyType.StaticBody;
    public static final boolean IS_SENSOR = true;

    private final Level level;

	private boolean playSound;
	private final Sound sound;

	public FinishLine(Body body, Level level, AssetManager assetManager) {
		super(body);
		this.level = level;

		playSound = true;
		String soundFile = "data/soundfx/finishLine.ogg";
		if (!assetManager.isLoaded(soundFile)) {
			assetManager.load(soundFile, Sound.class);
			assetManager.finishLoading();
		}
		sound = assetManager.get(soundFile, Sound.class);
	}

	/**
	 * This will end the level as soon as the ball makes contact with this object
	 * so you may want the collision object on the map to be smaller than its
	 * graphical representation so that it looks like the ball enters or crosses
	 * a finish area rather than just touches it.
	 */
	@Override
	public void handleBeginCollision(Contact contact, Ball ball) {
		Gdx.app.log("FinishLine", "Ball enter");
		level.win();
		playSound();
		level.exit();
	}

	@Override
	public void handleEndCollision(Contact contact, Ball ball) {
	}

	@Override
	public void setPlaySound(boolean value) {
		playSound = value;
	}

	@Override
	public void playSound() {
		if (playSound) {
			sound.play();
		}
	}

	@Override
	public void endSound() {
		sound.stop();
	}

	@Override
	public void dispose() {
	}
}
