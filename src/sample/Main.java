package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.betterfx.CommandConsole;
import sample.panel.RunPanel;
import sample.test.FXScript;
import sample.test.interpretation.Interpreter;
import sample.test.interpretation.run.CodeChunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends Application {

    private static final String PATH = "D:\\Users\\Windows\\Documents\\test_code.sfs";
    private static CodeChunk executeChunk = new CodeChunk();

    @Override
    public void start(Stage primaryStage) {

        Interpreter.init();

//        CommandConsole console = CommandConsole.generateForJava();

        CodeChunk chunk = Interpreter.getCodeChunkFromCode(
                "print \"example\"\n" +
                        "print new stage"
        );
        chunk.run();


//CommandConsole.generateForJava();
        CommandConsole console = new CommandConsole();
        Interpreter.setPrintConsole(console);
        primaryStage.setScene(new Scene(console));
        primaryStage.show();
        console.setOnUserInput((inputtedString, eventConsole) -> {
            if (!inputtedString.startsWith("/")) {
                String[] lines = inputtedString.split("\\\\n");
                for (String line : lines) {
                    console.addTexts(console.genText("\n&bRunning code: &m" + line));
                    executeChunk.runPiece(Interpreter.genCodePieceFromCode(line));
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
                    if (command.equals("")) command = PATH;
                    File codeFile = new File(command);
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(codeFile);
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
                        executeChunk = Interpreter.getCodeChunkFromCode(builder.toString());
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


    public static void main(String[] args) {
        launch(args);
    }
}
