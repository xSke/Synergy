package space.ske.jam.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import space.ske.jam.*;

public class Player extends Entity {
    private boolean isShooting;
    private Vector2 target;
    private float timer;
    private boolean didPress;
    private Ball ball;
    private SpringingContext1D pullForce = new SpringingContext1D(1, 0);
    private float beamAnim;

    public Player() {
        Filter f = new Filter();
        f.categoryBits = 0x02;
        f.maskBits = 0x03;

        Fixture bodyFix = createCircleFixture(0.5f);
        bodyFix.setRestitution(0.2f);
        bodyFix.setFilterData(f);
        body.setType(BodyDef.BodyType.DynamicBody);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        Assets.hum.play();
        pullForce.update(deltaTime);
        Assets.hum.setVolume(pullForce.value);

        timer += deltaTime;

        boolean isPressing = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (isPressing && !didPress) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            Jam.i.getCamera().unproject(mouse);

            final Vector2[] thePoint = new Vector2[1];
            final Ball[] ball = new Ball[1];
            body.getWorld().rayCast(new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                    Object ud = fixture.getBody().getUserData();
                    Object fud = fixture.getUserData();
                    if (ud instanceof Particle) return -1;
                    if (ud instanceof Barrier) return -1;

                    thePoint[0] = point.cpy();
                    if (ud instanceof Ball) {
                        ball[0] = (Ball) ud;
                    } else {
                        ball[0] = null;
                    }
                    if (fud == TileType.BRICK) {
                        thePoint[0] = null;
                        ball[0] = null;
                    }
                    return fraction;
                }
            }, body.getPosition(), new Vector2(mouse.x, mouse.y).sub(body.getPosition()).scl(999).add(body.getPosition()));

            if (ball[0] != null) {
                target = ball[0].body.getPosition();
                isShooting = true;
                this.ball = ball[0];
            } else if (thePoint[0] != null) {
                target = thePoint[0].cpy();
                isShooting = true;
            }

            if (isShooting) {
                beamAnim = 0;
                pullForce.target = 1;
                pullForce.frequency = 1.5f;
                Util.playSoundWithParams(Assets.pop, 5, target.x);
            }
        } else if (!isPressing && isShooting) {
            stopShooting();
        }

        if (isShooting) {
            body.setGravityScale(0);
            float speed = ball != null ? 25 : 35;
            speed *= pullForce.value;
            body.setLinearVelocity(body.getPosition().sub(target).nor().scl(-speed));

            if (ball != null) {
                ball.body.setGravityScale(0);
                ball.body.setLinearVelocity(ball.body.getPosition().cpy().sub(body.getPosition()).nor().scl(-speed * 1.5f));
            }
        } else {
            body.setGravityScale(1);
        }

        if (isShooting && target.dst(body.getPosition()) < 1f && ball != null) {
            stopShooting();
        }

        beamAnim += Gdx.graphics.getDeltaTime();

        didPress = isPressing;
    }

    private void stopShooting() {
        if (ball != null) {
            ball.body.setGravityScale(0.5f);
        }
        isShooting = false;
        ball = null;

        pullForce.target = 0;
        pullForce.frequency = 4;
        beamAnim = 0;
    }

    @Override
    public void render(ShapeRenderer sr) {
        drawBeam(sr);

        Vector2 pos = body.getPosition();
        sr.setColor(1, 1, 1, 1);
        sr.circle(pos.x, pos.y, 0.5f, 16);
    }

    private void drawBeam(ShapeRenderer sr) {
        sr.setColor(Color.CHARTREUSE);
        float width = 0.25f + MathUtils.sin(timer * 20) * 0.15f;
        if (isShooting) {
            sr.rectLine(body.getPosition(), target.cpy().sub(body.getPosition()).scl(Math.min(beamAnim * 20, 1)).add(body.getPosition()), width);
        } else if (target != null) {
            sr.rectLine(target.cpy().sub(body.getPosition()).scl(Math.min(beamAnim * 20, 1)).add(body.getPosition()), target, width);
        }
    }
}
