package tmw.me.com.ide.settings;

import com.google.gson.annotations.SerializedName;
import tmw.me.com.ide.settings.annotations.DisplayedJSON;

@DisplayedJSON(editMethod = DisplayedJSON.EditMethod.FULL_OBJECT)
public class EditorSettingJSON extends SettingsJSON {

    @SerializedName(value = "font-size")
    @DisplayedJSON(editMethod = DisplayedJSON.EditMethod.INTEGER, useTitle = true, title = "Font size: ")
    public int fontSize;
    @SerializedName(value = "wrap-text")
    @DisplayedJSON(editMethod = DisplayedJSON.EditMethod.BOOLEAN, useTitle = true, title = "Wrap text: ")
    public boolean wrapText;

    public static EditorSettingJSON defaultJSON() {
        EditorSettingJSON json = new EditorSettingJSON();
        json.pageName = "Text Editor Settings";
        json.description = "This page contains all the information for a default text editor.";
        json.fontSize = 18;
        json.wrapText = true;
        return json;
    }

}
