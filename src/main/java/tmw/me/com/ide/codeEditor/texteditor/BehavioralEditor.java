package tmw.me.com.ide.codeEditor.texteditor;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.highlighting.StyleSpansFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BehavioralEditor extends FilteredEditor implements Behavioral {

    private final ObservableList<Behavior> behaviors = FXCollections.observableArrayList();

    public BehavioralEditor() {
        this(Collections.emptyList());
    }
    public BehavioralEditor(Behavior... behaviors) {
        this(Arrays.asList(behaviors));
    }
    public BehavioralEditor(Collection<Behavior> behaviors) {
        this.behaviors.addListener((ListChangeListener<Behavior>) c -> {
            while (c.next()) {
                for (Behavior added : c.getAddedSubList()) {
                    behaviorAdded(added);
                }
                for (Behavior removed : c.getRemoved()) {
                    behaviorRemoved(removed);
                }
            }
        });

        this.behaviors.addAll(behaviors);
    }

    protected void behaviorAdded(Behavior behavior) {
        behavior.apply(this);
    }
    protected void behaviorRemoved(Behavior behavior) {
        behavior.remove(this);
    }

    @Override
    public void onHighlight() {

    }

    @Override
    public Collection<? extends StyleSpansFactory<Collection<String>>> getExtraFactories() {
        return Collections.emptyList();
    }

    @Override
    protected boolean alternateIsFocused() {
        return false;
    }

    @Override
    public ObservableList<Behavior> getBehaviors() {
        return behaviors;
    }

    @Override
    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    @Override
    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }
}
