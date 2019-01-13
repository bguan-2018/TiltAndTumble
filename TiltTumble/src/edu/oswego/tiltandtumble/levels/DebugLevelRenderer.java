package edu.oswego.tiltandtumble.levels;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.TimeUtils;

public class DebugLevelRenderer implements LevelRenderer {
	private final BallController ballController;

	private int fps = 0;
	private long startTime;

	private final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
	private final DecimalFormat decimalFormatter = new DecimalFormat("######.####");

	private final LevelRenderer decorated;

	public DebugLevelRenderer(LevelRenderer decorated, BallController ballController) {
		this.decorated = decorated;
		this.ballController = ballController;

		debugRenderer.setDrawVelocities(true);
		debugRenderer.setDrawContacts(true);
		debugRenderer.setDrawJoints(true);
	}

	@Override
	public void render(float delta, SpriteBatch batch, BitmapFont font) {
		decorated.render(delta, batch, font);

		OrthographicCamera camera = getCamera();
		Level level = getLevel();

		debugRenderer.render(level.getWorld(),
				camera.combined.scl(1f / level.getScale().getScale()));

		batch.begin();
		renderTextInCameraView(batch, font, "FPS: " + fps, 10, 15);
		renderTextInCameraView(batch, font,
				"TiltX: " + decimalFormatter.format(ballController.getX()),
				10, 30);
		renderTextInCameraView(batch, font,
				"TiltY: " + decimalFormatter.format(ballController.getY()),
				10, 45);
		renderTextInCameraView(
				batch,
				font,
				"Vel X: " + decimalFormatter.format(level.getBall().getBody().getLinearVelocity().x),
				10,
				60);
		renderTextInCameraView(
				batch,
				font,
				"Vel Y: " + decimalFormatter.format(level.getBall().getBody().getLinearVelocity().y),
				10,
				75);
		renderTextInCameraView(
				batch,
				font,
				"Vel A: " + decimalFormatter.format(level.getBall().getBody().getAngularVelocity()),
				10,
				90);
		batch.end();

		// update every second
		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			fps = Gdx.graphics.getFramesPerSecond();
			startTime = TimeUtils.nanoTime();
		}
	}

	private void renderTextInCameraView(SpriteBatch batch, BitmapFont font, String text, float x, float y) {
		OrthographicCamera camera = getCamera();
		font.draw(batch,
				text,
				camera.position.x - (camera.viewportWidth / 2f) + x,
				camera.position.y - (camera.viewportHeight / 2f) + y);
	}

	@Override
	public OrthographicCamera getCamera() {
		return decorated.getCamera();
	}

	@Override
	public Level getLevel() {
		return decorated.getLevel();
	}

	@Override
	public void updateCamera() {
		decorated.updateCamera();
	}

	@Override
	public void dispose() {
		decorated.dispose();
		debugRenderer.dispose();
	}
}
