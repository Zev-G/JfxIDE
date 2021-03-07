package tmw.me.com.ide.tools.control;

public interface JFXConstructable<T> {

    T addStyleClass(String styleClass);
    T addAllStyleClass(String... styleClass);

    T removeStyleClass(String styleClass);
    T removeAllStyleClass(String... styleClass);

}
