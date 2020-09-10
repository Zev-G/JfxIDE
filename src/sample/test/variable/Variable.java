package sample.test.variable;

import javafx.beans.value.ChangeListener;

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
        System.out.println("Value of variable: " + name + " has been set to:  " + value);
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
