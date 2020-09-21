package sample.ide;

import sample.language.interpretation.SyntaxManager;
import sample.language.interpretation.parse.Parser;
import sample.language.syntaxPiece.SyntaxPieceFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.variable.Variable;

import java.util.ArrayList;

public final class IdeSpecialParser {


    public static <T extends SyntaxPieceFactory> ArrayList<PossiblePiecePackage> possibleSyntaxPieces(String code, ArrayList<T> pickFrom) {
        code = code.replaceFirst("^\\s+", "");
//        System.out.println("CODE: " + code);
        ArrayList<PossiblePiecePackage> possiblePiecePackages = new ArrayList<>();
        pieces: for (T syntaxPieceFactory : pickFrom) {
            if (syntaxPieceFactory.getUsage().contains("IGNORE")) {
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
            String piece;
            String remainingText = usage;
            StringBuilder filledIn = new StringBuilder();
            boolean isAnExpression = false;
            boolean lastWasExpression;
            for (int i = 0; i < pieces.size() + 1; i++) {
                lastWasExpression = isAnExpression;
                if (codeBuffer.length() == 0) {
                    possiblePiecePackages.add(getPackage(syntaxPieceFactory, filledIn.toString()));
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
                            possiblePiecePackages.add(getPackage(syntaxPieceFactory, filledIn.toString()));
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
                            possiblePiecePackages.add(getPackage(syntaxPieceFactory, filledIn.toString()));
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
            possiblePiecePackages.add(new PossiblePiecePackage(codeBufferForPiece + String.join("", pieces), remainingText));

        }
        return possiblePiecePackages;
    }

    private static <T extends SyntaxPieceFactory> PossiblePiecePackage getPackage(T syntaxPieceFactory, String filledIn) {
        return new PossiblePiecePackage(filledIn,
                syntaxPieceFactory.getUsage().substring(filledIn.length()));
    }

    public static class PossiblePiecePackage {

        private final String filledIn;
        private final String notFilledIn;
        private final String putIn;

        private boolean putInIfNotFilledInIsEmpty = false;

        public PossiblePiecePackage(String filledIn, String notFilledIn) {
            this.filledIn = filledIn;
            this.notFilledIn = notFilledIn;
            this.putIn = filledIn + notFilledIn;
        }
        public PossiblePiecePackage(String filledIn, String notFilledIn, String putIn) {
            this.filledIn = filledIn;
            this.notFilledIn = notFilledIn;
            this.putIn = putIn;
        }
        public PossiblePiecePackage(String filledIn, String notFilledIn, String putIn, boolean putInIfFilledIn) {
            this.filledIn = filledIn;
            this.notFilledIn = notFilledIn;
            this.putIn = putIn;
            this.putInIfNotFilledInIsEmpty = putInIfFilledIn;
        }

        public String getFilledIn() {
            return filledIn;
        }
        public String getNotFilledIn() {
            return notFilledIn;
        }
        public String getPutIn() {
            return (notFilledIn.length() > 0 || putInIfNotFilledInIsEmpty) ? putIn : "";
        }

        public boolean putInIfFilledIn() {
            return putInIfNotFilledInIsEmpty;
        }
        public void setPutInIfFilledIn(boolean putInIfFilledIn) {
            putInIfNotFilledInIsEmpty = putInIfFilledIn;
        }
    }

}
