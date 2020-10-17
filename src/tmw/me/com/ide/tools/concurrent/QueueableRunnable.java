package tmw.me.com.ide.tools.concurrent;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.IntegratedTextEditor;
import tmw.me.com.ide.codeEditor.languages.SfsLanguage;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.interpretation.run.CodeChunk;

import java.util.ArrayList;

public class QueueableRunnable implements Runnable {

    private volatile ArrayList<Runnable> futureRunnables = new ArrayList<>();
    private volatile boolean locked = false;
    private volatile boolean finalized = false;

    public QueueableRunnable(Runnable defaultRunnable) {
        futureRunnables.add(defaultRunnable);
    }
    public QueueableRunnable() {}

    @Override
    public void run() {
        while (!finalized) {
            if (!locked && futureRunnables != null && !futureRunnables.isEmpty()) {
                futureRunnables.get(0).run();
                futureRunnables.remove(0);
            }
        }
    }

    public ArrayList<Runnable> getFutureRunnables() {
        return futureRunnables;
    }

    public void addFutureRunnable(Runnable runnable) {
        futureRunnables.add(runnable);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setFutureRunnables(ArrayList<Runnable> futureRunnables) {
        this.futureRunnables = futureRunnables;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public static void main(String[] args) {

        Stage primaryStage = new Stage();
        QueueableRunnable queueableRunnable = new QueueableRunnable();
        new Thread(queueableRunnable).start();
        Button triggerThread = new Button("Trigger");
        IntegratedTextEditor integratedTextEditor = new IntegratedTextEditor(new SfsLanguage());
        integratedTextEditor.getTextAreaHolder().setMinHeight(750);
        VBox vBox = new VBox(triggerThread, integratedTextEditor.getTextAreaHolder());
        vBox.getStylesheets().add(Ide.STYLE_SHEET);
        triggerThread.setOnAction(actionEvent -> {
            queueableRunnable.addFutureRunnable(() -> {
                CodeChunk codeChunk = FXScript.PARSER.parseChunk(integratedTextEditor.getText(), null);
                codeChunk.run();
            });
        });
        primaryStage.setScene(new Scene(vBox));
        primaryStage.show();
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

    }

}
