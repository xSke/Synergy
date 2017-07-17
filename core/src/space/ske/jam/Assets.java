package space.ske.jam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;

public class Assets {
    public static Music hum = Gdx.audio.newMusic(Gdx.files.internal("audio/hum.ogg"));
    public static Sound bounce = Gdx.audio.newSound(Gdx.files.internal("audio/bounce.ogg"));
    public static Sound pop = Gdx.audio.newSound(Gdx.files.internal("audio/pop.ogg"));
    public static Sound ded = Gdx.audio.newSound(Gdx.files.internal("audio/ded.ogg"));
    public static Sound win = Gdx.audio.newSound(Gdx.files.internal("audio/win.ogg"));
    public static Sound whoosh = Gdx.audio.newSound(Gdx.files.internal("audio/whoosh.ogg"));


    public static DistanceFieldFont normalFont = new DistanceFieldFont(Gdx.files.internal("fonts/normal.fnt"));
    public static DistanceFieldFont italicFont = new DistanceFieldFont(Gdx.files.internal("fonts/italic.fnt"));

    static {
        hum.setLooping(true);
        normalFont.setDistanceFieldSmoothing(1.5f);
        italicFont.setDistanceFieldSmoothing(1.5f);
    }
}
