package tmw.me.com.ide.codeEditor.languages;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.function.Supplier;

public interface LanguageSupplier<T extends LanguageSupport> extends Supplier<T> {

    @Override
    T get();

    String getName();

    static <T extends LanguageSupport> LanguageSupplier<T> fromLanguage(T language) {
        if (language instanceof LanguageSupplier)
            return (LanguageSupplier<T>) language;
        return new LanguageSupplier<>() {

            @Override
            public T get() {
                try {
                    return (T) language.getClass().getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public String getName() {
                return language.getLanguageName();
            }
        };
    }

    @SafeVarargs
    static ArrayList<LanguageSupplier<LanguageSupport>> fromLanguages(LanguageSupport... languages) {
        ArrayList<LanguageSupplier<LanguageSupport>> suppliers = new ArrayList<>();
        for (LanguageSupport lang : languages) {
            suppliers.add(fromLanguage(lang));
        }
        return suppliers;
    }

}
