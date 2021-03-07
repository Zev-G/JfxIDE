package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;

public class SettingsJSON {

    @SerializedName(value = "page-name")
    public String pageName;
    public String description;

    public static SettingsJSON defaultJSON() {
        SettingsJSON json = new SettingsJSON();
        json.pageName = "Untitled";
        json.description = "This page's description has yet to be set";
        return json;
    }

}
