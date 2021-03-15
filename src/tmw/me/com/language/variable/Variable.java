package tmw.me.com.language.variable;

import javafx.beans.value.ChangeListener;

/**
 * <p>
 * This class is used for storing and retrieving values for a variable. This variable has a name and can have a listener attached to it.
 * </p>
 *
 * @param <T> The type of the value stored inside.
 */
public class Variable<T> {

    private final boolean global;

    private String name;
    private T value;

    public ChangeListener<T> changeListener;

    public Variable(T value, String name, boolean global) {
        this.value = value;
        this.name = name;
        this.global = global;
    }

    public Variable(String name, boolean global) {
        this.name = name;
        this.global = global;
    }

    public T getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (changeListener != null) {
            changeListener.changed(null, this.value, (T) value);
        }
        this.value = (T) value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setChangeListener(ChangeListener<T> changeListener) {
        changeListener.changed(null, value, value);
        this.changeListener = changeListener;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
