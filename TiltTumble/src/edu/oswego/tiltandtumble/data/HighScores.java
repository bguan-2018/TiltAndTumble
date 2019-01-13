package edu.oswego.tiltandtumble.data;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class HighScores implements Serializable{
	private static final long serialVersionUID = -2777200203575485944L;
	private static final String FILE = "Scores.dat";

	private final SortedSet<HighScore> scores;

	public HighScores() {
		scores = new TreeSet<HighScore>();
	}

	public boolean isHighScore(Score score) {
		if (scores.size() >= 10) {
			return score.compareTo(scores.first()) > 0;
		}
		return true;
	}

	public void add(HighScore score) {
		Gdx.app.log("HighScores", "Adding score: " + score.getPoints());
		scores.add(score);
		if (scores.size() >= 10) {
			HighScore bumped = scores.first();
			Gdx.app.log("HighScores", "Bumping score: " + bumped.getPoints());
			scores.remove(bumped);
		}
	}

	public static void save(HighScores scores) {
		FileHandle file = Gdx.files.local(FILE);
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(file.write(false));
			out.writeObject(scores);
		} catch (IOException e) {
			Gdx.app.log("HighScores", e.getMessage(), e);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Gdx.app.log("HighScores", e.getMessage(), e);
				}
			}
		}
	}

	public static HighScores load() {
		FileHandle file = Gdx.files.local("Scores.dat");
		HighScores scores = null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(file.read());
			scores = (HighScores)in.readObject();
		} catch (Exception e) {
			Gdx.app.log("HighScores", e.getMessage(), e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Gdx.app.log("HighScores", e.getMessage(), e);
				}
			}
		}
		if (scores == null) {
			scores = new HighScores();
		}
		else {
			// NOTE: reload high scores because we had a bug that caused them to save incorrectly
			HighScores tmp = new HighScores();
			for (HighScore s : scores.scores) {
				tmp.scores.add(s);
			}
			scores = tmp;
		}
		return scores;
	}

	public Collection<HighScore> getScores(){
		return scores;
	}
}
