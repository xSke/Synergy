package space.ske.jam.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import space.ske.jam.SpringingContext1D;
import space.ske.jam.TileType;

public class Particle extends Entity {
    private final float radius;
    private final Color color;
    private Vector2[] pastPositions;
    private float timer;
    private float time;
    private float timeTimer;
    private boolean dead;

    private SpringingContext1D radiusSpring = new SpringingContext1D(1, 2);

    public Particle(float trailLengthSeconds, float radius, Color color, float time) {
        getBody().setType(BodyDef.BodyType.DynamicBody);
        Filter f = new Filter();
        f.categoryBits = 0x08;
        f.maskBits = 0x01;

        Fixture fix = createCircleFixture(0.05f);
        fix.setRestitution(0.6f);
        fix.setFilterData(f);

        this.pastPositions = new Vector2[(int) (trailLengthSeconds * 50)];
        this.radius = radius;
        this.color = color;
        this.time = time;

        radiusSpring.target = radiusSpring.value = radius;

        for (int i = 0; i < pastPositions.length; i++) {
            pastPositions[i] = new Vector2();
        }
    }

    public void init() {
        for (Vector2 pastPosition : pastPositions) {
            pastPosition.set(body.getPosition());
        }
    }

    @Override
    public void update(float deltaTime) {
        radiusSpring.update(deltaTime);

        timer += deltaTime;
        while (timer > 0.02f) {
            timer -= 0.02f;
            commitPosition();
        }

        timeTimer += deltaTime;
        if (timeTimer > time && time > 0) {
            kill();
        }

        boolean notNull = false;
        for (Vector2 pastPosition : pastPositions) {
            if (pastPosition != null) notNull = true;
        }
        if (!notNull) {
            killSelf();
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        sr.setColor(color);
        for (int i = 0; i < pastPositions.length; i++) {
            Vector2 pastPosition = pastPositions[i];
            Vector2 nextPos = i == 0 ? body.getPosition() : pastPositions[i - 1];
            float frac = i / (float) pastPositions.length;

            float frad = radiusSpring.value * (1 - frac);
            if (pastPosition == null) continue;
            sr.circle(pastPosition.x, pastPosition.y, frad, 8);

            if (nextPos == null) continue;
            sr.rectLine(pastPosition.x, pastPosition.y, nextPos.x, nextPos.y, frad * 2);
        }
    }

    private void commitPosition() {
        for (int i = pastPositions.length - 2; i >= 0; i--) {
            if (pastPositions[i] == null) {
                pastPositions[i + 1] = null;
            } else {
                pastPositions[i + 1].set(pastPositions[i]);
            }
        }
        if (dead) {
            pastPositions[0] = null;
        } else {
            pastPositions[0].set(getBody().getPosition());
        }
    }

    @Override
    public void collide(TileType type, Fixture self) {
        super.collide(type, self);
        if (time == 0) kill();
    }

    private void kill() {
        if (dead) return;
//        dead = true;
//        pastPositions[0].set(getBody().getPosition());
        radiusSpring.target = 0;
    }
}
