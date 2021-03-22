package tmw.me.com.ide.settings.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface DisplayedJSON {

    boolean useTitle() default false;
    String title() default "none";

    enum EditMethod { TEXT, FILE, FOLDER, INTEGER, NUMBER, BOOLEAN, FULL_OBJECT;

        public boolean isTextMethod() {
            return this == TEXT || this == FILE || this == FOLDER || this == INTEGER || this == NUMBER;
        }

        public boolean isFileMethod() {
            return this == FOLDER || this == FILE;
        }

        public boolean isNumberMethod() {
            return this == NUMBER || this == INTEGER;
        }

    }

    boolean editable() default true;
    int fontSize() default 14;
    int enforcePosition() default Integer.MAX_VALUE;
    boolean bold() default false;
    EditMethod editMethod() default EditMethod.TEXT;
    String[] additionalStyleClasses() default {  };

}
