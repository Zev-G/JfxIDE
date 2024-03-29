package tmw.me.com.draganddrop;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tmw.me.com.draganddrop.syntaxvisuals.ExpressionVisual;
import tmw.me.com.language.FXScript;
import tmw.me.com.language.syntax.SyntaxManager;
import tmw.me.com.language.syntaxPiece.effects.EffectFactory;

public class CodeLineVisual extends HBox {

    private final EffectFactory effect;

    private double startX;
    private double startY;

    public CodeLineVisual(EffectFactory effect) {
        this.effect = effect;
        this.setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(10), Insets.EMPTY)));
        this.setMinHeight(30);
        this.setMinWidth(130);
        this.setPadding(new Insets(5, 10, 5, 10));
        this.setSpacing(10);
        this.setCursor(Cursor.OPEN_HAND);

        this.setOnMousePressed(mouseEvent -> {
            startX = mouseEvent.getSceneX() - this.getLayoutX();
            startY = mouseEvent.getSceneY() - this.getLayoutY();
        });
        this.setOnMouseDragged(mouseEvent -> {
            this.setLayoutX(mouseEvent.getSceneX() - startX);
            this.setLayoutY(mouseEvent.getSceneY() - startY);
            this.setCursor(Cursor.CLOSED_HAND);
        });
        this.setOnMouseReleased(mouseEvent -> this.setCursor(Cursor.OPEN_HAND));
        setNodesForEffect();
    }

    public void setNodesForEffect() {
        setNodesForEffect(effect);
    }

    public void setNodesForEffect(EffectFactory effect) {
        setNodesForEffect(effect.getRegex());
    }

    public void setNodesForEffect(String code) {
        for (String typePiece : FXScript.PARSER.generateExpressionPiecesFromString(code)) {
            boolean isExpression = typePiece.startsWith("%");
            if (isExpression) {
                getChildren().add(new ExpressionVisual<>(SyntaxManager.SYNTAX_MANAGER.SUPPORTED_TYPES.get(typePiece.replaceAll("%", ""))));
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
