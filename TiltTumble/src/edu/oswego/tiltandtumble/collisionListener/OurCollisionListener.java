package edu.oswego.tiltandtumble.collisionListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import edu.oswego.tiltandtumble.worldObjects.Ball;

public class OurCollisionListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		Object a = contact.getFixtureA().getUserData();
		Object b = contact.getFixtureB().getUserData();
		if (a != null && b != null) {
			Gdx.app.log("begin contact", a.getClass().getName() + " > " + b.getClass().getName());
			if (a instanceof BallCollisionListener && b instanceof Ball) {
				((BallCollisionListener) a).handleBeginCollision(contact, (Ball) b);
			} else if (b instanceof BallCollisionListener && a instanceof Ball) {
				((BallCollisionListener) b).handleBeginCollision(contact, (Ball) a);
			}
		}
	}

	@Override
	public void endContact(Contact contact) {
		Object a = contact.getFixtureA().getUserData();
		Object b = contact.getFixtureB().getUserData();
		if (a != null && b != null) {
			Gdx.app.log("end contact", a.getClass().getName() + " > " + b.getClass().getName());
			if (a instanceof BallCollisionListener && b instanceof Ball) {
				((BallCollisionListener) a).handleEndCollision(contact, (Ball) b);
			} else if (b instanceof BallCollisionListener && a instanceof Ball) {
				((BallCollisionListener) b).handleEndCollision(contact, (Ball) a);
			}
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// not using this at this point
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// not using this at this point
	}
}
