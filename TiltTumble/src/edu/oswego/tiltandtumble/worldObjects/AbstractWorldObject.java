package edu.oswego.tiltandtumble.worldObjects;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

abstract class AbstractWorldObject implements WorldObject {
	protected final Body body;

	AbstractWorldObject(Body body) {
		this.body = body;
		body.setUserData(this);
		for (Fixture f : body.getFixtureList()) {
			f.setUserData(this);
		}
	}

	@Override
	public Body getBody() {
		return body;
	}
}
