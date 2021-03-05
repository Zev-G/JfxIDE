package tmw.me.com.ide.codeEditor.languages;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public final class LanguageLibrary {

    public static ArrayList<Class<? extends LanguageSupport>> allLanguageClasses = new ArrayList<>(Arrays.asList(
            SfsLanguage.class, CssLanguage.class, XmlLanguage.class, MathLanguage.class, JavaLanguage.class, PlainTextLanguage.class, SkriptLanguage.class));

    public static LanguageSupport[] genNewLanguages() {
        LanguageSupport[] languageSupports = new LanguageSupport[allLanguageClasses.size()];
        for (int i = 0; i < allLanguageClasses.size(); i++) {
            try {
                languageSupports[i] = allLanguageClasses.get(i).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                System.err.println("Language " + allLanguageClasses.get(i).getSimpleName() + " does not have an empty constructor. (" + e.getClass().getSimpleName() + ")");
            }
        }
        return languageSupports;
    }
}
