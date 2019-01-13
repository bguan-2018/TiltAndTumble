package edu.oswego.tiltandtumble.levels;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.oswego.tiltandtumble.worldObjects.Ball;


public class BallController extends ClickListener {

	private static final float KEY_MAX = 10;
	private static final float KEY_INCREMENT = 0.25f;

	private enum MyKeys {
		LEFT, RIGHT, UP, DOWN
	}

	private final Map<MyKeys, Boolean> keys = new EnumMap<MyKeys, Boolean>(MyKeys.class);
	private final boolean useAccelerometer;
	private Ball ball;

	private float tiltX = 0;
	private float tiltY = 0;
	private State currentState;
	private float keyX = 0;
	private float keyY = 0;

	public BallController(boolean useAccelerometer) {
		this.useAccelerometer = useAccelerometer;
		keys.put(MyKeys.LEFT, false);
		keys.put(MyKeys.RIGHT, false);
		keys.put(MyKeys.UP, false);
		keys.put(MyKeys.DOWN, false);
		currentState = State.ACTIVE;
	}

	public void setBall(Ball ball) {
		this.ball = ball;
	}

	public void update(float delta) {
		currentState.update(this, delta);
	}

	public void resetBall() {
		keyX = 0;
		tiltX = 0;
		keyY = 0;
		tiltY = 0;
	}

	public void pause() {
		currentState.pause(this);
	}

	public void resume() {
		currentState.resume(this);
	}

	private void updateFromDpad() {
		if (keys.get(MyKeys.UP)) {
			decrementY();
		}
		if (keys.get(MyKeys.DOWN)) {
			incrementY();
		}
		if (keys.get(MyKeys.LEFT)) {
			decrementX();
		}
		if (keys.get(MyKeys.RIGHT)) {
			incrementX();
		}
	}

	private void updateFromKeys() {
		if (Gdx.input.isKeyPressed(Keys.UP)) {
            decrementY();
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            incrementY();
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            decrementX();
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            incrementX();
        }
	}

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		Gdx.app.log("touchDown", event.getListenerActor().getName());
		String name = event.getListenerActor().getName();
		if (name.equals("up")) {
			upPressed();
		}
		else if (name.equals("down")) {
			downPressed();
		}
		else if (name.equals("left")) {
			leftPressed();
		}
		else if (name.equals("right")) {
			rightPressed();
		}
		return true;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer,
			int button) {
		Gdx.app.log("touchUp", event.getListenerActor().getName());
		String name = event.getListenerActor().getName();
		if (name.equals("up")) {
			upReleased();
		}
		else if (name.equals("down")) {
			downReleased();
		}
		else if (name.equals("left")) {
			leftReleased();
		}
		else if (name.equals("right")) {
			rightReleased();
		}
	}

	public float getX() {
		return tiltX;
	}

	public float getY() {
		return tiltY;
	}

	private void incrementX() {
		if (keyX >= KEY_MAX) {
			keyX = KEY_MAX;
		}
		else {
			keyX += KEY_INCREMENT;
		}
	}

	private void decrementX() {
		if (keyX <= -KEY_MAX) {
			keyX = -KEY_MAX;
		}
		else {
			keyX -= KEY_INCREMENT;
		}
	}

	private void incrementY() {
		if (keyY >= KEY_MAX) {
			keyY = KEY_MAX;
		}
		else {
			keyY += KEY_INCREMENT;
		}
	}

	private void decrementY() {
		if (keyY <= -KEY_MAX) {
			keyY = -KEY_MAX;
		}
		else {
			keyY -= KEY_INCREMENT;
		}
	}

	// ** Key presses and touches **************** //

	private void leftPressed() {
		keys.get(keys.put(MyKeys.LEFT, true));
	}

	private void rightPressed() {
		keys.get(keys.put(MyKeys.RIGHT, true));
	}

	private void upPressed() {
		keys.get(keys.put(MyKeys.UP, true));
	}

	private void downPressed(){
		keys.get(keys.put(MyKeys.DOWN, true));
	}

	private void leftReleased() {
		keys.get(keys.put(MyKeys.LEFT, false));
	}

	private void rightReleased() {
		keys.get(keys.put(MyKeys.RIGHT, false));
	}

	private void upReleased() {
		keys.get(keys.put(MyKeys.UP, false));
	}

	private void downReleased() {
		keys.get(keys.put(MyKeys.DOWN, false));
	}

	private void changeState(State state) {
		currentState = state;
	}

	private static enum State {
		PAUSED {
			@Override
			public void resume(BallController b) {
				b.changeState(ACTIVE);
			}
		},
		ACTIVE {
			@Override
			public void pause(BallController b) {
				b.changeState(PAUSED);
			}
			@Override
			public void update(BallController b, float delta) {
				float forceX = 0;
				float forceY = 0;
				if (b.useAccelerometer) {
					// accelerometer is reversed from screen coordinates, we are in landscape mode
					b.tiltX = Gdx.input.getAccelerometerY();
					b.tiltY = Gdx.input.getAccelerometerX();

					forceX = Math.signum(b.tiltX) * (float)(Math.pow(b.tiltX, 2) * 0.001);
					forceY = Math.signum(b.tiltY) * (float)(Math.pow(b.tiltY, 2) * 0.001) * -1;
				}
				else {
					// might as well accept either input
					b.updateFromDpad();
					b.updateFromKeys();

					b.tiltX = b.keyX;
					b.tiltY = b.keyY;

					forceX = b.tiltX * 0.001f;
					forceY = b.tiltY * 0.001f * -1;
				}
				if (b.ball != null) {
					b.ball.applyLinearImpulse(forceX, forceY);
				}
			}
		};

		public void pause(BallController b) {}
		public void resume(BallController b) {}
		public void update(BallController b, float delta) {}
	}
}
