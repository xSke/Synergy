package space.ske.jam;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class SpringingContext2D {
    private float frequency;
    private float damping;
    private Vector2 value = new Vector2();
    private Vector2 velocity = new Vector2();
    private Vector2 target = new Vector2();

    public SpringingContext2D(float frequency, float damping) {
        this.frequency = frequency;
        this.damping = damping;
    }

    public void update(float deltaTime) {
        float angularFrequency = frequency;
        angularFrequency *= MathUtils.PI2;

        {
            float f = 1.0f + 2.0f * deltaTime * damping * angularFrequency;
            float oo = angularFrequency * angularFrequency;
            float hoo = deltaTime * oo;
            float hhoo = deltaTime * hoo;
            float detInv = 1.0f / (f + hhoo);
            float detX = f * value.x + deltaTime * velocity.x + hhoo * target.x;
            float detV = velocity.x + hoo * (target.x - value.x);
            value.x = detX * detInv;
            velocity.x = detV * detInv;
        }

        {
            float f = 1.0f + 2.0f * deltaTime * damping * angularFrequency;
            float oo = angularFrequency * angularFrequency;
            float hoo = deltaTime * oo;
            float hhoo = deltaTime * hoo;
            float detInv = 1.0f / (f + hhoo);
            float detX = f * value.y + deltaTime * velocity.y + hhoo * target.y;
            float detV = velocity.y + hoo * (target.y - value.y);
            value.y = detX * detInv;
            velocity.y = detV * detInv;
        }
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

    public Vector2 getPosition() {
        return value;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getTarget() {
        return target;
    }
}