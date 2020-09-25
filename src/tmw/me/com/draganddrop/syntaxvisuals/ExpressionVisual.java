package tmw.me.com.draganddrop.syntaxvisuals;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;

import java.util.ArrayList;

public class ExpressionVisual<T> extends SyntaxVisual<ExpressionFactory<?>> {

    private final Class<T> tClass;
    private static final Color BG_COLOR = Color.valueOf("#f7ab2f");

    private double startX;
    private double startY;

    private final Text defaultText = new Text();
    private final Polygon dropDown = new Polygon(0, 0, 15, 0, 7.5, 15, 0, 0);

    public ExpressionVisual(Class<T> tClass) {
        this.tClass = tClass;
        this.setAlignment(Pos.CENTER);
        this.setCursor(Cursor.DEFAULT);
        dropDown.setCursor(Cursor.HAND);
        dropDown.setOnMousePressed(mouseEvent -> {
            Popup popup = new Popup();

        });
        defaultText.setText(tClass.getSimpleName());
        this.setPadding(new Insets(2, 5, 2, 5));
        this.setSpacing(5);
        this.getChildren().addAll(defaultText, dropDown);
        this.setBackground(new Background(new BackgroundFill(BG_COLOR, new CornerRadii(30), Insets.EMPTY)));
    }

    public ArrayList<ExpressionFactory<?>> genExpressionFactories() {
        ArrayList<Class<?>> supportedClasses = new ArrayList<>();
        supportedClasses.add(Object.class);
        for (Class<?> loopClass : SyntaxManager.SUPPORTED_TYPES.values()) {
            if (loopClass.isAssignableFrom(tClass)) {
                supportedClasses.add(loopClass);
            }
        }
        ArrayList<ExpressionFactory<?>> expressionFactories = new ArrayList<>();
        supportedClasses.forEach(aClass -> {
            expressionFactories.addAll(SyntaxManager.HIGHEST.get(aClass));
            expressionFactories.addAll(SyntaxManager.HIGH.get(aClass));
            expressionFactories.addAll(SyntaxManager.MEDIUM.get(aClass));
            expressionFactories.addAll(SyntaxManager.LOW.get(aClass));
            expressionFactories.addAll(SyntaxManager.LOWEST.get(aClass));
        });
        return expressionFactories;
    }

    public void setNodes(ExpressionFactory<?> expressionFactory) { setNodes(expressionFactory.getRegex()); }
    public void setNodes(String regex) {
        ArrayList<String> typePieces = FXScript.PARSER.generateExpressionPiecesFromString(regex);
        for (String typePiece : typePieces) {
            boolean isExpression = typePiece.startsWith("%");
            if (isExpression) {
                getChildren().add(new ExpressionVisual<>(SyntaxManager.SUPPORTED_TYPES.get(typePiece.replaceAll("%", ""))));
            } else {
                getChildren().add(defaultText(typePiece));
            }
        }
    }

    public Text defaultText(String text) {
        Text returnText = new Text(text);
        returnText.setFont(new Font(18));
        return returnText;
    }

}
