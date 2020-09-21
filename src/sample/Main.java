package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.betterfx.CommandConsole;
import sample.ide.Ide;
import sample.panel.RunPanel;
import sample.language.FXScript;
import sample.language.interpretation.SyntaxManager;
import sample.language.interpretation.run.CodeChunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    private static CodeChunk executeChunk = new CodeChunk();

    @Override
    public void start(Stage primaryStage) {



        SyntaxManager.init();


        CommandConsole console = new CommandConsole();
        SyntaxManager.setPrintConsole(console);
//        primaryStage.setScene(new Scene(console));
        primaryStage.setScene(new Scene(new Ide(new File("D:\\Users\\Windows\\Desktop\\FileTreeIssueFinding"))));
        primaryStage.show();
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        AtomicReference<File> codeFile = new AtomicReference<>(new File(Main.class.getResource("test_code.sfs").getFile()));
        console.setOnUserInput((inputtedString, eventConsole) -> {
            if (!inputtedString.startsWith("/")) {
                String[] lines = inputtedString.split("\\\\n");
                for (String line : lines) {
                    console.addTexts(console.genText("\n&bRunning code: &m" + line));
                    executeChunk.runPiece(SyntaxManager.genCodePieceFromCode(line, codeFile.get(), 0));
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
                        executeChunk = SyntaxManager.getCodeChunkFromCode(builder.toString(), codeFile.get());
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
