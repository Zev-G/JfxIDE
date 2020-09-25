package tmw.me.com.language.tools;

import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;

/**
 * This entire class isn't currently functional.
 */
public final class JSONTools {

    public static <T> String expressionFactoryToJSON(ExpressionFactory<T> expressionFactory, String autocompleteAfter, String descriptor, String iconName) {
        String json = "{\n";
        String displayText = "\t\"displayText\": \"" + autocompleteAfter + "\",\n";
        String snippet = "\t\"snippet\": \"" + expressionFactory.getRegex() + "\",\n";
        String description = "\t\"description\": \"" + descriptor + "\",\n";
        String icon = "\t\"icon\": \"<i class=\\\"icon-" + iconName + "\\\"></i>\"\n";
        return json + displayText + snippet + description + icon + "}";
    }

    /**
     * Not finished
     */
    public static <T> String expressionFactoryToCSON(ExpressionFactory<T> expressionFactory) {
        for (String piece : expressionFactory.getRegex().split("%(.*?)%")) {
            piece = piece.trim();
        }
        return "";
    }

    private static String indentX(int x) {
        return "\t".repeat(Math.max(0, x));
    }

}
