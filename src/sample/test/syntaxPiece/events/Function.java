package sample.test.syntaxPiece.events;

import sample.test.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class Function<T> extends Event {

    public final static HashMap<String, Function<?>> ALL_FUNCTIONS = new HashMap<>();

    private final String name;
    private final ArrayList<FunctionArgument> arguments = new ArrayList<>();
    public static boolean PRINT_IF_RETURNING_NULL = false;

    private boolean ran = false;

    public Function(String name) {
        this.name = name;
        if (ALL_FUNCTIONS.containsKey(name)) System.err.println("Function with name: " + name + " already exists and thus cannot be created.");
        ALL_FUNCTIONS.put(name, this);
    }

    @Override
    public void runWhenArrivedTo() {

    }

    public T invoke(Object... args) {
        // Check if args match args
        int i = 0;
        if (ran) {
            this.setRunChunk(this.getRunChunk().duplicateWithoutVariables());
        }
        ran = true;
        for (FunctionArgument argument : arguments) {
            if (args.length - 1 < i) {
                System.err.println("Function call to function: " + name + " is missing parameter: " + argument.getVariableName());
            } else {
                getRunChunk().addVariable(new Variable<>(args[i], argument.getVariableName(), false));
            }
            i++;
        }
        run();
        T returnedObject = (T) getRunChunk().getReturnedObject();
        if (returnedObject == null && PRINT_IF_RETURNING_NULL) {
            System.err.println("Function: " + name + " returned null.");
        }
        return returnedObject;
    }

    public ArrayList<FunctionArgument> getArguments() {
        return arguments;
    }

    public static class FunctionArgument {

        private Class<?> type;
        private String variableName;

        public FunctionArgument(Class<?> type) { this(type, ""); }
        public FunctionArgument(String variableName) { this(null, variableName); }
        public FunctionArgument(Class<?> type, String variableName) {
            this.type = type;
            this.variableName = variableName;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        @Override
        public String toString() {
            return "FunctionArgument{" +
                    "type=" + type +
                    ", variableName='" + variableName + '\'' +
                    '}';
        }
    }


}
