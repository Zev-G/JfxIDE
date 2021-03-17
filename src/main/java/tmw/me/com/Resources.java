package tmw.me.com;

import java.io.InputStream;
import java.util.Objects;

public final class Resources {

    public static final String EDITOR_STYLES = "ide/editor/styles/";
    public static final String EDITOR = "ide/editor/";
    public static final String IDE = "ide/";

    public static String getExternalForm(String path) {
        ClassLoader loader = Resources.class.getClassLoader();
        return Objects.requireNonNull(loader.getResource(path)).toExternalForm();
    }

    public static InputStream getAsStream(String path) {
        ClassLoader loader = Resources.class.getClassLoader();
        return Objects.requireNonNull(loader.getResourceAsStream(path));
    }

}
