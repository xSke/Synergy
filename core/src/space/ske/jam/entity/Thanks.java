package space.ske.jam.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import space.ske.jam.Assets;
import space.ske.jam.Jam;

public class Thanks extends Entity {
    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        sr.end();

        Jam.i.getBatch().setProjectionMatrix(Jam.i.getScreenCamera().combined);
        Jam.i.getBatch().setShader(Jam.i.getDFShader());
        Jam.i.getBatch().begin();
        Assets.italicFont.getData().setScale(2f);
        Assets.italicFont.draw(Jam.i.getBatch(), "Thanks for playing!", 0, Gdx.graphics.getHeight() * 0.77f, Gdx.graphics.getWidth(), Align.center, false);
        Assets.italicFont.getData().setScale(1f);
        Assets.normalFont.draw(Jam.i.getBatch(), "That's all I have for you right now - but I may expand on this concept later on.", Gdx.graphics.getWidth() * 0.15f, Gdx.graphics.getHeight() * 0.65f, Gdx.graphics.getWidth() * 0.7f, Align.center, true);


        Assets.normalFont.draw(Jam.i.getBatch(), "I hope you enjoyed playing my little game.", Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.45f, Gdx.graphics.getWidth() * 0.8f, Align.center, true);

        Assets.normalFont.getData().setScale(0.7f);
        Assets.normalFont.draw(Jam.i.getBatch(), "Game by Ske (@SkeDevs)", Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.35f, Gdx.graphics.getWidth() * 0.8f, Align.center, true);
        Assets.normalFont.draw(Jam.i.getBatch(), "Audio from Freesound (andre.rocha.nascimento, unfa, SunnySideSound, qubodup, Terhen, fins, GameAudio)", Gdx.graphics.getWidth() * 0.15f, Gdx.graphics.getHeight() * 0.3f, Gdx.graphics.getWidth() * 0.7f, Align.center, true);
        Assets.normalFont.getData().setScale(1f);
        Jam.i.getBatch().end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }
}
