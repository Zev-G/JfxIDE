package tmw.me.com.ide.codeEditor.languages.addon;

import com.google.gson.Gson;

import java.io.*;

public final class JSONHelper {

    public static <T> T fromFile(File file, Class<T> tClass) {
        return fromFile(LanguageAddon.GSON, file, tClass);
    }
    public static <T> T fromFile(Gson gson, File file, Class<T> tClass) {
        try {
            return gson.fromJson(new FileReader(file), tClass);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    public static void toFile(File file, Object toBeWritten) {
        toFile(LanguageAddon.GSON, file, toBeWritten);
    }
    public static void toFile(Gson gson, File file, Object toBeWritten) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            writer.print(gson.toJson(toBeWritten));
        } catch (IOException e) {
            // ... handle IO exception
        }
    }

}
