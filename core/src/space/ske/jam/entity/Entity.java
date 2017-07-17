package space.ske.jam.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import space.ske.jam.Jam;
import space.ske.jam.TileType;

public class Entity {
    protected Body body;

    public Entity() {
        body = Jam.i.getWorld().createBody(new BodyDef());
        body.setUserData(this);
    }

    public Fixture createCircleFixture(float radius) {
        CircleShape cs = new CircleShape();
        cs.setRadius(radius);
        Fixture fixture = body.createFixture(cs, 1);
        cs.dispose();
        return fixture;
    }
    public Fixture createEdgeFixture(Vector2 from, Vector2 to) {
        EdgeShape es = new EdgeShape();
        es.set(from, to);
        Fixture fixture = body.createFixture(es, 1);
        es.dispose();
        return fixture;
    }


    public void update(float deltaTime) {

    }

    public void render(ShapeRenderer sr) {

    }

    public void collide(Entity entity, Fixture self) {

    }

    public void collide(TileType type, Fixture self) {

    }

    public void stopCollide(Entity entity, Fixture self) {

    }

    public void stopCollide(TileType type, Fixture self) {

    }

    public void killSelf() {
        Jam.i.getEntities().removeValue(this, true);
        Jam.i.getWorld().destroyBody(body);
    }

    public Body getBody() {
        return body;
    }
}
