package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;

import java.util.ArrayList;

@DisplayedJSON(editMethod = DisplayedJSON.EditMethod.FULL_OBJECT)
public class AddonSettingJSON extends SettingsJSON {

    @DisplayedJSON(editMethod = DisplayedJSON.EditMethod.FOLDER, title = "Paths to addons:")
    @SerializedName(value = "addon-paths")
    public ArrayList<String> addonPaths;

    public static AddonSettingJSON makeDefault() {
        AddonSettingJSON json = new AddonSettingJSON();
        json.addonPaths = new ArrayList<>();
        json.pageName = "Addons";
        json.description = "This page contains a list of paths to the folders which represent addons. \nAny changes here will get applied when the application is restarted.";
        return json;
    }

}
