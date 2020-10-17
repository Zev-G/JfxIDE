package tmw.me.com.javaPlayground;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.JavaLanguage;
import tmw.me.com.ide.codeEditor.languages.LanguageLibrary;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class should be it's own project but isn't right now since it will use tons of utilities from the IDE project.
 */
public class JavaPlayground extends AnchorPane {

    private final IntegratedTextEditor quickCodeRunner = new IntegratedTextEditor(new JavaLanguage());
    private final Button runCode = new Button("Run Code");
    private final HBox codeButtonsHolder = new HBox(runCode);
    private final VBox codeBox = new VBox(quickCodeRunner.getTextAreaHolder(), codeButtonsHolder);

    public JavaPlayground() {
        this.getStylesheets().add(Ide.STYLE_SHEET);
        this.getChildren().add(codeBox);
        quickCodeRunner.setPrefSize(450, 250);
        runCode.setOnAction(actionEvent -> {
            try {
                runQuickCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void runQuickCode() throws Exception {

        StringBuilder code = new StringBuilder("package test;\n\nclass QuickRunner{\n\t" +
                "public static void main(String[] args) {");
        for (String line : quickCodeRunner.getText().split("\n")) {
            System.out.println("Line: " + line);
            code.append("\n\t\t").append(line.startsWith("\n") ? line.substring(1) : line);
        }
        code.append("\n\t}\n}");

        CompileString.run(code.toString(), "QuickRunner");

    }

    public static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        public JavaSourceFromString( String name, String code) {
            super( URI.create("string:///" + name.replace('.','/')
                    + Kind.SOURCE.extension),Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

}
