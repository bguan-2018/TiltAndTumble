package edu.oswego.tiltandtumble.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;

import edu.oswego.tiltandtumble.TiltAndTumble;

abstract class AbstractScreen implements Screen {

	protected final TiltAndTumble game;
	protected final Stage stage;
	protected final Skin skin;

	public AbstractScreen(final TiltAndTumble game) {
		this.game = game;
		this.stage = game.getStage();
		this.skin = game.getSkin();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		preStageRenderHook(delta);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		postStageRenderHook(delta);
	}

	protected void preStageRenderHook(float delta) {

	}

	protected void postStageRenderHook(float delta) {

	}

	@Override
	public void resize(int width, int height) {
		// NOTE: see https://github.com/libgdx/libgdx/wiki/Scene2d
		//       what this does is letter box the screen if it does not fit the
		//       current aspect ratio. seems to work pretty well.
		Vector2 size = Scaling.fit.apply(game.getWidth(), game.getHeight(), width, height);
	    int viewportX = (int)(width - size.x) / 2;
	    int viewportY = (int)(height - size.y) / 2;
	    int viewportWidth = (int)size.x;
	    int viewportHeight = (int)size.y;
	    Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
	    stage.setViewport(game.getWidth(), game.getHeight(), true, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	@Override
	public void hide() {
		stage.clear();
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
		// children can override if they need to
	}

	@Override
	public void resume() {
		// children can override if they need to
	}

	@Override
	public void dispose() {
		Gdx.input.setInputProcessor(null);
	}
}
