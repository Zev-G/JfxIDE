package tmw.me.com.ide.tools.builders;

import java.lang.reflect.InvocationTargetException;

public abstract class Builder<T> {

    public abstract T build() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException;

}
