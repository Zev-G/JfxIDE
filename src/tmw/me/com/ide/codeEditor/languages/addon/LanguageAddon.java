package tmw.me.com.ide.codeEditor.languages.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tmw.me.com.ide.codeEditor.languages.LanguageSupplier;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class LanguageAddon implements LanguageSupplier<AddonLanguageSupport> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] requiredFiles = new String[]{"style.css", "highlighter.json", "addon.json"};

    private final File dir;
    private File styleFile;
    private File highlighterFile;
    private File addonFile;

    private AddonJSON addonJSON;
    private HighlighterJSON highlighterJSON;

    public static LanguageAddon newAddon(File dir) {
        if (!verifyDir(dir)) {
            return null;
        }
        LanguageAddon addon = new LanguageAddon(dir);
        if (addon.getAddonJSON() == null)
            return null;
        return addon;
    }

    public static LanguageAddon createAtDir(File dir) throws IOException {
        File[] filesArray = dir.listFiles();
        ArrayList<String> files = new ArrayList<>();
        if (filesArray != null) {
            for (File file : filesArray) {
                files.add(file.getName());
            }
        }
        if (!files.contains("style.css")) {
            new File(dir, "style.css").createNewFile();
        }
        if (!files.contains("addon.json")) {
            File addonFile = new File(dir, "addon.json");
            addonFile.createNewFile();
            AddonJSON defaultAddonJSON = AddonJSON.makeDefault();
            JSONHelper.toFile(addonFile, defaultAddonJSON);
        }
        if (!files.contains("highlighter.json")) {
            File highlighterFile = new File(dir, "highlighter.json");
            highlighterFile.createNewFile();
            HighlighterJSON defaultHighlighterJSON = HighlighterJSON.makeDefault();
            JSONHelper.toFile(highlighterFile, defaultHighlighterJSON);
        }
        return new LanguageAddon(dir);
    }

    private LanguageAddon(File dir) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().equals(requiredFiles[0])) {
                styleFile = file;
            } else if (file.getName().equals(requiredFiles[1])) {
                highlighterFile = file;
            } else if (file.getName().equals(requiredFiles[2])) {
                addonFile = file;
            }
        }
        this.dir = dir;
        addonJSON = AddonJSON.fromFile(addonFile);
        highlighterJSON = HighlighterJSON.fromFile(highlighterFile);
    }

    public String getStyleSheet() {
        try {
            return styleFile.toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean verifyDir(File dir) {
        if (dir == null || !dir.isDirectory() || dir.listFiles() == null)
            return false;
        ArrayList<String> notFound = new ArrayList<>(Arrays.asList(requiredFiles));
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            notFound.remove(file.getName());
        }
        return notFound.isEmpty();
    }

    public void setHighlighterJSON(HighlighterJSON highlighterJSON) {
        this.highlighterJSON = highlighterJSON;
    }

    public void setAddonJSON(AddonJSON addonJSON) {
        this.addonJSON = addonJSON;
    }

    public AddonJSON getAddonJSON() {
        return addonJSON;
    }

    public HighlighterJSON getHighlighterJSON() {
        return highlighterJSON;
    }

    @Override
    public AddonLanguageSupport get() {
        return AddonLanguageSupport.fromDir(dir);
    }

    @Override
    public String getName() {
        return addonJSON.name;
    }

    public AddonLanguageSupport buildNewLanguageSupport() {
        return new AddonLanguageSupport(this);
    }

    public File getStyleFile() {
        return styleFile;
    }

    public File getHighlighterFile() {
        return highlighterFile;
    }

    public File getAddonFile() {
        return addonFile;
    }
}
