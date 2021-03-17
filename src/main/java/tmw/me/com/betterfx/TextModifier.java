package tmw.me.com.betterfx;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public final class TextModifier {

    public static String CODE = "&";

    public static String DEFAULT = CODE + "r";
    public static String FORESTGREEN = CODE + "a";
    public static String AQUAMARINE = CODE + "b";
    public static String ORANGERED = CODE + "c";
    public static String MEDIUMPURPLE = CODE + "d";
    public static String YELLOW = CODE + "e";
    public static String WHITE = CODE + "f";
    public static String DARKCYAN = CODE + "g";
    public static String DARKBLUE = CODE + "h";
    public static String CORNFLOWERBLUE = CODE + "i";
    public static String CADETBLUE = CODE + "j";
    public static String CHOCOLATE = CODE + "k";
    public static String DARKSLATEBLUE = CODE + "l";
    public static String MEDIUMSLATEBLUE = CODE + "m";
    public static String NAVY = CODE + "n";
    public static String ORANGE = CODE + "o";
    public static String PLUM = CODE + "p";
    public static String ANTIQUEWHITE = CODE + "q";
    public static String SKYBLUE = CODE + "s";
    public static String DARKGRAY = CODE + "t";
    public static String DARKRED = CODE + "u";
    public static String DARKGREEN = CODE + "v";
    public static String LIGHTGREEN = CODE + "w";
    public static String LIGHTGREY = CODE + "x";
    public static String RED = CODE + "y";
    public static String AZURE = CODE + "z";

    public static String BLACK = CODE + "0";
    public static String EXTRA_BOLD = CODE + "1";
    public static String BOLD = CODE + "2";
    public static String SEMI_BOLD = CODE + "3";
    public static String MEDIUM = CODE + "4";
    public static String LIGHT = CODE + "5";
    public static String EXTRA_LIGHT = CODE + "6";
    public static String THIN = CODE + "7";
    public static String ITALIC = CODE + "8";
    public static String NORMAL = CODE + "9";
    public static String REGULAR = CODE + "9";

    public static Color colorFromChar(char c) {
        return switch (c) {
            case 'a' -> Color.FORESTGREEN;
            case 'b' -> Color.AQUAMARINE;
            case 'c' -> Color.ORANGERED;
            case 'd' -> Color.MEDIUMPURPLE;
            case 'e' -> Color.YELLOW;
            case 'f' -> Color.WHITE;
            case 'g' -> Color.DARKCYAN;
            case 'h' -> Color.DARKBLUE;
            case 'i' -> Color.CORNFLOWERBLUE;
            case 'j' -> Color.CADETBLUE;
            case 'k' -> Color.CHOCOLATE;
            case 'l' -> Color.DARKSLATEBLUE;
            case 'm' -> Color.MEDIUMSLATEBLUE;
            case 'n' -> Color.NAVY;
            case 'o' -> Color.ORANGE;
            case 'p' -> Color.PLUM;
            case 'q' -> Color.ANTIQUEWHITE;
            case 's' -> Color.SKYBLUE;
            case 't' -> Color.DARKGRAY;
            case 'u' -> Color.DARKRED;
            case 'v' -> Color.DARKGREEN;
            case 'w' -> Color.LIGHTGREEN;
            case 'x' -> Color.LIGHTGREY;
            case 'y' -> Color.RED;
            case 'z' -> Color.AZURE;
            default -> null;
        };
    }

    public static Font fontFromChar(char c, Font font) {
        return switch (c) {
            case '0' -> Font.font(font.getFamily(), FontWeight.BLACK, font.getSize());
            case '1' -> Font.font(font.getFamily(), FontWeight.EXTRA_BOLD, font.getSize());
            case '2', '*', '!' -> Font.font(font.getFamily(), FontWeight.BOLD, font.getSize());
            case '3' -> Font.font(font.getFamily(), FontWeight.SEMI_BOLD, font.getSize());
            case '4' -> Font.font(font.getFamily(), FontWeight.MEDIUM, font.getSize());
            case '5' -> Font.font(font.getFamily(), FontWeight.LIGHT, font.getSize());
            case '6' -> Font.font(font.getFamily(), FontWeight.EXTRA_LIGHT, font.getSize());
            case '7' -> Font.font(font.getFamily(), FontWeight.THIN, font.getSize());
            case '#', '/', '\\', '8' -> Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());
            case '9', '^' -> Font.font(font.getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, font.getSize());
            default -> null;
        };
    }

    public static boolean strikethroughFromChar(char c) {
        return switch (c) {
            case '-', '~', '=' -> true;
            default -> false;
        };
    }

    public static boolean underlineFromChar(char c) {
        return switch (c) {
            case '_', '.' -> true;
            default -> false;
        };
    }

}

