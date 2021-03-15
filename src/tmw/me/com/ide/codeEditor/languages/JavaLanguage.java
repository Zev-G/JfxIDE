package tmw.me.com.ide.codeEditor.languages;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The (very minimal) file support for Java. This is just highlighting, see {@link LanguageSupport} for information on the methods used.
 */
public class JavaLanguage extends LanguageSupport {

    private Text currentErrorText = null;
    private Text currentPrintText = null;

    private static final String[] KEYWORDS = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "String", "Math"};

    private static final String KEYWORDS_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String PAREN_PATTERN = "((|[A-z]*)\\()|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String VARIABLE_CALL_PATTERN = "\\b([a-z][A-z]+?\\.)\\b";
    private static final String CLASS_PATTERN = "\\b([A-Z][A-z]+)\\b";

    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<COMMENT>" + COMMENT_PATTERN + ")" +
            "|(?<STRING>" + STRING_PATTERN + ")" +
            "|(?<NUMBER>" + NUMBER_PATTERN + ")" +
            "|(?<KEYWORD>" + KEYWORDS_PATTERN + ")" +
            "|(?<CLASS>" + CLASS_PATTERN + ")" +
            "|(?<VARCALL>" + VARIABLE_CALL_PATTERN + ")" +
            "|(?<PAREN>" + PAREN_PATTERN + ")" +
            "|(?<BRACKET>" + BRACKET_PATTERN + ")" +
            "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
            "|(?<BRACE>" + BRACE_PATTERN + ")" +
            "");

    public JavaLanguage() {
        super(JavaLanguage.class.getResource("styles/java.css").toExternalForm(), "Java");
        runnable = true;
    }

    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return
                matcher.group("COMMENT") != null ? "comment" :
                        matcher.group("STRING") != null ? "string" :
                                matcher.group("NUMBER") != null ? "number" :
                                        matcher.group("KEYWORD") != null ? "keyword" :
                                                matcher.group("CLASS") != null ? "class" :
                                                        matcher.group("VARCALL") != null ? "var-call" :
                                                                matcher.group("PAREN") != null ? "paren" :
                                                                        matcher.group("BRACKET") != null ? "bracket" :
                                                                                matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                                        matcher.group("BRACE") != null ? "brace" :
                                                                                                null;
    }

    @Override
    public Behavior[] addBehaviour(IntegratedTextEditor integratedTextEditor) {
        return null;
    }

    @Override
    public void run(IntegratedTextEditor textEditor, Ide ide) {
        super.run(textEditor, ide);

        String source = textEditor.getText();

        // Save source in .java file.
        File root = new File("C:\\java\\");
        root.mkdir();
        File sourceFile = new File(root, "\\Test.java");

        try {
            Writer writer = new FileWriter(sourceFile);
            writer.write(source);
            writer.close();

            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            OutputStream errorStream = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    if (currentErrorText == null) {
                        currentErrorText = ide.getRunConsole().getDefaultText(String.valueOf((char) b), Color.RED);
                        ide.getRunConsole().addText(currentErrorText, false);
                    } else {
                        currentErrorText.setText(currentErrorText.getText() + (char) b);
                    }
                    currentPrintText = null;
                }

            };
            OutputStream printStream = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    if (currentPrintText == null) {
                        currentPrintText = ide.getRunConsole().getDefaultText(String.valueOf((char) b));
                        ide.getRunConsole().addText(currentPrintText, false);
                    } else {
                        currentPrintText.setText(currentPrintText.getText() + (char) b);
                    }
                    currentErrorText = null;
                }

            };
            compiler.run(null, printStream, errorStream, sourceFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

}
