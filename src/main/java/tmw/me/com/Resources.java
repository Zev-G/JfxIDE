package tmw.me.com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Resources {

    public static final String EDITOR_STYLES = "ide/editor/styles/";
    public static final String EDITOR = "ide/editor/";
    public static final String IDE = "ide/";

    public static String getExternalForm(String path) {
        ClassLoader loader = Resources.class.getClassLoader();
        return Objects.requireNonNull(loader.getResource(path)).toExternalForm();
    }

    public static InputStream getAsStream(String path) {
        ClassLoader loader = Resources.class.getClassLoader();
        return loader.getResourceAsStream(path);
    }

    public static URL get(String path) {
        ClassLoader loader = Resources.class.getClassLoader();
        return loader.getResource(path);
    }

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

}
