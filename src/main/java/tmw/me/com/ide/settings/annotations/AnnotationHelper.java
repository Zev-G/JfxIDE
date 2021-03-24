package tmw.me.com.ide.settings.annotations;

import java.lang.annotation.Annotation;

public final class AnnotationHelper {

    public static DisplayedJSON createDisplayableJSON(
            boolean useTitle, String title, boolean editable, int fontSize, int enforcePosition, boolean bold,
            DisplayedJSON.EditMethod editMethod, String[] additionalStyleClasses
    ) {
        return new DisplayedJSON() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplayedJSON.class;
            }

            @Override
            public boolean useTitle() {
                return useTitle;
            }

            @Override
            public String title() {
                return title;
            }

            @Override
            public boolean editable() {
                return editable;
            }

            @Override
            public int fontSize() {
                return fontSize;
            }

            @Override
            public int enforcePosition() {
                return enforcePosition;
            }

            @Override
            public boolean bold() {
                return bold;
            }

            @Override
            public EditMethod editMethod() {
                return editMethod;
            }

            @Override
            public String[] additionalStyleClasses() {
                return additionalStyleClasses;
            }
        };
    }

}
