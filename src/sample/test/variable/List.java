package sample.test.variable;

import java.util.Arrays;
import java.util.HashMap;

public class List extends Variable<List> {

    private final HashMap<String, Object> values = new HashMap<>();

    public List(String name, boolean global) {
        super(name, global);
    }

    public Object get(Integer i) {
        return values.get(i.toString());
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void add(Object obj, String name) {
        this.values.put(name, obj);
    }
    public void remove(String name) {
        this.values.remove(name);
    }
    public void removeObject(Object obj) {
        this.values.remove(obj.toString());
    }

    @SafeVarargs
    public static <T> List fromList(T... values) {
        return fromList(Arrays.asList(values));
    }
    public static <T> List fromList(java.util.List<T> list) {
        int i = 0;
        List ourList = new List("temp", false);
        for (T t : list) {
            i++;
            ourList.add(t, String.valueOf(i));
        }
        return ourList;
    }

}
