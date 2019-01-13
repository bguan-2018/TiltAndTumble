package edu.oswego.tiltandtumble.collisionListener;

import com.badlogic.gdx.physics.box2d.Contact;

import edu.oswego.tiltandtumble.worldObjects.Ball;

public interface BallCollisionListener {
	public void handleBeginCollision(Contact contact, Ball ball);
	public void handleEndCollision(Contact contact, Ball ball);
}
