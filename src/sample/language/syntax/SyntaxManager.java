package sample.language.syntax;

import sample.language.FXScript;
import sample.language.interpretation.run.CodeChunk;
import sample.language.interpretation.run.CodePiece;
import sample.language.interpretation.run.CodeState;
import sample.language.syntax.addons.AddonBase;
import sample.language.syntax.addons.JavaFXAddon;
import sample.language.syntaxPiece.effects.Effect;
import sample.language.syntaxPiece.effects.EffectFactory;
import sample.language.syntaxPiece.events.Function;
import sample.language.syntaxPiece.events.WhenEventFactory;
import sample.language.syntaxPiece.expressions.ExpressionFactory;
import sample.language.syntaxPiece.expressions.ExpressionPriority;
import sample.language.variable.LinkedList;
import sample.language.variable.List;
import sample.language.variable.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 *
 */
public class SyntaxManager {

    public static final HashMap<String, Class<?>> SUPPORTED_TYPES = new HashMap<>();

    public static final HashMap<ExpressionPriority, HashMap<Class<?>, ArrayList<ExpressionFactory<?>>>> EXPRESSIONS = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOWEST = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> LOW = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> MEDIUM = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGH = new HashMap<>();
    public static final HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> HIGHEST = new HashMap<>();

    public static final ArrayList<EffectFactory> EFFECT_FACTORIES = new ArrayList<>();
    public static final ArrayList<WhenEventFactory> EVENT_FACTORIES = new ArrayList<>();

    public static final ArrayList<Class<? extends AddonBase>> ADDON_CLASSES = new ArrayList<>();
    public static final ArrayList<AddonBase> ADDONS = new ArrayList<>();

    public static void init() {

        // Initiate addons
        ADDON_CLASSES.add(JavaFXAddon.class);
        for (Class<?> addon : ADDON_CLASSES) {
            try {
                AddonBase addonBase = (AddonBase) addon.getConstructor().newInstance();
                ADDONS.add(addonBase);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        // All
        // Supported Types

        SUPPORTED_TYPES.put("file", File.class);

        // Java
        SUPPORTED_TYPES.put("value", Object.class);
        SUPPORTED_TYPES.put("object", Object.class);
        SUPPORTED_TYPES.put("string", String.class);
        SUPPORTED_TYPES.put("number", Number.class);
        SUPPORTED_TYPES.put("boolean", Boolean.class);

        //Special
        SUPPORTED_TYPES.put("variable", Variable.class);
        SUPPORTED_TYPES.put("list", List.class);
        SUPPORTED_TYPES.put("linked-list", LinkedList.class);

        // Load from addons
        for (AddonBase addonBase : ADDONS) {
            addonBase.addTypes();
        }

        for (Class<?> loopClass : SUPPORTED_TYPES.values()) {
            HIGHEST.put(loopClass, new ArrayList<>());
            HIGH.put(loopClass, new ArrayList<>());
            MEDIUM.put(loopClass, new ArrayList<>());
            LOW.put(loopClass, new ArrayList<>());
            LOWEST.put(loopClass, new ArrayList<>());
        }

        EXPRESSIONS.put(ExpressionPriority.LOWEST, LOWEST);
        EXPRESSIONS.put(ExpressionPriority.LOW, LOW);
        EXPRESSIONS.put(ExpressionPriority.MEDIUM, MEDIUM);
        EXPRESSIONS.put(ExpressionPriority.HIGH, HIGH);
        EXPRESSIONS.put(ExpressionPriority.HIGHEST, HIGHEST);

        effects: {

            addons: {
                EFFECT_FACTORIES.add(new EffectFactory("run code in %file%", (state, values, args) -> {
                    File file = (File) values.get(0);
                    try {
                        SyntaxManager.getCodeChunkFromCode(file).run();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }));
                EFFECT_FACTORIES.add(new EffectFactory("import %file%","$import %file%", (state, values, args) -> {
                    File file = (File) values.get(0);
                    System.out.println("Running file: " + file);
                    try {
                        SyntaxManager.getCodeChunkFromCode(file).run();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }));
            }

            reflect: {
                EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "%object%\\.(.*\\))", (state, values, args) -> {
                    String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                    Object obj = values.get(0);
                    reflectMethod(state, connectedArgs, obj);
                }));
            }
            file: {
                EFFECT_FACTORIES.add(new EffectFactory("create new file %file%", (state, values, args) -> {
                    try {
                        ((File) values.get(0)).createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
                EFFECT_FACTORIES.add(new EffectFactory("create new directory %file%", (state, values, args) -> ((File) values.get(0)).mkdir()));
                EFFECT_FACTORIES.add(new EffectFactory("delete %file%", (state, values, args) -> ((File) values.get(0)).delete()));
                EFFECT_FACTORIES.add(new EffectFactory("move %file% to %string%", (state, values, args) -> ((File) values.get(0)).renameTo(new File(values.get(1).toString()))));
                EFFECT_FACTORIES.add(new EffectFactory("write %string% to %file%", (state, values, args) -> {
                    File f = (File) values.get(1);
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            return;
                        }
                    }
                    try {
                        FileWriter fileWriter = new FileWriter(f.getPath());
                        fileWriter.write(values.get(0).toString());
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }

            EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "([A-z]+)\\((.*?)\\)", (state, values, args) -> {
                String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                Function<?> function = Function.ALL_FUNCTIONS.get(connectedArgs.split("\\(")[0]);
                function.invoke();
            }));
            EFFECT_FACTORIES.add(new EffectFactory("return %object%", (state, values, args) -> {
                state.setReturnedObject(values.get(0));
                ((CodeChunk) state).setFinished(true);
            }));
            EFFECT_FACTORIES.add(new EffectFactory("set %variable% to %object%", (state, values, args) -> state.setVariableValue(
                    ((Variable<?>) values.get(0)).getName(),
                    values.get(1))));
            EFFECT_FACTORIES.add(new EffectFactory("IGNORE", "[^\\s]+(:| =) %object%", (state, values, args) -> {
                String varName = args[0].replaceAll(":", "").replaceAll("=", "").replaceAll("[{}]", "");
                state.setVariableValue(varName, values.get(0));
            }));
            EFFECT_FACTORIES.add(new EffectFactory("remove %object% from %list%", (state, values, args) -> ((List) values.get(1)).removeObject(values.get(0))));
            EFFECT_FACTORIES.add(new EffectFactory("add %object% to %list%", (state, values, args) -> ((List) values.get(1)).add(values.get(0), values.get(0).toString())));

            EFFECT_FACTORIES.add(new EffectFactory("print %object%", (state, values, args) -> System.out.println(values.get(0))));
        }
        events: {

            special: {
                EVENT_FACTORIES.add(new WhenEventFactory("expression %type% -> %text%:","$expression [^\\s]+? -> (.*?)", (state, values, event, args) -> {
                    StringBuilder builder = new StringBuilder();
                    appendAllArgs(builder, args);
                    String returnType = args[1];
                    Class<?> returnClass = SUPPORTED_TYPES.get(returnType);
                    if (returnClass == null) {
                        System.err.println("Can't add expression for text: " + builder.toString() + " because type: " + returnType + " isn't a valid type. Valid types are: " + SUPPORTED_TYPES.keySet());
                    } else {
                        ExpressionFactory<Object> expressionFactory = new ExpressionFactory<>(builder.toString().replaceFirst("expression ", "").replaceFirst("(.*?) -> ", ""), (state1, values1, args1) -> {
                            event.getRunChunk().getLocalExpressions().clear();
                            int i = 0;
                            for (Object obj : values1) {
                                i++;
                                event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>(String.valueOf(i), (state2, values2, args2) -> obj, Object.class));
                            }
                            event.run();
                            return event.getRunChunk().getReturnedObject();
                        }, Object.class);
                        LOW.get(returnClass).add(expressionFactory);
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("effect -> %text%:","$effect -> (.*?)", (state, values, event, args) -> {
                    StringBuilder builder = new StringBuilder();
                    appendAllArgs(builder, args);
                    EffectFactory effectFactory = new EffectFactory(builder.toString().replaceFirst("effect -> ", ""), (state1, values1, args1) -> {
                        event.getRunChunk().getLocalExpressions().clear();
                        int i = 0;
                        for (Object obj : values1) {
                            i++;
                            event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>(String.valueOf(i), (state2, values2, args2) -> obj, Object.class));
                        }
                        event.run();
                    });
                    EFFECT_FACTORIES.add(effectFactory);
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("chance of %number%", (state, values, event, args) -> {
                    double value = Math.random();
                    if (value < (double) values.get(0)) {
                        event.run();
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("loop %number% times", (state, values, event, args) -> {
                    double times = ((Number) values.get(0)).doubleValue();
                    for (int i = 0; i < times; i++) {
                        event.getRunChunk().getLocalExpressions().clear();
                        int finalI = i;
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("num(ber|)", (state1, values1, args1) -> finalI, Number.class));
                        event.run();
                    }
                }));
                EVENT_FACTORIES.add(new WhenEventFactory("loop %list%", (state, values, event, args) -> {
                    List list = (List) values.get(0);
                    int loopTimes = 0;
                    for (Map.Entry<String, Object> entry : list.getValues().entrySet()) {
                        event.getRunChunk().getLocalExpressions().clear();
                        int finalLoopTimes = loopTimes;
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("num(ber|)", (state1, values1, args1) -> finalLoopTimes, Number.class));
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("index", (state1, values1, args1) -> entry.getKey(), String.class));
                        event.getRunChunk().getLocalExpressions().add(new ExpressionFactory<>("item", (state1, values1, args1) -> entry.getValue(), Object.class));
                        event.run();
                        loopTimes++;
                    }
                }));
            }
        }
        expressions: {
            java: {
                string: {
                    HIGHEST.get(String.class).add(new ExpressionFactory<>("IGNORE", "\"([^\\\"]*?)\"", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        return builder.toString().replaceAll("\"", "");
                    }, String.class));
                    HIGHEST.get(String.class).add(new ExpressionFactory<>("%string% appended to %string%", (state, values, args) -> values.get(0).toString() + values.get(1), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without the last character", "%string% without( the|) last character", (state, values, args) -> {
                        if (values.get(0).toString().length() < 1) return values.get(0).toString();
                        return values.get(0).toString().substring(0, values.get(0).toString().length() - 1);
                    }, String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without the first character", "%string% without( the|) first character", (state, values, args) -> {
                        if (values.get(0).toString().length() < 1) return values.get(0).toString();
                        return values.get(0).toString().substring(1);
                    }, String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("%string% without character at %number%", "%string% without character (at |)%number%", (state, values, args) -> {
                        String arg1 = values.get(0).toString();
                        int arg2 = ((Double) values.get(1)).intValue();
                        return arg1.substring(0, arg2) + (arg1.length() > arg2 + 1 ? arg1.substring(0, arg2 + 1) : "");
                    }, String.class));
//                    MEDIUM.get(String.class).add(new ExpressionFactory<>("text of %button%", (state, values, args) -> ((Button) values.get(0)).getText(), String.class));
//                    MEDIUM.get(String.class).add(new ExpressionFactory<>("title of %stage%", (state, values, args) -> ((Stage) values.get(0)).getTitle(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("path of %file%", (state, values, args) -> ((File) values.get(0)).getAbsolutePath(), String.class));
                    LOW.get(String.class).add(new ExpressionFactory<>("text in %file%", (state, values, args) -> {
                        File f = (File) values.get(0);
                        if (f == null || !f.exists()) {
                            System.err.println("Can't read from file that doesn't exist, file: " + f);
                            return "INVALID FILE";
                        }
                        try {
                            Scanner sc = new Scanner(f);
                            StringBuilder text = new StringBuilder();
                            int i = 0;
                            while (sc.hasNextLine()) {
                                text.append(sc.nextLine()).append(i == 0 ? "" : "\n");
                                i++;
                            }
                            return text.toString();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.err.println("Couldn't read file: " + f);
                        return "COULDN'T READ FILE";
                    }, String.class));
                }
                number: {
                    HIGHEST.get(Number.class).add(new ExpressionFactory<>("IGNORE", "([0-9]+)(|\\.([0-9]+))", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        return Double.parseDouble(builder.toString().replaceAll("@", ""));
                    }, Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\+ %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() + ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% - %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() - ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\* %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() * ((Number) values.get(1)).doubleValue(), Number.class));
                    HIGH.get(Number.class).add(new ExpressionFactory<>("IGNORE", "%number% \\/ %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() / ((Number) values.get(1)).doubleValue(), Number.class));

                    MEDIUM.get(Number.class).add(new ExpressionFactory<>("random number between %number% and %number%", (state, values, args) -> {
                        double firstValue = (double) values.get(0);
                        double secondValue = (double) values.get(1);
                        if (!(firstValue < secondValue)) {
                            System.err.println("Attempted to get a random number between " + firstValue + " and " + secondValue + " but the second value is not larger than the first value.");
                            return -1;
                        }
                        return ThreadLocalRandom.current().nextDouble(firstValue, secondValue);
                    }, Number.class));
                    MEDIUM.get(Number.class).add(new ExpressionFactory<>("random integer between %number% and %number%", (state, values, args) -> {
                        int firstValue = (int) Math.round((double) values.get(0));
                        int secondValue = (int) Math.round((double) values.get(1));
                        if (!(firstValue < secondValue)) {
                            System.err.println("Attempted to get a random number between " + firstValue + " and " + secondValue + " but the second value is not larger than the first value.");
                            return -1;
                        }
                        return ThreadLocalRandom.current().nextInt(firstValue, secondValue + 1);
                    }, Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("length of %string%", (state, values, args) -> values.get(0).toString().length(), Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("space taken up by %file%", (state, values, args) -> Long.valueOf(((File) values.get(0)).getTotalSpace()).doubleValue(), Number.class));
                    LOW.get(Number.class).add(new ExpressionFactory<>("size of %list%", ((state, values, args) -> ((List) values.get(0)).getValues().values().size()), Number.class));
                }
                bool: {
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("true", (state, values, args) -> true, Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("false", (state, values, args) -> false, Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%object% == %object%", (state, values, args) -> values.get(0).equals(values.get(1)), Boolean.class));
                    HIGHEST.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%object% != %object%", (state, values, args) -> !values.get(0).equals(values.get(1)), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% > %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() > ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% < %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() < ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% >= %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() >= ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("IGNORE", "%number% <= %number%", (state, values, args) -> ((Number) values.get(0)).doubleValue() <= ((Number) values.get(1)).doubleValue(), Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("%number% is a multiple of %number%", (state, values, args) -> (double) ((Number) values.get(0)) % (double) ((Number) values.get(1)) == 0, Boolean.class));
                    HIGH.get(Boolean.class).add(new ExpressionFactory<>("\\!%boolean%", (state, values, args) -> !((Boolean) values.get(0)), Boolean.class));
                    LOW.get(Boolean.class).add(new ExpressionFactory<>("computer is connected to the internet", (state, values, args) -> {
                        try {
                            final URL url = new URL("http://www.google.com");
                            final URLConnection conn = url.openConnection();
                            conn.connect();
                            conn.getInputStream().close();
                            return true;
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            return false;
                        }
                    }, Boolean.class));
                    LOW.get(Boolean.class).add(new ExpressionFactory<>("free space in %file%", (state, values, args) -> ((File) values.get(0)).exists(), Boolean.class));
                }
            }
            special: {
                variable: {
                    HIGHEST.get(Variable.class).add(new ExpressionFactory<>("IGNORE", "\\{([^\\s]*?)\\}", (state, values, args) -> {
                        StringBuilder builder = new StringBuilder();
                        appendAllArgs(builder, args);
                        String variableName = builder.toString().replaceAll("[{}]", "");
                        if (CodeChunk.printing) System.out.println("Getting Variable: " + variableName + " State: " + state + " children: " + state.getChildren());
                        if (state.getVariableByName(variableName) == null) state.setVariableValue(variableName, null);
                        return state.getVariableByName(variableName);
                    }, Variable.class));
                }
                function: {
                    HIGHEST.get(Object.class).add(new ExpressionFactory<>("IGNORE", "([A-z]+)\\((.*?)\\)", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                        String[] arguments = connectedArgs.split("\\(")[1].replaceAll("\\)", "").split(",( *)");
                        ArrayList<Object> objects = new ArrayList<>();
                        for (String argument : arguments) {
                            if (argument.length() > 0) {
                                ExpressionFactory<?> expressionFactory = FXScript.PARSER.parseExpression(argument);
                                expressionFactory.setState((CodeChunk) state);
                                expressionFactory.forChildren(expressionFactory1 -> expressionFactory1.setState(expressionFactory.getState()));
                                objects.add(expressionFactory.activate());
                            }
                        }
                        Function<?> function = Function.ALL_FUNCTIONS.get(connectedArgs.split("\\(")[0]);
                        return function.invoke(objects.toArray());
                    }, Object.class));
                }
                reflect: {
                    MEDIUM.get(Object.class).add(new ExpressionFactory<>("IGNORE", "%object%\\.(.*\\))", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString();
                        Object obj = values.get(0);
                        return reflectMethod(state, connectedArgs, obj);
                    }, Object.class));
                }
                loopOrEventValue: {
                    ExpressionFactory<?> loopOrEventValue = new ExpressionFactory<>("IGNORE", "(event|loop|expression|effect|arg|value)-(([^\\s]|-)*)", (state, values, args) -> {
                        String connectedArgs = appendAllArgs(new StringBuilder(), args).toString().split("-")[1];
                        for (ExpressionFactory<?> expression : state.getLocalExpressions()) {
                            if (connectedArgs.matches(expression.getRegex())) {
                                ExpressionFactory<?> dupedExpression = expression.duplicate();
                                dupedExpression.setCode(connectedArgs);
                                return dupedExpression.activate();
                            }
                        }
                        System.err.println("Invalid event value: " + connectedArgs + ". No local expression matches. Tried for: " + state.getLocalExpressions());
                        return null;
                    }, Object.class);
                    HIGHEST.get(Object.class).add(loopOrEventValue);
                }
            }
            lists: {
                file: {
                    MEDIUM.get(List.class).add(new ExpressionFactory<>("files in %file%", (state, values, args) -> List.fromList(Objects.requireNonNull(((File) values.get(0)).listFiles())), List.class));
                }
            }
        }

        // Load syntax from addons
        for (AddonBase addonBase : ADDONS) {
            addonBase.addSyntax();
        }


    }

    /**
     * This is in it's own method because it is easily the most complex effect/expression. And being used twice it doesn't make sense for it to be in it's respective expression and effect.
     */
    private static Object reflectMethod(CodeState state, String connectedArgs, Object obj) {
        String allMethodsInText = connectedArgs.replaceFirst("(.*?)\\.", "");
        ArrayList<String> methods = new ArrayList<>();
        StringBuilder currentPiece = new StringBuilder();
        int parenthesesDepth = 0;
        for (char c : allMethodsInText.toCharArray()) {
            if (c == '(') parenthesesDepth++;
            else if (c == ')') parenthesesDepth--;
            else if (c == '.' && parenthesesDepth <= 0) {
                methods.add(currentPiece.toString());
                currentPiece = new StringBuilder();
                continue;
            }
            currentPiece.append(c);
        }
        if (currentPiece.toString().length() > 0 && !currentPiece.toString().equals(",")) methods.add(currentPiece.toString());
        Object currentObj = obj;
        for (String methodText : methods) {
            Object nextObj = getMethodFromString(methodText, currentObj.getClass(), state, currentObj);
            if (nextObj == null) return currentObj;
            currentObj = nextObj;
        }
        return currentObj;
    }

    private static Object getMethodFromString(String methodText, Class<?> objectClass, CodeState state, Object invokeFor) {
        String methodName = methodText.split("\\(")[0];
        String params = methodText.replaceFirst("(.*?)\\(", "");
        params = params.substring(0, params.length() - 1);
        ArrayList<Object> methodArgs = new ArrayList<>();
        ArrayList<Class<?>> methodArgClasses = new ArrayList<>();
        ArrayList<String> argumentStrings = new ArrayList<>();
        int inParentheses = 0;
        StringBuilder currentPiece = new StringBuilder();
        for (char c : params.toCharArray()) {
            if (c == '(') inParentheses++;
            else if (c == ')') inParentheses--;
            else if (c == ',' && inParentheses <= 0) {
                argumentStrings.add(currentPiece.toString());
                currentPiece = new StringBuilder();
                continue;
            }
            currentPiece.append(c);
        }
        java.util.List<Method> methods = Arrays.stream(objectClass.getMethods()).filter(method -> method.getName().equals(methodName) && method.getParameters().length == argumentStrings.size() + 1).collect(Collectors.toList());
        if (currentPiece.toString().length() > 0 && !currentPiece.toString().equals(",")) argumentStrings.add(currentPiece.toString());
        int i = 0;
        for (String argument : argumentStrings) {
            argument = argument.trim();
            System.out.println("On argument: (" + argument + ")");
            if (argument.length() > 0) {
                ExpressionFactory<?> expressionFactory = FXScript.PARSER.parseExpression(argument);
                expressionFactory.setState((CodeChunk) state);
                expressionFactory.forChildren(expressionFactory1 -> expressionFactory1.setState(expressionFactory.getState()));
                Object objFromExpression = expressionFactory.activate();
                if (objFromExpression.getClass() == Double.class || objFromExpression.getClass() == Integer.class) {
                    boolean isDouble = objFromExpression.toString().matches("([0-9]+)\\.([1-9]([0-9]+|)|[0-9]+[0-9]+)");
                    if (methods.size() == 1) {
                        Parameter param = methods.get(0).getParameters()[i];
                        isDouble = param.getType() != int.class;
                    }
                    if (isDouble) {
                        methodArgs.add(((Number) objFromExpression).doubleValue());
                        methodArgClasses.add(double.class);
                    } else {
                        methodArgs.add(((Number) objFromExpression).intValue());
                        methodArgClasses.add(int.class);
                    }
                } else if (objFromExpression.getClass() == Variable.class) {
                    Object add = ((Variable<?>) expressionFactory.activate()).getValue();
                    methodArgs.add(add);
                    methodArgClasses.add(add.getClass());
                } else {
                    methodArgs.add(expressionFactory.activate());
                    methodArgClasses.add(objFromExpression.getClass());
                }
            }
            i++;
        }
        try {
            return objectClass.getMethod(methodName, methodArgClasses.toArray(new Class[0])).invoke(invokeFor, methodArgs.toArray());
        } catch (NoSuchMethodException e) {
            System.err.println("There is no method named: " + methodName + " that accepts arguments: " + methodArgClasses);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static StringBuilder appendAllArgs(StringBuilder builder, String... args) {
        int i = 0;
        for (String space : args) {
            i++;
            builder.append(space);
            if (args.length != i) {
                builder.append(' ');
            }
        }
        return builder;
    }

    /**
     *
     * @param code The code from which the code piece will be generated.
     * @return A CodePiece interpreted and parsed from the inputted code. Note that this piece is not yet attached to a code chunk.
     */
    public static CodePiece genCodePieceFromCode(String code, File file, int lineNum) {
        Effect effect = FXScript.PARSER.parseLine(code, file, lineNum);
        CodePiece piece = new CodePiece(code);
        if (effect != null) {
            piece.setEffect(effect);
        }
        return piece;
    }

    public static CodeChunk getCodeChunkFromCode(File file) throws FileNotFoundException {
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine()).append('\n');
        }
        return getCodeChunkFromCode(builder.toString(), file);
    }
    public static CodeChunk getCodeChunkFromCode(String code, File file) {
        return FXScript.PARSER.parseChunk(code, file);
    }

    public static boolean checkPrefix(String check, String... prefixes) {
        for (String prefix : prefixes) {
            if (check.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactories() {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        EXPRESSIONS.values().forEach(classArrayListHashMap -> classArrayListHashMap.values().forEach(arrayList::addAll));
        return arrayList;
    }
    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactories(Class<?> ofClass) {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        EXPRESSIONS.values().forEach(classArrayListHashMap -> arrayList.addAll(classArrayListHashMap.get(ofClass)));
        return arrayList;
    }
    public static ArrayList<ExpressionFactory<?>> getAllExpressionFactoriesFromClass(Class<?> ofClass) {
        ArrayList<ExpressionFactory<?>> arrayList = new ArrayList<>();
        for (HashMap<Class<?>, ArrayList<ExpressionFactory<?>>> map : EXPRESSIONS.values()) {
            for (Map.Entry<Class<?>, ArrayList<ExpressionFactory<?>>> entry : map.entrySet()) {
                if (ofClass.isAssignableFrom(entry.getKey()) || entry.getKey() == Object.class || ofClass == String.class) {
                    arrayList.addAll(entry.getValue());
                }
            }
        }
        return arrayList;
    }

}
