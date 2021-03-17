package tmw.me.com.ide.codeEditor.fonts;

import tmw.me.com.ide.codeEditor.languages.Styles;

import java.util.Objects;

public final class Fonts {

    public static String ttf(String name) {
        ClassLoader loader = Styles.class.getClassLoader();
        return Objects.requireNonNull(loader.getResource("ide/editor/fonts/ttf/" + name + (name.endsWith(".ttf") ? "" : ".ttf"))).toExternalForm();
    }

}
