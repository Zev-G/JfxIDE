package tmw.me.com.ide.codeEditor.languages.styles;

public final class LanguageStyles {

    public static String get(String name) {
        return LanguageStyles.class.getResource(name.endsWith(".css") ? name : name + ".css").toExternalForm();
    }

}
