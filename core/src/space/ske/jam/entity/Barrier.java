package space.ske.jam.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Barrier extends Entity {
    private final Vector2 from;
    private final Vector2 to;
    private float timer;

    public Barrier(Vector2 from, Vector2 to) {
        this.from = from;
        this.to = to;
        Filter f = new Filter();
        f.categoryBits = 0x10;
        f.maskBits = 0x04;
        Fixture fix = createEdgeFixture(from, to);
        fix.setFilterData(f);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        timer += deltaTime;
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        sr.setColor(Color.CHARTREUSE);

        float pulse = MathUtils.sin(timer * 5) * 0.05f;
        sr.rectLine(from.x, from.y, to.x, to.y, 0.1f + pulse);
    }
}
