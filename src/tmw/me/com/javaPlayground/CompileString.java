package tmw.me.com.javaPlayground;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompileString {

    public static void run(String program, String className) throws Exception {
        System.out.println(program);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        Iterable<? extends JavaFileObject> fileObjects;
        fileObjects = getJavaSourceFromString(program);

        System.out.println(compiler.getTask(null, null, null, null, null, fileObjects).call());

        Class<?> clazz = Class.forName("test." + className);
        Method m = clazz.getMethod("main", String[].class);
        Object[] _args = new Object[]{new String[0]};
        m.invoke(null, _args);
    }

    static Iterable<JavaSourceFromString> getJavaSourceFromString(String code) {
        final JavaSourceFromString jsfs;
        jsfs = new JavaSourceFromString("code", code);
        return () -> new Iterator<>() {
            boolean isNext = true;

            public boolean hasNext() {
                return isNext;
            }

            public JavaSourceFromString next() {
                if (!isNext)
                    throw new NoSuchElementException();
                isNext = false;
                return jsfs;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
