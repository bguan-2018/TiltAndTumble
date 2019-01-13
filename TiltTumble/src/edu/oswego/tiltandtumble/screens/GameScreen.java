package edu.oswego.tiltandtumble.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;

import edu.oswego.tiltandtumble.TiltAndTumble;
import edu.oswego.tiltandtumble.data.Score;
import edu.oswego.tiltandtumble.levels.AudioManager;
import edu.oswego.tiltandtumble.levels.BallController;
import edu.oswego.tiltandtumble.levels.DebugLevelRenderer;
import edu.oswego.tiltandtumble.levels.DefaultLevelRenderer;
import edu.oswego.tiltandtumble.levels.Level;
import edu.oswego.tiltandtumble.levels.LevelRenderer;
import edu.oswego.tiltandtumble.levels.WorldPopulator;
import edu.oswego.tiltandtumble.screens.dialogs.PauseDialog;
import edu.oswego.tiltandtumble.screens.dialogs.ScoreDialog;
import edu.oswego.tiltandtumble.screens.widgets.DPad;
import edu.oswego.tiltandtumble.screens.widgets.Hud;
import edu.oswego.tiltandtumble.screens.widgets.Starter;

public class GameScreen extends AbstractScreen {
	public static enum Mode {
		ARCADE, PRACTICE
	}

	private final BallController ballController;
	private final WorldPopulator worldPopulator;

	private final Mode currentMode;
	private Level level;
	private LevelRenderer renderer;
	private AudioManager audio;
	InputMultiplexer inputMux = new InputMultiplexer();

	boolean usingDpad = false;
	private final List<Score> scores = new ArrayList<Score>();
	private State currentState;

	private final Hud hud;

	private Dialog pauseDialog;

	public GameScreen(TiltAndTumble game, int currentLevel, Mode mode) {
		super(game);
		ballController = new BallController(!game.getSettings().isUseDpad());
		worldPopulator = new WorldPopulator(game.getAssetManager());

		hud = new Hud(this, skin, game.getAssetManager());
		loadLevel(currentLevel);
		hud.setScore(level.getScore());
		currentMode = mode;
	}

	public void loadLevel(int num) {
		changeState(State.WAITING);
		Gdx.app.log("GameScreen", "Loading level #" + num);
		if (level != null) {
			level.dispose();
			level = null;
		}
		if (renderer != null) {
			renderer.dispose();
			renderer = null;
		}
		if (audio != null) {
			game.getSettings().removeObserver(audio);
			audio.dispose();
			audio = null;
		}
		Gdx.app.log("GameScreen", "Cleaned up previous level");
		level = new Level(num,
				game.getLevels().get(num),
				ballController, worldPopulator, game.getAssetManager());
		Gdx.app.log("GameScreen", "Level loaded");
		renderer = new DefaultLevelRenderer(level,
				game.getWidth(), game.getHeight(),
				game.getSpriteBatch(),
				game.getAssetManager());
		if (game.getSettings().isDebugRender()) {
			renderer = new DebugLevelRenderer(renderer, ballController);
		}
		Gdx.app.log("GameScreen", "Renderer created");
		audio = new AudioManager(
				level,
				game.getSettings().isMusicOn(),
				game.getSettings().isSoundEffectOn(),
				game.getAssetManager());
		game.getSettings().addObserver(audio);
		Gdx.app.log("GameScreen", "Audio manager created");
		hud.setLevel(num + 1);
		new Starter(this, skin, game).show(stage);
		Gdx.app.log("GameScreen", "Level starting...");
	}

	public boolean hasMoreLevels() {
		if (currentMode == Mode.ARCADE){
			return level.getLevelNumber() < game.getLevels().size();
		}
		return false;
	}

	public void loadNextLevel() {
		if (hasMoreLevels() && !level.isFailed()) {
			loadLevel(level.getLevelNumber() + 1);
		}
		else {
			game.showPreviousScreen();
		}
	}

	public Level getCurrentLevel() {
		return level;
	}

	public List<Score> getScores() {
		return scores;
	}

	public Mode getMode() {
		return currentMode;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMux);
		inputMux.addProcessor(stage);
		inputMux.addProcessor(new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				if(keycode == Keys.BACK){
					pause();
					return true;
				}
				return super.keyDown(keycode);
			}
		});

		if (game.getSettings().isUseDpad()){
			DPad dpad = new DPad(skin, ballController);
			dpad.setPosition(0, 0);
			stage.addActor(dpad);
		}
		hud.setPosition(0, stage.getHeight());
		hud.setHeight(32);
		hud.setWidth(stage.getWidth());
		stage.addActor(hud);

		currentState.show(this);
	}

	@Override
	protected void preStageRenderHook(float delta) {
		renderer.render(delta, game.getSpriteBatch(), game.getFont());
		currentState.render(this, delta);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (level != null) {
			level.dispose();
		}
		if (renderer != null) {
			renderer.dispose();
		}
		if (audio != null) {
			audio.dispose();
		}
	}

	public void togglePause() {
		currentState.togglePause(this);
	}

	@Override
	public void pause() {
		currentState.pause(this);
	}

	@Override
	public void resume() {
		currentState.resume(this);
	}

	public void start() {
		currentState.start(this);
	}

	private void changeState(State state) {
		currentState = state;
	}

	private static enum State {
		WAITING {
			@Override
			public void show(GameScreen s) {
				new Starter(s, s.skin, s.game).show(s.stage);
			}

			@Override
			public void start(GameScreen s) {
				//s.game.endMusic();
				s.ballController.resetBall();
				s.ballController.resume();
				s.level.start();
				s.audio.start();
				s.changeState(State.PLAYING);
			}
		},
		PAUSED {
			@Override
			public void resume(GameScreen s) {
				// isVisible seems to always return true, not sure why...
				// make sure the dialog goes away, this can be called from the system
				// level rather than direct user interaction so we want to make sure
				// the game does not start playing before the window goes away.
				if (s.pauseDialog != null && s.pauseDialog.hasParent()) {
					s.pauseDialog.hide();
					s.pauseDialog = null;
				}
				s.ballController.resume();
				s.audio.start();
				s.level.resume();
				s.changeState(State.PLAYING);
			}

			@Override
			public void show(GameScreen s) {
				s.pauseDialog = new PauseDialog("Paused", s.skin, s, s.game).show(s.stage);
			}

			@Override
			public void togglePause(GameScreen s) {
				resume(s);
			}
		},
		PLAYING {
			@Override
			public void render(GameScreen s, float delta) {
				if (s.level.hasFinished()) {
					s.audio.pause();
					s.scores.add(s.level.getScore());
					new ScoreDialog("Score", s.skin, s.game, s).show(s.stage);
					s.changeState(State.SCORED);
				}
				else {
					s.level.update(delta);
				}

				s.hud.setScore(s.level.getScore());
			}

			@Override
			public void pause(GameScreen s) {
				s.pauseDialog = new PauseDialog("Paused", s.skin, s, s.game).show(s.stage);
				s.ballController.pause();
				s.audio.pause();
				s.level.pause();
				s.changeState(State.PAUSED);
			}

			@Override
			public void togglePause(GameScreen s) {
				pause(s);
			}
		},
		SCORED;

		public void start(GameScreen s) {}
		public void togglePause(GameScreen s) {}
		public void pause(GameScreen s) {}
		public void resume(GameScreen s) {}
		public void show(GameScreen s) {}
		public void render(GameScreen s, float delta) {}
	}
}
