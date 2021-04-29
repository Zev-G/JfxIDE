package tmw.me.com.jfxhelper;


import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class PowerfulStyler {

    private final Node node;
    private final HashMap<String, String> styleAndValueMap = new HashMap<>();

    public PowerfulStyler(Node styleable, String... values) {
        this.node = styleable;
        addStyles(values);
    }

    public void addStyles(String... names) {
        for (String s : names)
            addStyle(s);
    }

    public void addStyle(String name) {
        styleAndValueMap.putIfAbsent(name, null);
    }

    public void removeStyle(String name) {
        styleAndValueMap.remove(name);
    }

    public void removeStyleAndUpdate(String name) {
        removeStyle(name);
        update();
    }

    public void setStyleValue(String key, String value) {
        styleAndValueMap.put(key, value);
    }

    public void setStyleAndUpdate(String name, String value) {
        setStyleValue(name, value);
        update();
    }

    public void update() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : styleAndValueMap.entrySet()) {
            if (entry.getValue() != null) {
                builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
            }
        }
        node.setStyle(builder.toString());
    }

}

