package tmw.me.com.ide.settings;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;
import tmw.me.com.ide.codeEditor.languages.addon.JSONHelper;
import tmw.me.com.ide.codeEditor.languages.addon.LanguageAddon;

import java.io.File;
import java.io.IOException;

public final class IdeSettings {

    // Properties
    public static final ObservableList<String> ADDON_PATHS = FXCollections.observableArrayList();
    // ----------

    private static final String PROGRAM_NAME = "JfxIDE";

    private static final String APP_DATA = System.getenv("APPDATA");
    private static final String PROGRAM_FOLDER = APP_DATA + "\\" + PROGRAM_NAME;

    private static final File PROGRAM_FILE = new File(PROGRAM_FOLDER);
    private static final File SETTINGS_FOLDER = new File(PROGRAM_FOLDER + "\\" + "settings");

    private static final File ADDONS_FILE = new File(SETTINGS_FOLDER, "addons.json");
    private static AddonSettingJSON addonJSON;

    public static void start() throws IOException {
        createListeners();
        if (!PROGRAM_FILE.exists()) { PROGRAM_FILE.mkdir(); }
        if (!SETTINGS_FOLDER.exists()) { SETTINGS_FOLDER.mkdir(); }
        if (!ADDONS_FILE.exists()) {
            ADDONS_FILE.createNewFile();
            addonJSON = AddonSettingJSON.makeDefault();
            JSONHelper.toFile(ADDONS_FILE, addonJSON);
        } else {
            addonJSON = JSONHelper.fromFile(ADDONS_FILE, AddonSettingJSON.class);
        }
        for (String path : addonJSON.addonPaths) {
            if (new File(path).exists()) {
                ADDON_PATHS.add(path);
            }
        }
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
            }
        });
    }

}
