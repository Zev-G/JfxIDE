package tmw.me.com;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tmw.me.com.betterfx.CommandConsole;
import tmw.me.com.ide.Ide;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.interpretation.run.CodeChunk;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.panel.RunPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    private static CodeChunk executeChunk = new CodeChunk();

    @Override
    public void start(Stage primaryStage) {
        // Script Lab
        primaryStage.setScene(new Scene(new Ide(new File("C:\\Users\\Windows\\Desktop\\McMods\\SimpleCodingLanguage\\src\\tmw\\me\\com"))));
        primaryStage.show();
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);


    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void consoleExample() {
        CommandConsole console = new CommandConsole();
        AtomicReference<File> codeFile = new AtomicReference<>(new File("C:\\Users\\Windows\\Desktop\\McMods\\SimpleCodingLanguage"));
        console.setOnUserInput((inputtedString, eventConsole) -> {
            if (!inputtedString.startsWith("/")) {
                String[] lines = inputtedString.split("\\\\n");
                for (String line : lines) {
                    console.addTexts(console.genText("\n&bRunning code: &m" + line));
                    executeChunk.runPiece(SyntaxManager.SYNTAX_MANAGER.genCodePieceFromCode(line, codeFile.get(), 0));
                }
            } else {
                String command = inputtedString.split("/")[1];
                if (command.equals("clear")) {
                    console.getConsoleText().getChildren().clear();
                } else if (command.equals("run")) {
                    executeChunk.run();
                } else if (command.equals("restart")) {
                    FXScript.restart();
                    console.getConsoleText().getChildren().clear();
                } else if (command.startsWith("load")) {
                    command = command.replaceFirst("load ", "");
                    command = command.replaceFirst("load", "");
                    if (command.equals("")) command = Main.class.getResource("test_code.sfs").getFile();
                    codeFile.set(new File(command));
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(codeFile.get());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (scanner != null) {
                        StringBuilder builder = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            String next = scanner.nextLine();
                            builder.append(next);
                            if (scanner.hasNextLine()) {
                                builder.append("\n");
                            }
                        }
                        console.addTexts(console.genText("\n&aCompiling..."));
                        executeChunk = SyntaxManager.SYNTAX_MANAGER.getCodeChunkFromCode(builder.toString(), codeFile.get());
                        Stage runStage = new Stage();
                        runStage.setScene(new Scene(new RunPanel(executeChunk)));
                        runStage.show();
                        console.addTexts(console.genText("\n&bCompiled from file. Running..."));
                        executeChunk.run();
                        console.addTexts(console.genText("\n&mFinished running."));
                    }
                }
            }
        });
    }

}
