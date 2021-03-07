package tmw.me.com.ide.codeEditor.languages.addon;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Arrays;

public class AddonJSON {

    public String name;
    @SerializedName(value = "file-associations")
    public String[] fileAssociations;

    public static AddonJSON fromFile(File file) {
        return JSONHelper.fromFile(file, AddonJSON.class);
    }

    public static AddonJSON makeDefault() {
        AddonJSON json = new AddonJSON();
        json.name = "Untitled";
        json.fileAssociations = new String[0];
        return json;
    }

    @Override
    public String toString() {
        return "AddonJSON{" +
                "name='" + name + '\'' +
                ", fileAssociations=" + Arrays.toString(fileAssociations) +
                '}';
    }
}
