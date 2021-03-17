package tmw.me.com.ide.codeEditor.languages.addon;

import tmw.me.com.ide.codeEditor.languages.LanguageSupport;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddonLanguageSupport extends LanguageSupport {

    private final LanguageAddon addon;

    public static AddonLanguageSupport fromDir(File dir) {
        LanguageAddon addon = LanguageAddon.newAddon(dir);
        if (addon == null) {
            return null;
        }
        AddonLanguageSupport support = new AddonLanguageSupport(addon);
        return support;
    }

    private final Pattern pattern;

    AddonLanguageSupport(LanguageAddon addon) {
        super(addon.getStyleSheet(), addon.getName());
        this.addon = addon;

        StringBuilder patternBuilder = new StringBuilder();
        for (StyleFactoryJSON jsonFactory : addon.getHighlighterJSON().styles) {
            if (jsonFactory.id.length() > 0) {
                patternBuilder.append("|(?<").append(jsonFactory.id.toUpperCase()).append(">")
                        .append(jsonFactory.regex).append(")");
            }
        }
        pattern = Pattern.compile(patternBuilder.length() > 1 ? patternBuilder.substring(1) : patternBuilder.toString());
    }

    @Override
    public Pattern generatePattern() {
        return pattern;
    }

    @Override
    public String styleClass(Matcher matcher) {
        for (StyleFactoryJSON jsonFactory : addon.getHighlighterJSON().styles) {
            if (jsonFactory.id.toUpperCase().length() > 0 && matcher.group(jsonFactory.id.toUpperCase()) != null) {
                return jsonFactory.style;
            }
        }
        return null;
    }

    public LanguageAddon getAddon() {
        return addon;
    }
}
