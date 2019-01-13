package edu.oswego.tiltandtumble.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import edu.oswego.tiltandtumble.worldObjects.Activatable;
import edu.oswego.tiltandtumble.worldObjects.AttractorForce;
import edu.oswego.tiltandtumble.worldObjects.Ball;
import edu.oswego.tiltandtumble.worldObjects.FinishLine;
import edu.oswego.tiltandtumble.worldObjects.Hole;
import edu.oswego.tiltandtumble.worldObjects.MomentarySwitch;
import edu.oswego.tiltandtumble.worldObjects.MovingWall;
import edu.oswego.tiltandtumble.worldObjects.PushBumper;
import edu.oswego.tiltandtumble.worldObjects.Spike;
import edu.oswego.tiltandtumble.worldObjects.StaticWall;
import edu.oswego.tiltandtumble.worldObjects.Switch;
import edu.oswego.tiltandtumble.worldObjects.Teleporter;
import edu.oswego.tiltandtumble.worldObjects.TeleporterRandomSelector;
import edu.oswego.tiltandtumble.worldObjects.TeleporterRoundRobinSelector;
import edu.oswego.tiltandtumble.worldObjects.TeleporterSelectorStrategy;
import edu.oswego.tiltandtumble.worldObjects.TeleporterTarget;
import edu.oswego.tiltandtumble.worldObjects.TimedSwitch;
import edu.oswego.tiltandtumble.worldObjects.ToggleSwitch;
import edu.oswego.tiltandtumble.worldObjects.graphics.AnimationGraphic;
import edu.oswego.tiltandtumble.worldObjects.graphics.GraphicComponent;
import edu.oswego.tiltandtumble.worldObjects.graphics.NullGraphic;
import edu.oswego.tiltandtumble.worldObjects.graphics.ParticleEffectGraphic;
import edu.oswego.tiltandtumble.worldObjects.graphics.SpriteGraphic;
import edu.oswego.tiltandtumble.worldObjects.paths.ConstantMovement;
import edu.oswego.tiltandtumble.worldObjects.paths.MovementStrategy;
import edu.oswego.tiltandtumble.worldObjects.paths.NodeStopMovement;
import edu.oswego.tiltandtumble.worldObjects.paths.PathPoint;
import edu.oswego.tiltandtumble.worldObjects.paths.PathPointTraverser;

public final class WorldPopulator implements Disposable {
	private final BodyDefBuilder bodyDef = new BodyDefBuilder();
	private final FixtureDefBuilder fixtureDef = new FixtureDefBuilder();
	private final String atlasFile = "data/WorldObjects/worldobjects.pack";
	private final TextureAtlas atlas;
	private final AssetManager assetManager;

	public WorldPopulator(AssetManager assetManager) {
		this.assetManager = assetManager;
		assetManager.load(atlasFile, TextureAtlas.class);
		assetManager.finishLoading();
		atlas = assetManager.get(atlasFile, TextureAtlas.class);
	}

	@Override
	public void dispose() {
		assetManager.unload("data/WorldObjects/worldobjects.pack");
	}

	public Ball populateWorldFromMap(Level level, TiledMap map, World world,
			UnitScale scale) {
		Ball ball = null;
		MapLayer layer = map.getLayers().get("collision");
		Map<String, PathPoint> paths = getPaths(map, world, scale);
		TeleportationMeshHelper meshHelper = new TeleportationMeshHelper();
		SwitchConnectionHelper switchHelper = new SwitchConnectionHelper();
		for (MapObject obj : layer.getObjects()) {
			if (obj.getName() != null) {
				if (obj.getName().equals("StaticWall")) {
					level.addWorldObject(createStaticWall(obj, level, world, scale));
				} else if (obj.getName().equals("MovingWall")) {
					level.addWorldObject(createMovingWall(obj, level, world, scale, paths, switchHelper));
				} else if (obj.getName().equals("PushBumper")) {
					level.addWorldObject(createPushBumper(obj, world, scale));
				} else if (obj.getName().equals("FinishLine")) {
					level.addWorldObject(createFinishLine(obj, level, world, scale));
				} else if (obj.getName().equals("Spike")) {
					level.addWorldObject(createSpike(obj, level, world, scale));
				} else if (obj.getName().equals("Hole")) {
					level.addWorldObject(createHole(obj, level, world, scale));
				} else if (obj.getName().equals("Teleporter")) {
					level.addWorldObject(createTeleporter(obj, level, world, scale, meshHelper));
				} else if (obj.getName().equals("TeleporterTarget")) {
					level.addWorldObject(createTeleporterTarget(obj, level, world, scale, meshHelper));
				} else if (obj.getName().equals("Ball")) {
					ball = createBall(obj, world, scale);
					level.addWorldObject(ball);
				} else if (obj.getName().equals("AttractorForce")) {
					level.addWorldObject(createAttractorForce(obj, level, world, scale));
				} else if (obj.getName().equals("ToggleSwitch")) {
					level.addWorldObject(createToggleSwitch(obj, world, scale, switchHelper));
				} else if (obj.getName().equals("TimedSwitch")) {
					level.addWorldObject(createTimedSwitch(obj, world, scale, switchHelper));
				} else if (obj.getName().equals("MomentarySwitch")) {
					level.addWorldObject(createMomentarySwitch(obj, world, scale, switchHelper));

				}
			}
		}
		meshHelper.buildMesh();
		switchHelper.wireSwitches();
		return ball;
	}

	public TeleporterTarget createTeleporterTarget(MapObject obj, Level level,
			World world, UnitScale scale, TeleportationMeshHelper meshHelper) {
		Body body = world.createBody(bodyDef.reset().type(TeleporterTarget.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(TeleporterTarget.IS_SENSOR).build());
		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		GraphicComponent effect;
		String name = props.get("effect", String.class);
		if (name == null) {
			name = "teleporter.p";
		}
		if (name.equals("none")) {
			effect = new NullGraphic();
		} else {
			String filename = "data/WorldObjects/" + name;
			if (!assetManager.isLoaded(filename)) {
				ParticleEffectLoader.ParticleEffectParameter param = new ParticleEffectLoader.ParticleEffectParameter();
				param.atlasFile = atlasFile;
				assetManager.load(filename, ParticleEffect.class, param);
				assetManager.finishLoading();
			}
			// NOTE: particle effects need to be copied if you have more than one.
			ParticleEffect particle = new ParticleEffect(assetManager.get(filename, ParticleEffect.class));
			effect = new ParticleEffectGraphic(particle);
			effect.setPosition(
					scale.metersToPixels(body.getPosition().x),
					scale.metersToPixels(body.getPosition().y));
		}

		TeleporterTarget target = new TeleporterTarget(
				body,
				Boolean.valueOf(props.get("reset velocity", "true", String.class)),
				level.getBallController(),
				effect,
				assetManager);
		meshHelper.add(
				props.get("id", String.class),
				target);
		return target;
	}

	public Teleporter createTeleporter(MapObject obj, Level level, World world,
			UnitScale scale, TeleportationMeshHelper meshHelper) {
		Body body = world.createBody(bodyDef.reset().type(Teleporter.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(Teleporter.IS_SENSOR).build());

		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		String selectorName = props.get("selector", "Random", String.class);
		TeleporterSelectorStrategy selector;
		if (selectorName.equals("Random")) {
			selector = new TeleporterRandomSelector();
		} else {
			selector = new TeleporterRoundRobinSelector();
		}

		GraphicComponent effect;
		String name = props.get("effect", String.class);
		if (name == null) {
			name = "teleporter.p";
		}
		if (name.equals("none")) {
			effect = new NullGraphic();
		} else {
			String filename = "data/WorldObjects/" + name;
			if (!assetManager.isLoaded(filename)) {
				ParticleEffectLoader.ParticleEffectParameter param = new ParticleEffectLoader.ParticleEffectParameter();
				param.atlasFile = atlasFile;
				assetManager.load(filename, ParticleEffect.class, param);
				assetManager.finishLoading();
			}
			// NOTE: particle effects need to be copied if you have more than one.
			ParticleEffect particle = new ParticleEffect(assetManager.get(filename, ParticleEffect.class));
			effect = new ParticleEffectGraphic(particle);
			effect.setPosition(
					scale.metersToPixels(body.getPosition().x),
					scale.metersToPixels(body.getPosition().y));
		}

		GraphicComponent animation;
		name = props.get("animation", String.class);
		if (name == null) {
			name = "teleporter-glow";
		}
		if (name.equals("none")) {
			animation = new NullGraphic();
		} else {
			TextureRegion sheet = atlas.findRegion(name);
			animation = new AnimationGraphic.Builder(sheet,
					props.get("animation rows", 1, Integer.class),
					props.get("animation columns", 8, Integer.class),
					props.get("animation duration", 1, Integer.class))
					.position(scale.metersToPixels(body.getPosition().x),
							scale.metersToPixels(body.getPosition().y))
					.center()
					.looping(Animation.LOOP_PINGPONG)
					.build();
		}

		Teleporter teleporter = new Teleporter(
				body,
				selector,
				Boolean.valueOf(props.get("reset velocity", "true", String.class)),
				level.getBallController(),
				effect,
				getFloatProperty(obj, "wait time", Teleporter.WAIT_TIME),
				animation,
				assetManager);
		meshHelper.add(
				props.get("id", String.class),
				teleporter, selector,
				props.get("target", "", String.class).split(","));
		return teleporter;
	}

	public Map<String, PathPoint> getPaths(TiledMap map, World world, UnitScale scale) {
		MapLayer layer = map.getLayers().get("paths");
		Map<String, PathPoint> paths = new HashMap<String, PathPoint>();

		if (layer != null) {
			for (MapObject obj : layer.getObjects()) {
				if (obj.getName() != null) {
					float[] vertices;
					boolean loop = Boolean.valueOf(obj.getProperties().get("loop", "false", String.class));
					if (obj instanceof PolylineMapObject) {
						vertices = ((PolylineMapObject)obj).getPolyline().getTransformedVertices();
					}
					else if (obj instanceof PolygonMapObject) {
						vertices = ((PolygonMapObject)obj).getPolygon().getTransformedVertices();
						loop = true;
					}
					else if (obj instanceof RectangleMapObject) {
						vertices = new float[8];
						Rectangle r = ((RectangleMapObject)obj).getRectangle();
						vertices[0] = r.x;
						vertices[1] = r.y;
						vertices[2] = r.x + r.width;
						vertices[3] = r.y;
						vertices[4] = r.x + r.width;
						vertices[5] = r.y + r.height;
						vertices[6] = r.x;
						vertices[7] = r.y + r.height;
						loop = true;
					}
					else {
						// we don't support circles for this at this point in time...
						continue;
					}
					PathPoint head = null;
					PathPoint last = null;
					for (int i = 0; i < vertices.length; i += 2) {
						PathPoint next = new PathPoint(
							scale.pixelsToMeters(vertices[i]),
							scale.pixelsToMeters(vertices[i + 1])
						);
						if (head == null) {
							head = next;
						}
						if (last != null) {
							last.setNext(next);
							next.setPrevious(last);
						}
						last = next;
					}
					if (loop) {
						last.setNext(head);
						head.setPrevious(last);
					}
					paths.put(obj.getName(), head);
				}
			}
		}
		return paths;
	}

	private PathResult findClosestPointOnPathToPoint(PathPoint path, Vector2 objPoint) {
		Vector2 bestPoint = new Vector2(path.x, path.y);
		float bestDistance = Float.MAX_VALUE;
		PathPoint bestPath = path;

		PathPoint current = path;
		PathPoint next = current.getNext();
		Vector2 start = new Vector2();
		Vector2 end = new Vector2();
		int idx = 0;
		do {
			start.x = current.x;
			start.y = current.y;
			end.x = next.x;
			end.y = next.y;
			Gdx.app.log("PointOnPath", "Checking leg " + (++idx) + " " + start + " " + end);

			Vector2 point = findClosestPointOnLineToPoint(start, end, objPoint);
			if (!point.equals(Vector2.Zero)) {
				float distance = objPoint.dst(point);
				Gdx.app.log("PointOnPath", "Distance: " + distance);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestPoint.set(point);
					bestPath = current;
					Gdx.app.log("PointOnPath", "New Best Distance!");
					Gdx.app.log("PointOnPath", "New Best Point: " + bestPoint + " for " + objPoint);
				}
			}
			else {
				Gdx.app.log("PointOnPath", "Invalid leg found");
			}
			current = next;
			next = current.getNext();
		} while (next != null && !next.equals(path));
		Gdx.app.log("PointOnPath", "Best Point: " + bestPoint + " for " + objPoint);
		return new PathResult(bestPath, bestPoint);
	}

	private Vector2 findClosestPointOnLineToPoint(Vector2 start, Vector2 end,
			Vector2 point) {
		// http://nic-gamedev.blogspot.com/2011/11/using-vector-mathematics-and-bit-of_08.html
		Vector2 diff = new Vector2(end);
		diff.sub(start);
		float len2 = diff.len2();

		Vector2 toPoint = new Vector2(point);
		toPoint.sub(start);
		float dot = diff.dot(toPoint);

		float percent = dot / len2;
		if (percent < 0.0f || percent > 1.0f) {
			point = Vector2.Zero;
		}

		return ((new Vector2(end)
			.sub(start))
			.scl(percent))
			.add(start);
	}

	public MovingWall createMovingWall(MapObject obj, Level level, World world,
			UnitScale scale, Map<String, PathPoint> paths,
			SwitchConnectionHelper switchHelper) {
		PathPoint head = paths.get(obj.getProperties().get("path", String.class));
		Body body = world.createBody(bodyDef.reset().type(MovingWall.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		// warp to the point closest to the shapes point on the path, just to make sure
		// we are on the path.
		PathResult result = findClosestPointOnPathToPoint(head,
				getCenter(obj).scl(scale.getScale()));
		body.setTransform(result.point, 0);
		body.createFixture(fixtureDef.reset().shape(shape)
				.friction(getFloatProperty(obj, "friction", MovingWall.FRICTION))
				.density(getFloatProperty(obj, "density", MovingWall.DENSITY))
				.restitution(getFloatProperty(obj, "restitution", MovingWall.RESTITUTION))
				.build());
		//Vector2 dimensions = getDimensions(obj);
		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		float speed = getFloatProperty(obj, "speed", MovingWall.DEFAULT_SPEED);

		Sprite sprite = atlas.createSprite(props.get("sprite", MovingWall.DEFAULT_SPRITE, String.class));
		float degrees = getFloatProperty(obj, "rotate", 0f);
		sprite.setRotation(degrees);
		// TODO: i would like to scale the graphic to the shape but i can't
		//       get it to work correctly. the size seems to get set based on
		//       the unrotated image.
		//dimensions.rotate(degrees);
		//sprite.setSize(dimensions.x, dimensions.y);
		GraphicComponent graphic = new SpriteGraphic(sprite);

		MovementStrategy movement;
		if (props.containsKey("movement")
				&& !props.get("movement", String.class).equals("Constant")) {
			if (props.get("movement", String.class).equals("NodeStop")) {
				movement = new NodeStopMovement(
						new PathPointTraverser(result.head, speed >= 0),
						Math.abs(speed));
			} else {
				throw new IllegalArgumentException("Invalid movement type: "
						+ props.get("movement", String.class));
			}
		} else {
			movement = new ConstantMovement(
					new PathPointTraverser(result.head, speed >= 0),
					Math.abs(speed));
		}

		TextureRegion sheet = atlas.findRegion("popped");
		GraphicComponent deathGraphic = new AnimationGraphic.Builder(sheet, 1, 8, 1)
				.origin(12, 12)
				.scale(1.5f, 1.5f)
				.build();

		MovingWall wall = new MovingWall(body,
				movement,
				graphic,
				deathGraphic,
				scale,
				level,
				assetManager);
		if (props.containsKey("switch")) {
			switchHelper.add(wall, props.get("switch", String.class));
		}
		return wall;
	}

	public StaticWall createStaticWall(MapObject obj, Level level, World world,
			UnitScale scale) {
		Body body = world.createBody(bodyDef.reset().type(StaticWall.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.friction(getFloatProperty(obj, "friction", StaticWall.FRICTION))
				.density(getFloatProperty(obj, "density", StaticWall.DENSITY))
				.restitution(getFloatProperty(obj, "restitution", StaticWall.RESTITUTION))
				.build());
		// dispose after creating fixture
		shape.dispose();

		TextureRegion sheet = atlas.findRegion("popped");
		GraphicComponent deathGraphic = new AnimationGraphic.Builder(sheet, 1, 8, 1)
				.origin(12, 12)
				.scale(1.5f, 1.5f)
				.build();

		return new StaticWall(body, level, deathGraphic, assetManager);
	}

	public PushBumper createPushBumper(MapObject obj, World world,
			UnitScale scale) {
		Body body = world.createBody(bodyDef.reset().type(PushBumper.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.friction(getFloatProperty(obj, "friction", PushBumper.FRICTION))
				.density(getFloatProperty(obj, "density", PushBumper.DENSITY))
				.restitution(getFloatProperty(obj, "restitution", PushBumper.RESTITUTION))
				.build());
		// dispose after creating fixture
		shape.dispose();

		return new PushBumper(body,
				getFloatProperty(obj, "speed", PushBumper.DEFAULT_SPEED),
				assetManager);
	}

	public Ball createBall(MapObject obj, World world, UnitScale scale) {
		if (!(obj instanceof EllipseMapObject)) {
			throw new IllegalArgumentException(obj.getName()
					+ " Unsupported MapObject: "
					+ obj.getClass().getName());
		}

		Body body = world.createBody(bodyDef
				.reset()
				.type(Ball.BODY_TYPE)
				.angularDampening(getFloatProperty(obj, "angular dampening", Ball.ANGULAR_DAMPENING))
				.linearDamping(getFloatProperty(obj, "linear dampening", Ball.LINEAR_DAMPENING))
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.friction(getFloatProperty(obj, "friction", Ball.FRICTION))
				.density(getFloatProperty(obj, "density", Ball.DENSITY))
				.restitution(getFloatProperty(obj, "restitution", Ball.RESTITUTION))
				.build());
		float diameter = scale.metersToPixels(shape.getRadius()) * 2;
		// dispose after creating fixture
		shape.dispose();

		Sprite sprite = atlas.createSprite("GreenOrb");
		sprite.setSize(diameter, diameter);
		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
		GraphicComponent graphic = new SpriteGraphic(sprite);

		return new Ball(body, graphic, scale, assetManager);
	}

	public FinishLine createFinishLine(MapObject obj, Level level,
			World world, UnitScale scale) {
		Body body = world.createBody(bodyDef.reset().type(FinishLine.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(FinishLine.IS_SENSOR).build());
		// dispose after creating fixture
		shape.dispose();

		return new FinishLine(body, level, assetManager);
	}

	public Hole createHole(MapObject obj, Level level, World world,
			UnitScale scale) {
		Body body = world.createBody(bodyDef.reset().type(Hole.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(Hole.IS_SENSOR).build());

		// dispose after creating fixture
		shape.dispose();

		TextureRegion sheet = atlas.findRegion("ballfall");
		GraphicComponent graphic = new AnimationGraphic.Builder(sheet, 1, 8, 1)
				.position(scale.metersToPixels(body.getPosition().x),
						scale.metersToPixels(body.getPosition().y))
				.origin(16, 16)
				.build();

		return new Hole(body, level, graphic, assetManager);
	}

	public Spike createSpike(MapObject obj, Level level, World world,
			UnitScale scale) {
		Body body = world.createBody(bodyDef.reset().type(Spike.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape).build());

		// dispose after creating fixture
		shape.dispose();

		TextureRegion sheet = atlas.findRegion("deflate");
		GraphicComponent graphic = new AnimationGraphic.Builder(sheet, 1, 8, 1)
				.origin(12, 12)
				.build();
		return new Spike(body, level, graphic, assetManager);
	}

	public AttractorForce createAttractorForce(MapObject obj, Level level,
			World world, UnitScale scale)
	{
		Body body = world.createBody(bodyDef.reset()
				.type(AttractorForce.BODY_TYPE).build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(AttractorForce.IS_SENSOR).build());

		float radius = shape.getRadius();
		// dispose after creating fixture
		shape.dispose();

		TextureRegion sheet = atlas.findRegion("attractor-ani");
		GraphicComponent graphic = new AnimationGraphic.Builder(sheet, 1, 8, 1)
				.position(scale.metersToPixels(body.getPosition().x),
						scale.metersToPixels(body.getPosition().y))
				// No idea why i need to do -4 on this to get it to line up correctly
				.origin(16 - 4, 0)
				.looping(Animation.LOOP)
				.build();

		return new AttractorForce(body,
				getFloatProperty(obj, "speed", AttractorForce.DEFAULT_SPEED),
				radius,
				graphic,
				scale,
				assetManager);
	}

	public ToggleSwitch createToggleSwitch(MapObject obj, World world,
			UnitScale scale, SwitchConnectionHelper switchHelper) {
		Body body = world.createBody(bodyDef.reset().type(ToggleSwitch.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(ToggleSwitch.IS_SENSOR).build());

		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		GraphicComponent graphicOn;
		if (props.containsKey("sprite-on")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-on", String.class));
			graphicOn = new SpriteGraphic(sprite);
		} else {
			graphicOn = new NullGraphic();
		}
		GraphicComponent graphicOff;
		if (props.containsKey("sprite-off")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-off", String.class));
			graphicOff = new SpriteGraphic(sprite);
		} else {
			graphicOff = new NullGraphic();
		}
		graphicOn.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));
		graphicOff.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));

		ToggleSwitch swtch = new ToggleSwitch(body,
				props.get("startOn", false, Boolean.class),
				graphicOn, graphicOff,
				assetManager);
		switchHelper.add(props.get("id", String.class), swtch);
		return swtch;
	}

	public TimedSwitch createTimedSwitch(MapObject obj, World world,
			UnitScale scale, SwitchConnectionHelper switchHelper) {
		Body body = world.createBody(bodyDef.reset().type(TimedSwitch.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(TimedSwitch.IS_SENSOR).build());

		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		GraphicComponent graphicOn;
		if (props.containsKey("sprite-on")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-on", String.class));
			graphicOn = new SpriteGraphic(sprite);
		} else {
			graphicOn = new NullGraphic();
		}
		GraphicComponent graphicOff;
		if (props.containsKey("sprite-off")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-off", String.class));
			graphicOff = new SpriteGraphic(sprite);
		} else {
			graphicOff = new NullGraphic();
		}
		graphicOn.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));
		graphicOff.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));

		TimedSwitch swtch = new TimedSwitch(body,
				getFloatProperty(obj, "interval", TimedSwitch.DEFAULT_INTERVAL),
				props.get("startOn", false, Boolean.class),
				graphicOn, graphicOff,
				assetManager);
		switchHelper.add(props.get("id", String.class), swtch);
		return swtch;
	}

	public MomentarySwitch createMomentarySwitch(MapObject obj, World world,
			UnitScale scale, SwitchConnectionHelper switchHelper) {
		Body body = world.createBody(bodyDef.reset().type(MomentarySwitch.BODY_TYPE)
				.build());
		Shape shape = createShape(obj, scale, body);
		body.createFixture(fixtureDef.reset().shape(shape)
				.isSensor(MomentarySwitch.IS_SENSOR).build());

		// dispose after creating fixture
		shape.dispose();

		MapProperties props = obj.getProperties();

		GraphicComponent graphicOn;
		if (props.containsKey("sprite-on")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-on", String.class));
			graphicOn = new SpriteGraphic(sprite);
		} else {
			graphicOn = new NullGraphic();
		}
		GraphicComponent graphicOff;
		if (props.containsKey("sprite-off")) {
			Sprite sprite = atlas.createSprite(props.get("sprite-off", String.class));
			graphicOff = new SpriteGraphic(sprite);
		} else {
			graphicOff = new NullGraphic();
		}
		graphicOn.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));
		graphicOff.setPosition(scale.metersToPixels(body.getPosition().x),
				scale.metersToPixels(body.getPosition().y));

		MomentarySwitch swtch = new MomentarySwitch(body,
				props.get("startOn", false, Boolean.class),
				graphicOn, graphicOff,
				assetManager);
		switchHelper.add(props.get("id", String.class), swtch);
		return swtch;
	}

	private Shape createShape(MapObject object, UnitScale scale, Body body) {
		Shape shape;
		if (object instanceof PolygonMapObject) {
			shape = createShape((PolygonMapObject)object, scale, body);
		} else if (object instanceof RectangleMapObject) {
			shape = createShape((RectangleMapObject)object, scale, body);
		} else if (object instanceof EllipseMapObject) {
			shape = createShape((EllipseMapObject)object, scale, body);
		} else if (object instanceof PolylineMapObject) {
			shape = createShape((PolylineMapObject)object, scale, body);
		} else {
			throw new IllegalArgumentException(object.getName()
					+ " Unsupported MapObject: "
					+ object.getClass().getName());
		}
		Gdx.app.log("WorldPopulator", "Creating " + object.getName()
				+ " - " + object.getClass().getSimpleName()
				+ " > "+ shape.getClass().getSimpleName());
		return shape;
	}

	private Shape createShape(PolylineMapObject object, UnitScale scale, Body body) {
		ChainShape shape = new ChainShape();
		Polyline polyline = object.getPolyline();
		float[] vertices = polyline.getVertices();
		Vector2[] worldVertices = new Vector2[vertices.length / 2];
		for (int i = 0; i < worldVertices.length; ++i) {
			worldVertices[i] = new Vector2(
				scale.pixelsToMeters(vertices[i * 2]),
				scale.pixelsToMeters(vertices[(i * 2) + 1])
			);
		}
		if (Boolean.valueOf(object.getProperties().get("loop", "false", String.class))) {
			shape.createLoop(worldVertices);
		}
		else {
			shape.createChain(worldVertices);
		}
		body.setTransform(
				scale.pixelsToMeters(polyline.getX()),
				scale.pixelsToMeters(polyline.getY()),
				0);
		return shape;
	}

	private Shape createShape(PolygonMapObject object, UnitScale scale, Body body) {
		// NOTE: when creating the map objects the polygons must have no
		// more than 8 vertices and must not be concave. this is a
		// limitation of the physics engine. so complex shapes need to be
		// composed of multiple adjacent polygons.
		PolygonShape shape = new PolygonShape();
		Polygon polygon = object.getPolygon();
		float[] vertices = polygon.getVertices();
		float[] worldVertices = new float[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			worldVertices[i] = scale.pixelsToMeters(vertices[i]);
		}
		shape.set(worldVertices);
		body.setTransform(
				scale.pixelsToMeters(polygon.getX()),
				scale.pixelsToMeters(polygon.getY()),
				0);
		return shape;
	}

	private Shape createShape(RectangleMapObject object, UnitScale scale, Body body) {
		PolygonShape shape = new PolygonShape();
		Rectangle rectangle = object.getRectangle();
		shape.setAsBox(scale.pixelsToMeters(rectangle.width * 0.5f),
				scale.pixelsToMeters(rectangle.height * 0.5f));
		Vector2 center = new Vector2();
		rectangle.getCenter(center);
		center.scl(scale.getScale());
		body.setTransform(center, 0);
		return shape;
	}

	private Shape createShape(EllipseMapObject object, UnitScale scale, Body body) {
		Gdx.app.log("warning", "Converting ellipse to a circle");
		// NOTE: there are no ellipse shapes so just convert it to a circle
		CircleShape shape = new CircleShape();

		Vector2 dimensions = getDimensions(object);
		shape.setRadius(scale.pixelsToMeters(dimensions.x * 0.5f));
		Vector2 center = getCenter(object);

		body.setTransform(
				scale.pixelsToMeters(center.x),
				scale.pixelsToMeters(center.y),
				body.getAngle());
		return shape;
	}

	private Vector2 getCenter(MapObject object) {
		Vector2 center = new Vector2();
		if (object instanceof PolygonMapObject) {
			Polygon p = ((PolygonMapObject)object).getPolygon();
			center.x = p.getX();
			center.y = p.getY();
		} else if (object instanceof RectangleMapObject) {
			Rectangle r = ((RectangleMapObject)object).getRectangle();
			r.getCenter(center);
		} else if (object instanceof EllipseMapObject) {
			Ellipse e = ((EllipseMapObject)object).getEllipse();
			center.x = e.x + (e.width * 0.5f);
			center.y = e.y + (e.height * 0.5f);
		} else if (object instanceof PolylineMapObject) {
			Polyline p = ((PolylineMapObject)object).getPolyline();
			center.x = p.getX();
			center.y = p.getY();
		} else {
			throw new IllegalArgumentException(object.getName()
					+ " Unsupported MapObject: "
					+ object.getClass().getName());
		}
		return center;
	}

	private Vector2 getDimensions(MapObject object) {
		Vector2 dimensions = new Vector2();
		if (object instanceof PolygonMapObject) {
			Polygon p = ((PolygonMapObject)object).getPolygon();
			dimensions.x = p.getBoundingRectangle().width;
			dimensions.y = p.getBoundingRectangle().height;
		} else if (object instanceof RectangleMapObject) {
			Rectangle r = ((RectangleMapObject)object).getRectangle();
			dimensions.x = r.width;
			dimensions.y = r.height;
		} else if (object instanceof EllipseMapObject) {
			Ellipse e = ((EllipseMapObject)object).getEllipse();
			dimensions.x = (e.width + e.height) * 0.5f;
			dimensions.y = dimensions.x;
		} else if (object instanceof PolylineMapObject) {
			Polyline p = ((PolylineMapObject)object).getPolyline();
			float maxX = 0;
			float maxY = 0;
			float minX = 0;
			float minY = 0;
			boolean isX = true;
			for (float v : p.getVertices()) {
				if (isX) {
					if (v < minX) {
						minX = v;
					} else if (v > maxX) {
						maxX = v;
					}
				} else {
					if (v < minY) {
						minY = v;
					} else if (v > maxY) {
						maxY = v;
					}
				}
				isX = !isX;
			}
			dimensions.x = maxX - minX;
			dimensions.y = maxY - minY;
		} else {
			throw new IllegalArgumentException(object.getName()
					+ " Unsupported MapObject: "
					+ object.getClass().getName());
		}
		return dimensions;
	}

	private float getFloatProperty(MapObject object, String key, float def) {
		String prop = object.getProperties().get(key, null, String.class);
		if (prop == null) {
			return def;
		}
		return Float.valueOf(prop);
	}

	static final class BodyDefBuilder {
		private final BodyDef def = new BodyDef();

		public BodyDef build() {
			return def;
		}

		/**
		 * how quickly spin degrades over time, range between 0.0 and 1.0
		 *
		 * @param val
		 * @return
		 */
		public BodyDefBuilder angularDampening(float val) {
			def.angularDamping = val;
			return this;
		}

		/**
		 * how quickly speed degrades over time, range between 0.0 and 1.0
		 *
		 * @param val
		 * @return
		 */
		public BodyDefBuilder linearDamping(float val) {
			def.linearDamping = val;
			return this;
		}

		/**
		 * position in the world in meters
		 *
		 * @param x
		 * @param y
		 * @return
		 */
		public BodyDefBuilder position(float x, float y) {
			def.position.set(x, y);
			return this;
		}

		/**
		 * the body type, static bodies do not move, kinematic bodies move but
		 * are not affected by forces in the world, dynamic bodies move and are
		 * affected by the world.
		 *
		 * @param val
		 * @return
		 */
		public BodyDefBuilder type(BodyType val) {
			def.type = val;
			return this;
		}

		/**
		 * prevent spin and angular velocity
		 *
		 * @param val
		 * @return
		 */
		public BodyDefBuilder fixedRotation(boolean val) {
			def.fixedRotation = val;
			return this;
		}

		public BodyDefBuilder reset() {
			def.angularDamping = 0;
			def.linearDamping = 0;
			def.position.set(0, 0);
			def.type = BodyType.StaticBody;
			def.fixedRotation = false;
			return this;
		}
	}

	static final class FixtureDefBuilder {
		private final FixtureDef def = new FixtureDef();

		public FixtureDef build() {
			return def;
		}

		/**
		 * the shape of the fixture
		 *
		 * @param val
		 * @return
		 */
		public FixtureDefBuilder shape(Shape val) {
			def.shape = val;
			return this;
		}

		/**
		 * the friction used when the fixture collides with another fixture
		 * range between 0.0 and 1.0
		 *
		 * @param val
		 * @return
		 */
		public FixtureDefBuilder friction(float val) {
			def.friction = val;
			return this;
		}

		/**
		 * the density, kg/m^2
		 *
		 * @param val
		 * @return
		 */
		public FixtureDefBuilder density(float val) {
			def.density = val;
			return this;
		}

		/**
		 * the bouncyness, range between 0.0 and 1.0
		 *
		 * @param val
		 * @return
		 */
		public FixtureDefBuilder restitution(float val) {
			def.restitution = val;
			return this;
		}

		/**
		 * sensors do not generate a collision response, but do generate
		 * collision events
		 *
		 * @param val
		 * @return
		 */
		public FixtureDefBuilder isSensor(boolean val) {
			def.isSensor = val;
			return this;
		}

		public FixtureDefBuilder reset() {
			def.shape = null;
			def.friction = 0;
			def.density = 1.0f;
			def.restitution = 0;
			def.isSensor = false;
			return this;
		}
	}

	private static class PathResult {
		public final PathPoint head;
		public final Vector2 point;

		public PathResult(PathPoint head, Vector2 point) {
			this.head = head;
			this.point = point;
		}
	}

	private static class SwitchConnectionHelper {
		Map<String, Switch> switches = new HashMap<String, Switch>();
		Map<String, List<Activatable>> activatables = new HashMap<String, List<Activatable>>();

		public void add(String id, Switch swtch) {
			switches.put(id, swtch);
		}

		public void add(Activatable item, String switchId) {
			if (!activatables.containsKey(switchId)) {
				activatables.put(switchId, new ArrayList<Activatable>());
			}
			activatables.get(switchId).add(item);
		}

		public void wireSwitches() {
			for (String id : switches.keySet()) {
				if (activatables.containsKey(id)) {
					for (Activatable a : activatables.get(id)) {
						switches.get(id).addActivatable(a);
						Gdx.app.log("SwitchWire", "Linking: " + id + " -> " + a.getClass().getSimpleName());
					}
				}
			}
		}
	}

	private static class TeleportationMeshHelper {
		Map<String, TeleporterSelectorStrategy> strategies = new HashMap<String, TeleporterSelectorStrategy>();
		Map<String, TeleporterTarget> targets = new HashMap<String, TeleporterTarget>();
		Map<String, String[]> associations = new HashMap<String, String[]>();

		public void add(String id, TeleporterTarget target) {
			targets.put(id, target);
		}

		public void add(String id, TeleporterTarget target,
				TeleporterSelectorStrategy strategy, String[] endPoints) {
			targets.put(id, target);
			strategies.put(id, strategy);
			associations.put(id, endPoints);
		}

		public void buildMesh() {
			for (String source : strategies.keySet()) {
				for (String target : associations.get(source)) {
					strategies.get(source).addTarget(targets.get(target));
					Gdx.app.log("TeleporterMesh", "Linking: " + source + " -> " + target);
				}
			}
		}
	}
}
