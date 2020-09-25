package tmw.me.com.language.variable;

import java.util.List;

public class LinkedList<T> extends tmw.me.com.language.variable.List {

    private final List<T> linkedList;
    private final Class<T> genericClass;

    public LinkedList(String name, boolean global, List<T> list, Class<T> tClass) {
        super(name, global);
        this.linkedList = list;
        this.genericClass = tClass;
        int i = 0;
        for (T value : list) {
            getValues().put(String.valueOf(i), value);
            i++;
        }
    }

    @Override
    public void add(Object obj, String name) {
        super.add(obj, name);
        Object realObj = obj instanceof Variable ? ((Variable<?>) obj).getValue() : obj;
        if (genericClass.isAssignableFrom(realObj.getClass())) {
            linkedList.add((T) realObj);
        }
    }

    @Override
    public void remove(String name) {
        Object obj = getValues().get(name);
        Object realObj = obj instanceof Variable ? ((Variable<?>) obj).getValue() : obj;
        super.remove(name);
        if (genericClass.isAssignableFrom(realObj.getClass())) {
            linkedList.remove((T) realObj);
        }
    }
    @Override
    public void removeObject(Object obj) {
        super.removeObject(obj);
        Object realObj = obj instanceof Variable ? ((Variable<?>) obj).getValue() : obj;
        if (genericClass.isAssignableFrom(realObj.getClass())) {
            linkedList.remove((T) realObj);
        }
    }

    public List<T> getLinkedList() {
        return linkedList;
    }
    public Class<T> getGenericClass() {
        return genericClass;
    }

    @Override
    public String toString() {
        return "LinkedList{" +
                "linkedList=" + linkedList +
                '}';
    }
}
