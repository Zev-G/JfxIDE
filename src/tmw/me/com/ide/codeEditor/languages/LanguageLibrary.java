package tmw.me.com.ide.codeEditor.languages;

public final class LanguageLibrary {

    public static final CssLanguage CSS = new CssLanguage();
    public static final SfsLanguage SFS = new SfsLanguage();
    public static final JavaLanguage JAVA = new JavaLanguage();
    public static final XmlLanguage XML = new XmlLanguage();
    public static final MathLanguage MATH = new MathLanguage();
    public static final LanguageSupport[] ALL_LANGUAGES = new LanguageSupport[] { SFS, CSS, XML, MATH, JAVA };

    public static LanguageSupport[] genNewLanguages() {
        return new LanguageSupport[] { new SfsLanguage(), new CssLanguage(), new XmlLanguage(), new MathLanguage(), new JavaLanguage() };
    }
}
