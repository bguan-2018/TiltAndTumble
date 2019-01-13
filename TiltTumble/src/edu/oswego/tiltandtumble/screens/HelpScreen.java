package edu.oswego.tiltandtumble.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import edu.oswego.tiltandtumble.TiltAndTumble;

public class HelpScreen extends AbstractScreen {
	private Sound button;

	public HelpScreen(final TiltAndTumble game) {
		super(game);
	}

	@Override
	public void show() {
		InputMultiplexer multiplexer = new InputMultiplexer(stage,
			new InputAdapter() {
				@Override
				public boolean keyDown(int keycode) {
					if (keycode == Keys.BACK) {
						game.showPreviousScreen();
						return true;
					}
					return super.keyDown(keycode);
				}
			});
		Gdx.input.setInputProcessor(multiplexer);

		AssetManager assetManager = game.getAssetManager();
		String musicFile = "data/soundfx/button-8.ogg";
		button = assetManager.get(musicFile, Sound.class);

		Window table = new Window("\nHelp", skin);
		table.setFillParent(true);
		table.setModal(true);
		table.setMovable(false);
		stage.addActor(table);

		Table table2 = new Table(skin);
		ScrollPane scroll = new ScrollPane(table2, skin);

		table.add(scroll).expandY().padTop(40).padBottom(10);

		table2.row();
		table2.add("Play:", "header");
		table2.row();
		table2.add("The game will begin will a 3-second count down.");
		table2.row();
		table2.add("Tilt the ball to move. There are many obstacles,");
		table2.row();
		table2.add("your main goal is to get to the red finish line.");
		table2.row();
		table2.add("Setting: ", "header").expandX();
		table2.row();
		table2.add("Dpad, debug mode, music and sound effect can be");
		table2.row();
		table2.add("enabled or disabled inside settings.");
		table2.row();
		table2.add("HighScore: ", "header").expandX();
		table2.row();
		table2.add("You can check the top ten high scores you achieved");
		table2.row();
		table2.add("in the game.");
		table2.row();
		table2.add("Obstacles:", "header").expandX();
		table2.row();

		table2.add("Moving Wall:");
		table2.row();
		table2.add("Wall that moves in a certain direction.");
		table2.row();
		table2.add("Spike:");
		table2.row();
		table2.add("Spikes are extemely deadly, instant kill.");
		table2.row();
		table2.add("Hole:");
		table2.row();
		table2.add("Don't get near them,");
		table2.row();
		table2.add(" usually paired with atrractor forces.");
		table2.row();
		table2.add("Attractor Force:");
		table2.row();
		table2.add("Attracts the ball from a far distance.");
		table2.row();
		table2.add("Teleporter:");
		table2.row();
		table2.add("Teleports the ball to a different location.");

		table.row().expand().padBottom(10);

		Button back = new TextButton("Go Back", skin);
		table.add(back).bottom();
		back.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				button.play();
				game.showPreviousScreen();
			}
		});
	}
}
