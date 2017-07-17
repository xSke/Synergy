package space.ske.jam;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Util {
    public static void drawHollowCircle(ShapeRenderer sr, float x, float y, float innerRad, float thickness, int segments) {
        float rad = innerRad + thickness / 2;

        Vector2 pos = new Vector2(x, y);

        for (int i = 0; i < segments; i++) {
            float angle = (i / (float) segments) * 360;
            float angleNext = ((i + 1) / (float) segments) * 360;
            sr.rectLine(
                    new Vector2(rad, 0).rotate(angle).add(pos),
                    new Vector2(rad, 0).rotate(angleNext).add(pos),
                    thickness
            );
        }
    }

    public static float calculatePan(float x) {
        return (x / Jam.i.getTiles().length) - 0.5f;
    }

    public static long playSoundWithParams(Sound sound, float vel, float x) {
        return sound.play(Math.min(vel, 20) / 20f, MathUtils.random(0.9f, 1.1f), calculatePan(x));
    }
}
