package space.ske.jam.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import space.ske.jam.*;

public class Ball extends Entity {
    private final Fixture bodyFix;

    private int collisions;
    private boolean hit;
    private SpringingContext1D size = new SpringingContext1D(0.4f, 4);

    public Ball() {
        Filter f = new Filter();
        f.categoryBits = 0x04;
        f.maskBits = 0x15;

        body.setType(BodyDef.BodyType.DynamicBody);
        body.setGravityScale(0.3f);
        bodyFix = createCircleFixture(0.5f);
        bodyFix.setFilterData(f);
        bodyFix.setRestitution(0.6f);
        createCircleFixture(3f).setSensor(true);
    }

    @Override
    public void collide(TileType type, Fixture self) {
        if (self != bodyFix) return;

        if (collisions == 0) {
            Sound sound = Assets.bounce;
            Util.playSoundWithParams(sound, body.getLinearVelocity().len(), body.getPosition().x);
            size.velocity = 10;

//            Jam.i.setScreenshake(0.05f);
        }

        collisions++;
        if (type == TileType.GOAL) {
            if (!hit) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Assets.win.play();
                        Jam.i.setScreenshake(0.2f);
                        spawnParticles(body.getPosition(), Color.CHARTREUSE);
                        body.setType(BodyDef.BodyType.StaticBody);
                        Jam.i.nextLevel();
                    }
                });
                hit = true;
            }
        } else if (type == TileType.DAMAGE) {
            final Vector2 pos = body.getPosition().cpy();


            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Assets.ded.play();
                    Jam.i.setScreenshake(0.5f);
                    Jam.i.restartButNoEffectsPls();
                    spawnParticles(pos, new Color(0.906f, 0.298f, 0.235f, 1f));
                }
            });
        }
    }

    @Override
    public void stopCollide(TileType type, Fixture self) {
        if (self != bodyFix) return;

        collisions--;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        size.update(deltaTime);
        size.target = 0.5f;
    }

    private void spawnParticles(Vector2 pos, Color color) {
        for (int i = 0; i < 20; i++) {
            Particle p = new Particle(0.1f, 0.1f, color, MathUtils.random(0.3f, 1f));
            p.body.setGravityScale(0.1f);
            p.body.setTransform(pos, 0);
            p.body.setLinearVelocity(new Vector2(MathUtils.random(10, 40), 0).rotate(MathUtils.random(360f)));
            p.init();
            Jam.i.getEntities().add(p);
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        sr.setColor(Color.CHARTREUSE);
        float radius = size.value;
        sr.circle(body.getPosition().x, body.getPosition().y, radius, 16);

        Util.drawHollowCircle(sr, body.getPosition().x, body.getPosition().y, 2.5f, 0.1f, 32);
    }
}
