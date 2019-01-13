package edu.oswego.tiltandtumble.worldObjects;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

import edu.oswego.tiltandtumble.levels.UnitScale;
import edu.oswego.tiltandtumble.worldObjects.graphics.GraphicComponent;

public class Ball extends AbstractWorldObject implements Disposable, Audible {
	public static final float FRICTION = 0.1f;
	public static final float DENSITY = 1.0f;
	public static final float RESTITUTION = 0.5f;
	public static final BodyType BODY_TYPE = BodyType.DynamicBody;
	public static final float ANGULAR_DAMPENING = 0.1f;
	public static final float LINEAR_DAMPENING = 0.1f;

	private final GraphicComponent graphic;
	private boolean visible = true;

	private final UnitScale scale;
	private boolean playSound;
	private final Sound sound;

	public Ball(Body body, GraphicComponent graphic, UnitScale scale, AssetManager assetManager) {
		super(body);
		this.scale = scale;

		this.graphic = graphic;

		playSound = true;
		String soundFile = "data/soundfx/boing1.ogg";
		if (!assetManager.isLoaded(soundFile)) {
			assetManager.load(soundFile, Sound.class);
			assetManager.finishLoading();
		}
		sound = assetManager.get(soundFile, Sound.class);
	}

	public void applyLinearImpulse(float x, float y) {
		body.applyLinearImpulse(x, y, body.getPosition().x,
				body.getPosition().y, true);
	}

	public void draw(float delta, SpriteBatch batch) {
		if (visible) {
			graphic.setPosition(getMapX(), getMapY());
			graphic.draw(delta, batch);
		}
	}

	public float getRadius() {
		return scale.metersToPixels(
				body.getFixtureList().get(0).getShape().getRadius());
	}

	public float getMapX() {
		return scale.metersToPixels(body.getPosition().x);
	}

	public float getMapY() {
		return scale.metersToPixels(body.getPosition().y);
	}

	public void hide() {
		visible = false;
	}

	public void show() {
		visible = true;
	}

	@Override
	public void dispose() {
		graphic.dispose();
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
}
