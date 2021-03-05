package tmw.me.com.ide.codeEditor.languages;

import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import javafx.application.Platform;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.components.Behavior;
import tmw.me.com.ide.codeEditor.languages.styles.LanguageStyles;
import tmw.me.com.ide.tools.concurrent.schedulers.ChangeListenerScheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkriptLanguage extends LanguageSupport {

    private final Behavior[] behaviors = new Behavior[] {new SkriptParserBehavior()};

    public SkriptLanguage() {
        super(LanguageStyles.get("skript"), "Skript");
    }

    @Override
    public Pattern generatePattern() {
        return null;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return null;
    }

    @Override
    public Behavior[] addBehaviour(IntegratedTextEditor integratedTextEditor) {
        return behaviors;
    }

    @Override
    public Behavior[] removeBehaviour(IntegratedTextEditor integratedTextEditor) {
        return behaviors;
    }

    private static class SkriptParserBehavior extends Behavior {

        private ChangeListenerScheduler<String> textListener;

        @Override
        public void apply(IntegratedTextEditor integratedTextEditor) {
            textListener = new ChangeListenerScheduler<>(200, false, (observableValue, s, t1) -> {
                try {
                    File tempFile = File.createTempFile("sk lang", "sk");
                    FileWriter writer = new FileWriter(tempFile);
                    writer.write(integratedTextEditor.getText());
                    writer.close();
                    List<LogEntry> entries = ScriptLoader.loadScript(tempFile, true);
                    ArrayList<Integer> errors = new ArrayList<>();
                    System.out.println(entries);
                    for (LogEntry entry : entries) {
                        System.out.println(entry.getMessage());
                        int lineNum = lineNumFromLogEntry(entry);
                        errors.add(lineNum);
                    }
                    Platform.runLater(() -> integratedTextEditor.getErrorLines().setAll(errors));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            integratedTextEditor.textProperty().addListener(textListener);
        }

        @Override
        public void remove(IntegratedTextEditor integratedTextEditor) {
            integratedTextEditor.textProperty().removeListener(textListener);
        }

        private int lineNumFromLogEntry(LogEntry entry) {

            String[] after = entry.getMessage().split("line ");
            for (String piece : after) {
                String[] spiltAtColon = piece.split(":");
                if (spiltAtColon.length >= 1) {
                    String beforeColo = spiltAtColon[0];
                    try {
                        return Integer.parseInt(beforeColo);
                    } catch (NumberFormatException ignored) {

                    }
                }
            }

            return -1;

        }

    }

}
