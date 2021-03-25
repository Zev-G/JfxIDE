package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;

public class IdeSettingJSON extends SettingsJSON {

    @SerializedName("default-style")
    public String defaultStyle;

    public static IdeSettingJSON makeDefault() {
        IdeSettingJSON json = new IdeSettingJSON();
        json.pageName = "Ide Settings";
        json.description = "This page contains the general settings for the IDE.";
        json.defaultStyle = "atom";
        return json;
    }

}
