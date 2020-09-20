package sample.ide;

import sample.language.interpretation.SyntaxManager;
import sample.language.interpretation.parse.Parser;
import sample.language.syntaxPiece.SyntaxPieceFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.variable.Variable;

import java.util.ArrayList;

public final class IdeSpecialParser {

//    public static final HashMap<String, ArrayList<PossiblePiecePackage<?>>> CACHE = new HashMap<>();
//    private static boolean CHECKED_CACHE = false;

    public static <T extends SyntaxPieceFactory> ArrayList<PossiblePiecePackage<T>> possibleSyntaxPieces(String code, ArrayList<T> pickFrom) {
//        if (!CHECKED_CACHE) {
//            CHECKED_CACHE = true;
//            Scanner scanner = new Scanner(IdeSpecialParser.class.getResourceAsStream("saved_cache.txt"));
//            ArrayList<SyntaxPieceFactory> syntaxPieceFactories = new ArrayList<>();
//            syntaxPieceFactories.addAll(SyntaxManager.EFFECT_FACTORIES);
//            syntaxPieceFactories.addAll(SyntaxManager.EVENT_FACTORIES);
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                if (line.contains("===")) {
//                    String lineCode = line.split("===")[0];
//                    String usage = line.split("===")[1];
//                    for (SyntaxPieceFactory factory : syntaxPieceFactories) {
//                        if (factory.getUsage().equals(usage)) {
//                            CACHE.put(lineCode, factory);
//                        }
//                    }
//                }
//            }
//        }
//        if (CACHE.get(code) != null) {
//            return (ArrayList<PossiblePiecePackage<T>>) CACHE.get(code);
//        }
        code = code.replaceFirst("^\\s+", "");
//        System.out.println("CODE: " + code);
        ArrayList<PossiblePiecePackage<T>> possiblePiecePackages = new ArrayList<>();
        pieces: for (T syntaxPieceFactory : pickFrom) {
            if (syntaxPieceFactory.getRegex().startsWith("$") || syntaxPieceFactory.getUsage().contains("IGNORE")) {
                continue;
            }
            String codeBuffer = code;
            ArrayList<String> pieces = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            String usage = syntaxPieceFactory.getUsage();
            ArrayList<String> argTypes = new ArrayList<>();
            for (char c : usage.toCharArray()) {
                if (c == '%' && builder.toString().length() >= 1) {
                    if (builder.toString().startsWith("%")) {
                        builder.append(c);
                        pieces.add(builder.toString());
                        argTypes.add(builder.toString());
                        builder = new StringBuilder();
                        continue;
                    } else if (!builder.toString().contains("%")) {
                        pieces.add(builder.toString());
                        builder = new StringBuilder();
                    }
                }
                builder.append(c);
            }
            if (!usage.endsWith("%")) {
                pieces.add(builder.toString());
            }
            if (pieces.isEmpty()) pieces.add(usage);
            int expressionTimes = 0;
            String codeBufferForPiece = "";
            String piece = "";
            String lastExpression = "";
            String remainingText = usage;
            StringBuilder filledIn = new StringBuilder();
            boolean isAnExpression = false;
            boolean lastWasExpression = false;
            for (int i = 0; i < pieces.size() + 1; i++) {
                String lastPiece = piece;
                lastWasExpression = isAnExpression;
                if (codeBuffer.length() == 0) {
                    possiblePiecePackages.add(getPackage(syntaxPieceFactory, pieces, lastWasExpression, lastExpression, lastPiece, filledIn.toString(), code));
                    //System.out.println("Continue (0) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                    continue pieces;
                }
                piece = "";
                if (pieces.size() > i) {
                    piece = pieces.get(i);
                }
                remainingText = remainingText.replaceFirst(piece, "");
                isAnExpression = piece.startsWith("%");
                if (lastWasExpression) {
                    StringBuilder expressionCode = new StringBuilder();
                    String beforeCodeBuffer = codeBuffer;
                    while (!piece.startsWith(codeBuffer)) {
                        expressionCode.append(codeBuffer.charAt(0));
                        codeBuffer = codeBuffer.substring(1);
                        if (codeBuffer.equals("") && i < pieces.size() - 1) {
                            //System.out.println("Continue (1) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ") (Before code buffer: " + beforeCodeBuffer + ")");
                            continue pieces;
                        }
                    }
                    String chosen = (expressionCode.toString().length() > 0 ? expressionCode.toString() : beforeCodeBuffer);
                    lastExpression = chosen;
                    ExpressionFactory<?> expressionFactory = Parser.parseExpression(chosen, null, 0);
                    if (expressionFactory != null) {
                        if (argTypes.size() > expressionTimes && SyntaxManager.SUPPORTED_TYPES.get(argTypes.get(expressionTimes)) != null) {
                            if (!SyntaxManager.SUPPORTED_TYPES.get(argTypes.get(expressionTimes).replaceAll("%", ""))
                                    .isAssignableFrom(expressionFactory.getGenericClass()) &&
                                    !Variable.class.isAssignableFrom(expressionFactory.getGenericClass())
                                    && expressionFactory.getGenericClass() != Object.class) {
//                                System.out.println("Continue (2) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                                continue pieces;
                            }
                        } else if (piece.length() == 0) {
                            possiblePiecePackages.add(getPackage(syntaxPieceFactory, pieces, true, lastExpression, lastPiece, filledIn.toString(), code));
//                            System.out.println("Continue (3) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                            continue pieces;
                        }
                    } else {
//                        System.out.println("Continue (4) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                        continue pieces;
                    }
                    expressionTimes++;
                }
                if (!isAnExpression) {
                    if (piece.startsWith(codeBuffer)) {
                        codeBufferForPiece = codeBuffer;
                        codeBuffer = codeBuffer.replaceFirst(codeBuffer, "");
                        filledIn.append(codeBufferForPiece);
                        if (pieces.size() - 1 == i) {
                            possiblePiecePackages.add(getPackage(syntaxPieceFactory, pieces, lastWasExpression, lastExpression, lastPiece, filledIn.toString(), code));
                            //System.out.println("Continue (5) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                            continue pieces;
                        }
                        //System.out.println("CodeBuffer at this exact moment: " + codeBuffer + "(i: " + i + ")" + " (factory: " + syntaxPieceFactory.getUsage() + ")");
                    } else {
                        if (codeBuffer.startsWith(piece)) {
                            codeBuffer = codeBuffer.replaceFirst(piece, "");
                            filledIn.append(piece);
                        } else {
                            //System.out.println("Continue (6) " + usage + " (piece: " + piece + ") (code buffer: " + codeBuffer + ")");
                            continue pieces;
                        }
                    }
                } else {
                    filledIn.append(piece);
                }
            }
            //System.out.println("Down here");
            Class<?> aClass = lastWasExpression ? SyntaxManager.SUPPORTED_TYPES.get(piece.replaceAll("%", "")) : null;
            possiblePiecePackages.add(new PossiblePiecePackage<>(syntaxPieceFactory, aClass,
                    codeBufferForPiece + String.join("", pieces), remainingText, lastWasExpression ? lastExpression : "", pieces));

        }
        return possiblePiecePackages;
    }

    private static <T extends SyntaxPieceFactory> PossiblePiecePackage<T> getPackage(T syntaxPieceFactory, ArrayList<String> pieces, boolean lastWasExpression, String lastExpression, String lastPiece, String filledIn, String code) {
        Class<?> aClass = lastWasExpression ? SyntaxManager.SUPPORTED_TYPES.get(lastPiece.replaceAll("%", "")) : null;
        String notFilled = syntaxPieceFactory.getUsage().substring(filledIn.length());

//        System.out.println("Code Buffer: " + codeBuffer + " Filled In: " + filledIn);

        return new PossiblePiecePackage<>(syntaxPieceFactory, aClass, filledIn,
                notFilled,
                lastWasExpression ? lastExpression : "", pieces);
    }

    public static class PossiblePiecePackage<T extends SyntaxPieceFactory> {

        private final T syntaxPieceFactory;
        private Class<?> tryingToGet;
        private String filledIn;
        private String notFilledIn;
        private String expressionText;
        private ArrayList<String> pieces;

        public PossiblePiecePackage(T syntaxPieceFactory, Class<?> tryingToGet, String filledIn, String notFilledIn, String expressionText, ArrayList<String> pieces) {
            this.syntaxPieceFactory = syntaxPieceFactory;
            this.tryingToGet = tryingToGet;
            this.filledIn = filledIn;
            this.notFilledIn = notFilledIn;
            this.expressionText = expressionText;
            this.pieces = pieces;
        }
        public PossiblePiecePackage(T syntaxPieceFactory, Class<?> tryingToGet, String filledIn, String notFilledIn, String expressionText) {
            this.syntaxPieceFactory = syntaxPieceFactory;
            this.tryingToGet = tryingToGet;
            this.filledIn = filledIn;
            this.notFilledIn = notFilledIn;
            this.expressionText = expressionText;
        }
        public PossiblePiecePackage(T syntaxPieceFactory, Class<?> tryingToGet, String filledIn, String notFilledIn) {
            this(syntaxPieceFactory, tryingToGet, filledIn, notFilledIn, null);
        }
        public PossiblePiecePackage(T syntaxPieceFactory, String filledIn, String notFilledIn) {
            this(syntaxPieceFactory, null, filledIn, notFilledIn, null);
        }
        public PossiblePiecePackage(T syntaxPieceFactory, String filledIn) {
            this(syntaxPieceFactory, null, filledIn, null, null);
        }
        public PossiblePiecePackage(T syntaxPieceFactory) {
            this(syntaxPieceFactory, null, null, null, null);
        }

        public T getSyntaxPieceFactory() {
            return syntaxPieceFactory;
        }
        public Class<?> getTryingToGet() {
            return tryingToGet;
        }
        public String getFilledIn() {
            return filledIn;
        }
        public String getNotFilledIn() {
            return notFilledIn;
        }
        public String getExpressionText() {
            return expressionText;
        }
        public ArrayList<String> getPieces() {
            return pieces;
        }

        public void setTryingToGet(Class<?> tryingToGet) {
            this.tryingToGet = tryingToGet;
        }
        public void setFilledIn(String filledIn) {
            this.filledIn = filledIn;
        }
        public void setNotFilledIn(String notFilledIn) {
            this.notFilledIn = notFilledIn;
        }
        public void setExpressionText(String expressionText) {
            this.expressionText = expressionText;
        }
        public void setPieces(ArrayList<String> pieces) {
            this.pieces = pieces;
        }

        @Override
        public String toString() {
            return "PossiblePiecePackage{" +
                    "syntaxPieceFactory=" + syntaxPieceFactory.getRegex() +
                    ", tryingToGet=" + tryingToGet +
                    ", filledIn='" + filledIn + '\'' +
                    ", notFilledIn='" + notFilledIn + '\'' +
                    ", expressionText='" + expressionText + '\'' +
                    '}';
        }
    }

}
