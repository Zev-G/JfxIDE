package tmw.me.com.ide.codeEditor.languages;

import java.util.Objects;

public final class Styles {

    public static String forName(String name) {
        ClassLoader loader = Styles.class.getClassLoader();
        return Objects.requireNonNull(loader.getResource("ide/editor/languages/styles/" + name + (name.endsWith(".css") ? "" : ".css"))).toExternalForm();
    }

}
