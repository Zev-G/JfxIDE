package sample.test.interpretation.parse;

import sample.test.interpretation.parse.error.ParseError;
import sample.test.syntaxPiece.SyntaxPiece;
import sample.test.syntaxPiece.SyntaxPieceFactory;
import sample.test.syntaxPiece.events.WhenEventFactory;
import sample.test.syntaxPiece.events.statements.ElseStatement;
import sample.test.syntaxPiece.events.Event;
import sample.test.interpretation.run.CodeChunk;
import sample.test.interpretation.run.CodePiece;
import sample.test.interpretation.SyntaxManager;
import sample.test.syntaxPiece.effects.Effect;
import sample.test.syntaxPiece.events.Function;
import sample.test.syntaxPiece.events.statements.IfStatement;
import sample.test.syntaxPiece.expressions.Expression;
import sample.test.syntaxPiece.expressions.ExpressionFactory;
import sample.test.variable.Variable;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public final class Parser {

    public static boolean PRINTING_CHUNK_PARSING_INFO = false;

    /**
     *
     * @param code The code from which the CodeChunk is parsed.
     * @return A parsed CodeChunk that is ready to be ran.
     */
    public static CodeChunk parseChunk(String code, File file) { return parseChunk(code, null, file); }
    public static CodeChunk parseChunk(String code, CodeChunk parent, File file) {
        CodeChunk chunk = new CodeChunk();
        chunk.setCode("Top");
        boolean inEvent = false;
        Event lastEvent = null;
        StringBuilder eventCode = new StringBuilder();
        String[] pieces = code.split("\n");
        String lastEventCode = "";
        for (int i = 0; i < pieces.length + 1; i++) {
            String line = "";
            if (pieces.length > i) {
                line = pieces[i];
                line = line.replaceFirst(" {2}", "\t");
            }
            if (PRINTING_CHUNK_PARSING_INFO) System.out.println("On number: " + i + " text: " + line);
            if (!line.replaceAll("\\s", "").startsWith("#") && !line.equals("")) {
                CodePiece addPiece = null;
                if (!line.startsWith("\t") && !line.endsWith(":")) {
                    addPiece = SyntaxManager.genCodePieceFromCode(line.trim(), file, i + 1);
                    addPiece.setCodeChunk(parent);
                }
                if (line.startsWith("\t")) {
                    inEvent = true;
                    eventCode.append(line.replaceFirst("\t", "")).append("\n");
                } else {
                    if (PRINTING_CHUNK_PARSING_INFO) System.out.println("Line: " + line + " doesn't start with a tab");
                    if (inEvent) {
                        if (PRINTING_CHUNK_PARSING_INFO) System.out.println("Exiting event");
                        assert lastEvent != null;
                        CodeChunk runChunk = SyntaxManager.getCodeChunkFromCode(eventCode.toString(), file);
                        runChunk.setCode(lastEventCode);
                        runChunk.setParent(chunk);
                        lastEvent.setRunChunk(runChunk);
                        eventCode = new StringBuilder();
                    }
                    inEvent = false;
                }
                if (line.endsWith(":") && !line.startsWith("\t")) {
                    lastEvent = parseEvent(line, file, i + 1);
                    lastEventCode = line;
                    if (PRINTING_CHUNK_PARSING_INFO) System.out.println("Got event for line: " + line + " (event: " + lastEvent + ")");
                    addPiece = new CodePiece(line);
                    assert lastEvent != null;
                    addPiece.setEvent(lastEvent);
                }
                if (addPiece != null) {
                    chunk.addPiece(addPiece);
                }
            }
        }
        if (inEvent && lastEvent != null) {
            CodeChunk runChunk = SyntaxManager.getCodeChunkFromCode(eventCode.toString(), file);
            runChunk.setCode(lastEventCode);
            runChunk.setParent(chunk);
            lastEvent.setRunChunk(runChunk);
        }
        return chunk;
    }


    /**
     * <b>How it works:</b>
     * <p>
     *     This method will loop through every inputted {@link SyntaxPieceFactory} and will then separate the text of said factories regex
     *     into pieces where each piece is separated by a expression (as in "%anything-here%"). For every piece of text if the piece of
     *     text is not an expression we will test if the literal regex matches the corresponding code. If it does not we will continue the loop.
     *     Assuming it does we will then check if the last piece was an expression. If it was we will save the text in between so that it can be
     *     parsed as an expression later. We continue this process until either the loop continues due to the code not matching what is required
     *     by the loop SyntaxPieceFactory or we reach the end of the regex. Should the end of the regex be met we then start to attempt to parse
     *     each separated {@link Expression} if any one of these expressions either doesn't match or isn't of the correct type we continue to the
     *     next factory. Once we have all the expressions we return the {@link SyntaxPiece} that's gathered from the SyntaxPieceFactory.
     *     If we continue to the end of the list of factories we return null;
     * </p>
     * <p>
     *     *Any line starting with (if (CodeChunk.printing)) is temporary and should be removed before any official release.
     * </p>
     * @param code The code from which the code will be extracted from.
     * @param possiblePieces The SyntaxPieceFactories that will be tested.
     * @return A parsed syntax piece. This could be a Event, Effect or Expression.
     */
    public static SyntaxPiece<?> parseSyntaxPiece(String code, ArrayList<? extends SyntaxPieceFactory> possiblePieces, File file, int line) {
        code = code.trim();
        if (CodeChunk.printing) System.out.println("Parsing syntax piece: " + code + " with possible pieces: " + possiblePieces);
        pieces: for (SyntaxPieceFactory syntaxPieceFactory : possiblePieces) {
            if (CodeChunk.printing) System.out.println("On factory w/ regex: " + syntaxPieceFactory.getRegex());
            // Important variable creation
            String codeBuffer = code;
            ArrayList<String> pieces = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            String regex = syntaxPieceFactory.getRegex().replaceFirst("\\$", "");
            ArrayList<String> argTypes = new ArrayList<>();
            boolean lastWasExpression = false;
            // Split into pieces separated by whether or not they're an expression
            // TODO Add support for optional expression arguments
            for (char c : regex.toCharArray()) {
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
            if (!regex.endsWith("%")) {
                pieces.add(builder.toString());
            }
            if (pieces.isEmpty()) pieces.add(regex);
            if (CodeChunk.printing) System.out.println("Pieces: " + pieces);
            ArrayList<String> nestedExpressionTexts = new ArrayList<>();
            // Loop through texts figuring out what texts are expressions and which ones arent while simultaneously separating them
            for (int i = 0; i < pieces.size() + 1; i++) {
                // Init piece and get then get the correct piece from the list of pieces.
                // We check if the ArrayList size isn't to large because the algorithm loops one more time than the length of the ArrayList so that we can catch the last expression.
                String piece = "";
                if (pieces.size() > i) {
                    piece = pieces.get(i);
                }
                // Due to how the code is separated all expression pieces will begin and end with a percentage sign.
                boolean isAnExpression = piece.startsWith("%");
                if (CodeChunk.printing) System.out.println("On piece: " + piece + "(" + i + ") which " + (isAnExpression ? "is" : "isn't") + " an expression");
                // This adds the text for the last expression. We wait until after because the way we figure out when we got to the other side of the expression is by checking for the next piece of text.
                // This is also why we loop one more time. See above for a bit more info on this.
                if (lastWasExpression) {
                    StringBuilder expressionCode = new StringBuilder();
                    String beforeCodeBuffer = codeBuffer;
                    Pattern pattern = Pattern.compile(piece);
                    while (!pattern.matcher(codeBuffer).lookingAt()) {
                        if (codeBuffer.equals("")) {
                            if (CodeChunk.printing) System.out.println("Continuing to next factory due to codeBuffer not being valid");
                            continue pieces;
                        }
                        expressionCode.append(codeBuffer.charAt(0));
                        codeBuffer = codeBuffer.substring(1);
                    }
                    String chosen = (expressionCode.toString().length() > 0 ? expressionCode.toString() : beforeCodeBuffer);
                    nestedExpressionTexts.add(chosen);
                }
                // Refactoring the codeBuffer if the code doesn't match our expression factory's regex.
                if (!isAnExpression) {
                    if (!Pattern.compile(piece).matcher(codeBuffer).lookingAt()) {
                        if (CodeChunk.printing) System.out.println("Continuing to the next factory on factory with regex: " + syntaxPieceFactory.getRegex());
                        continue pieces;
                    }
                    codeBuffer = codeBuffer.replaceFirst(piece, "");
                }
                lastWasExpression = isAnExpression;
            }
            // Once this point has been reached we have all the nested expressions
            ArrayList<ExpressionFactory<?>> nestedExpressionFactories = new ArrayList<>();
            String codeWithoutExpressions = code;
            int i = 0;
            for (String expression : nestedExpressionTexts) {
                if (CodeChunk.printing) System.out.println("-Getting nested expression for text: (" + expression + ")-");
                ExpressionFactory<?> nestedExpressionFactory = parseExpression(expression, file, line);
                if (nestedExpressionFactory != null) {
                    if (!SyntaxManager.SUPPORTED_TYPES.get(argTypes.get(i).replaceAll("%", "")).isAssignableFrom(nestedExpressionFactory.getGenericClass()) && !Variable.class.isAssignableFrom(nestedExpressionFactory.getGenericClass()) && nestedExpressionFactory.getGenericClass() != Object.class) {
                        continue pieces;
                    }
                    nestedExpressionFactory.setCode(expression);
                    if (CodeChunk.printing) System.out.println("Got nested expression for text: (" + Pattern.quote(expression) + "). Now replacing string in codeWithoutExpressions(" + codeWithoutExpressions + ")");
                    codeWithoutExpressions = codeWithoutExpressions.replaceFirst(Pattern.quote(expression), "");
                } else {
                    if (CodeChunk.printing) System.out.println("Continuing cause bad expression");
                    continue pieces;
                }
                nestedExpressionFactories.add(nestedExpressionFactory);
                i++;
            }
            String parsedRegex = syntaxPieceFactory.getRegex().replaceFirst("\\$", "");
            parsedRegex = parsedRegex.replaceAll("%(.*?)%", "");
            if (!codeWithoutExpressions.matches(parsedRegex)) {
                if (CodeChunk.printing) System.out.println("Continuing due to code not matching. Code w/out Expressions: " + codeWithoutExpressions + " Parsed Regex: " + parsedRegex);
                continue;
            }
            if (CodeChunk.printing) System.out.println("Nested Expression factories: " + nestedExpressionFactories);
            SyntaxPiece<?> syntaxPiece = syntaxPieceFactory.getSyntaxPiece().duplicate();
            syntaxPiece.setCode(code);
            nestedExpressionFactories.forEach(expressionFactory -> expressionFactory.setParent(syntaxPiece));
            syntaxPieceFactory = (SyntaxPieceFactory) syntaxPiece;
            syntaxPieceFactory.getExpressionArgs().addAll(nestedExpressionFactories);
            return syntaxPieceFactory.getSyntaxPiece();
        }
        return null;
    }
    public static SyntaxPiece<?> parseSyntaxPiece(String code, ArrayList<? extends SyntaxPieceFactory> possiblePieces) {
        return parseSyntaxPiece(code, possiblePieces, null, 0);
    }

    /**
     *
     * @param code The code which will be parsed and the effect will be extracted from.
     * @return The effect that is parsed from the given code.
     */
    public static Effect parseLine(String code, File file, int lineNum) {
        Effect parsedEffect = (Effect) parseSyntaxPiece(code.trim(), SyntaxManager.EFFECT_FACTORIES, file, lineNum);
        if (parsedEffect == null) {
            new ParseError(lineNum, 0, code, "Unrecognized Effect", null, file).print();
        }
        return parsedEffect;
    }
    public static Effect parseLine(String code) {
        return parseLine(code, null, 0);
    }

    /**
     *
     * @param code The code from which the Event is parsed. Should be only one line. For example: "when {button} is pressed:"
     * @return A parsed event that doesn't contain it's CodeChunk.
     */
    public static Event parseEvent(String code, File file, int lineNum) {
        // Different types of Events:
        //  * If statements
        //  * Else statements
        //  * List loops
        //  * Number loops
        //  * Functions
        //  * Run when x
        code = code.trim();
        if (code.startsWith("if")) {
            Expression<?> expression = parseExpression(code.replaceFirst("if ", "").replaceAll(":", ""), file, lineNum);
            assert expression != null;
            if (expression.getGenericClass() == Boolean.class) {
                return new IfStatement((Expression<Boolean>) expression);
            }
        } else if (code.startsWith("else")) {
            if (!code.contains("if")) {
                System.out.println("ELSE STATEMENT");
                return new ElseStatement();
            }
//            else if (code.startsWith("else if")) {
//
//            }
        } else if (code.startsWith("function")) {
            ArrayList<String> spaces = new ArrayList<>(Arrays.asList(code.split(" ")));
            spaces.remove(0);
            if (spaces.size() > 1 && spaces.get(1).contains("(")) {
                spaces.remove(0);
            }
            StringBuilder spacesConnected = new StringBuilder();
            int i = 0;
            for (String space : spaces) {
                i++;
                spacesConnected.append(space);
                if (i != spaces.size()) {
                    spacesConnected.append(" ");
                }
            }
            String functionName = spacesConnected.toString().replaceAll("\\((.*?)\\)", "").replaceAll(":", "");
            String params = spacesConnected.toString().split("\\(")[1].replaceAll(":", "").replaceAll("\\)", "");
            ArrayList<Function.FunctionArgument> functionArguments = new ArrayList<>();
            for (String param : params.split(",( *)")) {
                if (param.split(" ").length > 1) {
                    System.out.println("Param: " + param);
                    Class<?> type = SyntaxManager.SUPPORTED_TYPES.get(param.split(" ")[0].toLowerCase());
                    String name = param.split(" ")[1];
                    functionArguments.add(new Function.FunctionArgument(type, name));
                }
            }
            Function<?> function = new Function<>(functionName);
            function.getArguments().addAll(functionArguments);
            System.out.println(function.getArguments());
            return function;
        } else {
            String justCode = code.replaceAll(":", "").replaceFirst("\\$", "");
            if (CodeChunk.printing) System.out.println("Getting event from code: " + justCode);
            SyntaxPiece<?> syntaxPiece = parseSyntaxPiece(justCode, SyntaxManager.EVENT_FACTORIES, file, lineNum);
            // Error Handling
            if (syntaxPiece == null) {
                new ParseError(lineNum, 0, code, "Unrecognized Event", null, file).print();
                return null;
            }
            if (code.startsWith("$") || ((WhenEventFactory) syntaxPiece).getRegex().startsWith("$")) {
                ((Event) syntaxPiece).setParent(new CodePiece(""));
                ((Event) syntaxPiece).runWhenArrivedTo();
                if (syntaxPiece instanceof WhenEventFactory) {
                    ((WhenEventFactory) syntaxPiece).setEventProcessedHandler((state, values, event, args) -> { });
                }
            }
            return (Event) syntaxPiece;
        }
        new ParseError(lineNum, 0, code, "Unrecognized Event", null, file).print();
        return null;
    }
    public static Event parseEvent(String code) {
        return parseEvent(code, null, 0);
    }

    /**
     *
     * @param code Inputted code, should already be trimmed and improved.
     * @return The parsed Expression. Will return null if it isn't a valid Expression.
     */
    public static ExpressionFactory<?> parseExpression(String code, File file, int lineNum) {
        code = code.trim();
        if (CodeChunk.printing) System.out.println("--Parsing Expression (code: " + code +")--");
        HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> fullMap = new HashMap<>();
        addHashMapToHashMap(fullMap, SyntaxManager.HIGHEST);
        addHashMapToHashMap(fullMap, SyntaxManager.HIGH);
        addHashMapToHashMap(fullMap, SyntaxManager.MEDIUM);
        addHashMapToHashMap(fullMap, SyntaxManager.LOW);
        addHashMapToHashMap(fullMap, SyntaxManager.LOWEST);
        ArrayList<ExpressionFactory<?>> allExpressions = new ArrayList<>();
        fullMap.values().forEach(allExpressions::addAll);
        return (ExpressionFactory<?>) parseSyntaxPiece(code, allExpressions, file, lineNum);
    }
    public static ExpressionFactory<?> parseExpression(String code) {
        return parseExpression(code, null, 0);
    }

    /**
     * 
     * @param addTo The HashMap which addFrom is added to.
     * @param addFrom The HashMap that is added to addTo
     * @param <T> The type of object stored in the arraylist
     */
    private static <T> void addHashMapToHashMap(HashMap<Class<?>, ArrayList<T>> addTo, HashMap<Class<?>, ArrayList<T>> addFrom) {
        for (Class<?> loopClass : addFrom.keySet()) {
            addTo.computeIfAbsent(loopClass, k -> new ArrayList<>());
            addTo.get(loopClass).addAll(addFrom.get(loopClass));
        }
    }

    /**
     * This method is used to get a list of strings separated by percentage signs.
     * @param code The different expressions will be separated from this string
     * @return A {@code ArrayList} of strings where the expressions start and end with a %
     */
    public static ArrayList<String> generateExpressionPiecesFromString(String code) {
        ArrayList<String> separatedStrings = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (char character : code.toCharArray()) {
            if (character == '%') {
                if (builder.toString().startsWith("%")) {
                    builder.append(character);
                    separatedStrings.add(builder.toString());
                    builder = new StringBuilder();
                    continue;
                } else {
                    separatedStrings.add(builder.toString());
                    builder = new StringBuilder();
                }
            }
            builder.append(character);
        }
        if (!builder.toString().equals("")) {
            separatedStrings.add(builder.toString());
        }
        return separatedStrings;
    }

}