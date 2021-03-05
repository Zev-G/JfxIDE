package tmw.me.com.ide.codeEditor.highlighting;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;

import java.util.*;

public class Highlighter {

    private final IntegratedTextEditor editor;
    private final ArrayList<StyleSpansFactory<Collection<String>>> factories = new ArrayList<>();

    @SafeVarargs
    public Highlighter(IntegratedTextEditor editor, StyleSpansFactory<Collection<String>>... factories) {
        this.editor = editor;
        this.factories.addAll(Arrays.asList(factories));
    }

    public StyleSpans<Collection<String>> createStyleSpans() {
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        ArrayList<SortableStyleSpan<Collection<String>>> allStyleSpans = new ArrayList<>();
        String text = editor.getText();
        for (StyleSpansFactory<Collection<String>> factory : factories) {
            allStyleSpans.addAll(factory.genSpans(text));
        }

        boolean brokeDueToError = false;

        // Sort the list from smallest to largest.
        allStyleSpans.sort(Comparator.comparingInt(SortableStyleSpan::getStart));

//        System.out.println("---Original Style Spans---\n" + allStyleSpans + "\n-----------------------------\n\n");

        int current = 0;
        ArrayList<SortableStyleSpan<Collection<String>>> withinSpans = new ArrayList<>();
        first: while (!allStyleSpans.isEmpty()) {
            SortableStyleSpan<Collection<String>> firstSpan = allStyleSpans.get(0);
            SortableStyleSpan<Collection<String>> secondSpan = null;
            if (allStyleSpans.size() >= 2) {
                secondSpan = allStyleSpans.get(1);
            }
            if (secondSpan == null || (secondSpan.getStart() >= firstSpan.getEnd() && withinSpans.isEmpty())) {
                if (firstSpan.getStart() > current) {
                    builder.add(new StyleSpan<>(stringsInSortableSpans(withinSpans), firstSpan.getStart() - current));
                    current = firstSpan.getStart();
                }
                withinSpans.add(firstSpan);
                builder.add(new StyleSpan<>(stringsInSortableSpans(withinSpans), firstSpan.length()));
                current += firstSpan.length();
                allStyleSpans.remove(firstSpan);
                withinSpans.remove(firstSpan);
//                System.out.println("New current: " + current);
                continue;
            } else {
                int firstStart = firstSpan.getStart();

                ArrayList<SortableStyleSpan<Collection<String>>> tempSortedWithin = new ArrayList<>(withinSpans);
                tempSortedWithin.sort(Comparator.comparingInt(SortableStyleSpan::getEnd));

                if (!tempSortedWithin.isEmpty()) {
                    int min = tempSortedWithin.get(0).getEnd();
                    if (min < firstStart) {
                        // Calculate, add, and then remove all segments which would otherwise be added despite their end being before the firstSpan's start.
                        for (SortableStyleSpan<Collection<String>> span : tempSortedWithin) {
                            int end = span.getEnd();
                            if (end < firstStart) {
                                if (current > end) {
                                    System.err.println("Ran into an issue here (Highlighter.java:68), current is greater than end. Current = [" + current +"] End = [" + end + "]");
                                    brokeDueToError = true;
                                    break first;
                                } else {
                                    builder.add(new StyleSpan<>(stringsInSortableSpans(withinSpans), end - current));
                                    current = end;
                                    withinSpans.remove(span);
                                }
                            }
                        }
                        continue;
                    }
                }



                System.out.println();
                int len;
                if (secondSpan.getStart() >= firstSpan.getEnd()) {
                    len = firstSpan.getEnd() - current;
                } else {
                    len = secondSpan.getStart() - firstStart;
                }

                System.out.println("[" + current + "] First Span: " + firstSpan + " Second Span: " + secondSpan);
                int ogCurrent = current;

                int moved = 0;
                int tempCurrent = firstStart;

                // Remove spans which would extend past the new current.
//                for (SortableStyleSpan<Collection<String>> span : tempSortedWithin) {
//                    int end = span.getEnd();
//                    if (end <= tempCurrent) {
//                        System.out.println("Removing: " + span);
//                        System.out.println("End: [" + end + "] Current: [" + current + "] Moved: [" + moved + "] Result: " + (end - current + moved));
//                        builder.add(new StyleSpan<>(stringsInSortableSpans(withinSpans), end - current + moved));
//                        moved = end - current;
//                        withinSpans.remove(span);
//                    }
//                }
                System.out.println("Before: " + tempSortedWithin + " After: " + withinSpans);

                System.out.println("Current: [" + current + "] len: [" + len + "] Moved: [" + moved + "]");
                current = current + len;

                withinSpans.add(firstSpan);
                if (ogCurrent + moved < firstStart) {
                    int tempResult = firstStart - (ogCurrent + moved);
                    current += tempResult;
                    builder.add(new StyleSpan<>(Collections.emptyList(), tempResult));
                }
                System.out.println("New-current: " + current);
                StyleSpan<Collection<String>> styleSpan = new StyleSpan<>(stringsInSortableSpans(withinSpans), len - moved);
                System.out.println("Adding: " + styleSpan);
                builder.add(styleSpan);
            }

            allStyleSpans.remove(firstSpan);

        }

//        StyleSpans<Collection<String>> result = builder.create();

//        System.out.println("Current: " + current);
//        int realCurrent = 0;
//        for (StyleSpan span : result) {
//            realCurrent += span.getLength();
//        }
//        System.out.println("Real Current: " + realCurrent);

        if (current < text.length() || text.length() == 0)
            builder.add(new StyleSpan<>(brokeDueToError ? Collections.singleton("error-text") : Collections.emptyList(), text.length() - current));

        return builder.create();
    }

    private ArrayList<StyleSpan<Collection<String>>> fromSortableSpans(Collection<SortableStyleSpan<Collection<String>>> sortableSpans) {
        ArrayList<StyleSpan<Collection<String>>> spans = new ArrayList<>();
        for (SortableStyleSpan<Collection<String>> span : sortableSpans) {
            spans.add(span.toStyleSpan());
        }
        return spans;
    }

    private Collection<String> stringsInSortableSpans(Collection<SortableStyleSpan<Collection<String>>> spans) {
        if (spans.isEmpty())
            return Collections.emptyList();
        ArrayList<String> styles = new ArrayList<>();
        for (SortableStyleSpan<Collection<String>> span : spans) {
            styles.addAll(span.getStyle());
        }
        return styles;
    }

    public IntegratedTextEditor getEditor() {
        return editor;
    }

}
