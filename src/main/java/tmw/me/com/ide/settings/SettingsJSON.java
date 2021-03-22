package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;

@DisplayedJSON(editMethod = DisplayedJSON.EditMethod.FULL_OBJECT)
public class SettingsJSON {

    @DisplayedJSON(editable = false, fontSize = 24, bold = true, enforcePosition = 0, additionalStyleClasses = { "white-text" })
    @SerializedName(value = "page-name")
    public String pageName;
    @DisplayedJSON(editable = false, fontSize = 12, enforcePosition = 1)
    public String description;

    public static SettingsJSON defaultJSON() {
        SettingsJSON json = new SettingsJSON();
        json.pageName = "Untitled";
        json.description = "This page's description has yet to be set";
        return json;
    }

}
