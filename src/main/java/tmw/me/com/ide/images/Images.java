package tmw.me.com.ide.images;

import tmw.me.com.ide.codeEditor.languages.Styles;

import java.util.Objects;

public final class Images {

    public static String get(String name) {
        ClassLoader loader = Styles.class.getClassLoader();
        return Objects.requireNonNull(loader.getResource("ide/images/" + name)).toExternalForm();
    }

}
