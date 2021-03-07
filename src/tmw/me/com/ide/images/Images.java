package tmw.me.com.ide.images;

public final class Images {

    public static String get(String name) {
        return Images.class.getResource(name).toExternalForm();
    }

}
