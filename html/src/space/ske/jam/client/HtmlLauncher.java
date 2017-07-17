package space.ske.jam.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import space.ske.jam.Jam;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration gac = new GwtApplicationConfiguration(1280, 768);
        gac.antialiasing = true;
        return gac;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new Jam();
    }
}