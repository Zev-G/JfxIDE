package tmw.me.com.ide.documentation;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tmw.me.com.language.syntaxPiece.SyntaxPieceFactory;

public class DocumentationItem<T extends SyntaxPieceFactory> extends VBox {

    private final Label title = new Label();
    private final HBox tags = new HBox();
    private final BorderPane titleBox = new BorderPane();
    private final VBox codeBox = new VBox();
    private final HBox examplesBox = new HBox();

    public DocumentationItem() {

    }

    public void fromSyntax(T value) {
        title.setText(value.getUsage());
        Label codeLabel = new Label();
        codeBox.getChildren().add(codeLabel);
    }

}
