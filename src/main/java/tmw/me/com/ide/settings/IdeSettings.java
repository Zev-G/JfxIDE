package tmw.me.com.ide.settings;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import tmw.me.com.Resources;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;
import tmw.me.com.ide.codeEditor.languages.addon.JSONHelper;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class IdeSettings {

    // Properties
    public static ObservableList<String> ADDON_PATHS;
    public static IntegerProperty TAB_SIZE = new SimpleIntegerProperty(4);
    // ----------

    private static final String PROGRAM_NAME = "JfxIDE";

    private static final String APP_DATA = System.getenv("APPDATA");
    private static final String PROGRAM_FOLDER = APP_DATA + "\\" + PROGRAM_NAME;

    private static final File PROGRAM_FILE = new File(PROGRAM_FOLDER);
    private static final File SETTINGS_FOLDER = new File(PROGRAM_FOLDER + "\\" + "settings");

    private static File IDE_FILE;
    private static IdeSettingJSON ideJSON;

    private static File ADDONS_FILE;
    private static AddonSettingJSON addonJSON;

    private static File EDITOR_FILE;
    private static EditorSettingJSON editorJSON;

    public static final List<FileAndInstance<SettingsJSON>> SETTING_PAGES = new ArrayList<>();

    public static final List<String> THEMES = Arrays.asList( "atom", "palenight", "oceanic", "deepocean", "monokai", "dracula", "solarized", "nightowl" );
    public static StringProperty currentTheme = new SimpleStringProperty();

    public static void start() throws IOException {
        SETTING_PAGES.clear();
        ADDONS_FILE = new File(SETTINGS_FOLDER, "addons.json");
        EDITOR_FILE = new File(SETTINGS_FOLDER, "editor.json");
        IDE_FILE = new File(SETTINGS_FOLDER, "ide.json");
        ADDON_PATHS = FXCollections.observableArrayList();
        LanguageLibrary.defaultLanguages = FXCollections.observableArrayList(LanguageLibrary.HARD_CODED_LANGUAGES);
        createListeners();
        if (!PROGRAM_FILE.exists()) {
            PROGRAM_FILE.mkdir();
        }
        if (!SETTINGS_FOLDER.exists()) {
            SETTINGS_FOLDER.mkdir();
        }
        if (!EDITOR_FILE.exists()) {
            EDITOR_FILE.createNewFile();
            editorJSON = EditorSettingJSON.defaultJSON();
            JSONHelper.toFile(EDITOR_FILE, editorJSON);
        } else {
            editorJSON = JSONHelper.fromFile(EDITOR_FILE, EditorSettingJSON.class);
        }
        if (!IDE_FILE.exists()) {
            IDE_FILE.createNewFile();
            ideJSON = IdeSettingJSON.makeDefault();
            JSONHelper.toFile(EDITOR_FILE, ideJSON);
        } else {
            ideJSON = JSONHelper.fromFile(IDE_FILE, IdeSettingJSON.class);
        }
        if (!ADDONS_FILE.exists()) {
            ADDONS_FILE.createNewFile();
            addonJSON = AddonSettingJSON.makeDefault();
            JSONHelper.toFile(ADDONS_FILE, addonJSON);
        } else {
            addonJSON = JSONHelper.fromFile(ADDONS_FILE, AddonSettingJSON.class);
        }
        for (String path : addonJSON.addonPaths) {
            try {
                if (new File(path).exists()) {
                    ADDON_PATHS.add(path);
                }
            } catch (NullPointerException exception) {
                System.err.println("The file: { " + path + " } does not exist.");
            }
        }
        SETTING_PAGES.add(new FileAndInstance<>(ADDONS_FILE, addonJSON));
        SETTING_PAGES.add(new FileAndInstance<>(EDITOR_FILE, editorJSON));
        currentTheme.set(ideJSON.defaultStyle);
    }

    private static void createListeners() {
        ADDON_PATHS.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                for (String path : change.getAddedSubList()) {
                    File tempFile = new File(path);
                    if (tempFile.exists()) {
                        LanguageAddon addon = LanguageAddon.newAddon(tempFile);
                        if (addon != null) {
                            LanguageLibrary.defaultLanguages.add(addon);
                            if (!addonJSON.addonPaths.contains(tempFile.getAbsolutePath())) {
                                addonJSON.addonPaths.add(tempFile.getAbsolutePath());
                                JSONHelper.toFile(ADDONS_FILE, addonJSON);
                            }
                        }
                    }
                }
                for (String path : change.getRemoved()) {
                    addonJSON.addonPaths.remove(path);
                }
            }
        });
        currentTheme.addListener((observable, oldValue, newValue) -> {
            ideJSON.defaultStyle = newValue;
            JSONHelper.toFile(IDE_FILE, ideJSON);
        });
    }

    public static String tabSize() {
        return " ".repeat(TAB_SIZE.get());
    }

    public static AddonSettingJSON getAddonJSON() {
        return addonJSON;
    }

    public static File getAddonsFile() {
        return ADDONS_FILE;
    }

    public static EditorSettingJSON getEditorJSON() {
        return editorJSON;
    }

    public static File getEditorFile() {
        return EDITOR_FILE;
    }

    public static class FileAndInstance<T> {

        private final File file;
        private final T instance;

        public FileAndInstance(File file, T instance) {
            this.file = file;
            this.instance = instance;
        }

        public File getFile() {
            return file;
        }

        public T getInstance() {
            return instance;
        }

        @Override
        public String toString() {
            return "FileAndInstance{" +
                    "file=" + file +
                    ", instance=" + instance +
                    '}';
        }

    }

    public static String getThemeFromName(String name) {
        return Resources.getExternalForm("ide/styles/" + name + ".css");
    }

}
