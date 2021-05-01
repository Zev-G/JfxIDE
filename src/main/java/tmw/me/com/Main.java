package tmw.me.com;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tmw.me.com.ide.Ide;
import tmw.me.com.ide.codeEditor.fonts.Fonts;
import tmw.me.com.ide.settings.IdeSettings;
import tmw.me.com.jfxhelper.NodeUtils;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        startProgram();

        Ide ide = new Ide();
        primaryStage.setScene(new Scene(NodeUtils.wrapNode(ide)));
        primaryStage.show();
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

    }

    public static void startProgram() throws IOException {
        IdeSettings.start();
        Font.loadFont(Fonts.ttf("JetBrainsMono-Bold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-BoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraBold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraBoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraLight.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraLightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Italic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Light.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-LightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Medium.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-MediumItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Regular.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Thin.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ThinItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("Montserrat-Regular.ttf"), 10);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
