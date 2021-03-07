package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AddonSettingJSON extends SettingsJSON {

    @SerializedName(value = "addon-paths")
    public ArrayList<String> addonPaths;

    public static AddonSettingJSON makeDefault() {
        AddonSettingJSON json = new AddonSettingJSON();
        json.addonPaths = new ArrayList<>();
        json.pageName = "Addons";
        json.description = "Not set yet.";
        return json;
    }

}
