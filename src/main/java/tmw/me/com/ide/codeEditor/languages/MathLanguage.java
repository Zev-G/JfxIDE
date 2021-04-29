package tmw.me.com.ide.codeEditor.languages;

import tmw.me.com.ide.IdeSpecialParser;
import tmw.me.com.ide.codeEditor.Behavior;
import tmw.me.com.ide.codeEditor.texteditor.BehavioralLanguageEditor;
import tmw.me.com.ide.codeEditor.texteditor.IntegratedTextEditor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathLanguage extends LanguageSupport {


    public MathLanguage() {
        super(Styles.forName("math"), "Math");
        usingAutoComplete = true;
    }

    public static final Pattern PATTERN = Pattern.compile("(?<NUMBER>" + NUMBER_PATTERN + ")");

    @Override
    public Pattern generatePattern() {
        return PATTERN;
    }

    @Override
    public String styleClass(Matcher matcher) {
        return matcher.group("NUMBER") != null ? "number" : null;
    }

    @Override
    public Behavior[] addBehaviour(BehavioralLanguageEditor integratedTextEditor) {
        return null;
    }

    @Override
    public ArrayList<IdeSpecialParser.PossiblePiecePackage> getPossiblePieces(String line, IntegratedTextEditor editor) {
        ArrayList<IdeSpecialParser.PossiblePiecePackage> possiblePiecePackages = new ArrayList<>();
        if (line.trim().endsWith("=")) {
            String notPutIn = String.valueOf(eval(line.trim().substring(0, line.trim().length() - 1)));
            possiblePiecePackages.add(new IdeSpecialParser.PossiblePiecePackage(
                    "", notPutIn
                    , notPutIn, true).setReplaceLine(false));
        }
        return possiblePiecePackages;
    }

    /**
     * @param str The math equation which will be evaluated.
     * @return The answer to the equation.
     * @author Boann from StackOverFlow. Question #3422673
     */
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    x = switch (func) {
                        case "sqrt" -> Math.sqrt(x);
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        default -> throw new RuntimeException("Unknown function: " + func);
                    };
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

}
