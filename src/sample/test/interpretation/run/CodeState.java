package sample.test.interpretation.run;

import sample.test.syntaxPiece.events.Event;
import sample.test.syntaxPiece.expressions.Expression;
import sample.test.syntaxPiece.expressions.ExpressionFactory;
import sample.test.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public class CodeState {

    protected String code = "";

    protected final HashMap<String, Variable<?>> variables = new HashMap<>();
    private final ArrayList<ExpressionFactory<?>> localExpressions = new ArrayList<>();
    protected final ArrayList<CodeState> children = new ArrayList<>();
    protected CodeState parent;
    protected boolean global = false;

    protected Event lastEvent;


    protected AddedListener<Variable<?>> newVariable;


    private Object returnedObject;

    public Object getVariable(String name) {
        return variables.get(name);
    }
    public void setVariableValue(String name, Object value) {
        if (!variables.containsKey(name)) addVariable(new Variable<>(name, global));
        variables.get(name).setValue(value);
    }
    public void addVariables(Variable<?>... variables) {
        for (Variable<?> variable : variables) {
            addVariable(variable);
        }

    }
    public void addVariable(Variable<?> variable) {
        variables.put(variable.getName(), variable);
        for (CodeState state : children) {
            state.addVariable(variable);
        }
        if (newVariable != null) {
            newVariable.added(variable);
        }
    }

    public void copyVariableValuesToChildren() {
        for (CodeState state : children) {
            copyVariableValuesToChild(state);
        }
    }
    public void copyVariableValuesToChild(CodeState state) {
        for (Variable<?> variable : variables.values()) {
            state.addVariable(new Variable<>(variable.getValue(), variable.getName(), variable.isGlobal()));
        }
    }

    public void setParent(CodeState parent) {
        this.parent = parent;
        parent.getChildren().add(this);
        variables.putAll(parent.getVariables());
    }
    public CodeState getParent() {
        return parent;
    }

    public ArrayList<CodeState> getChildren() {
        return children;
    }

    public Variable<?> getVariableByName(String name) {
        return variables.get(name);
    }

    public HashMap<String, Variable<?>> getVariables() {
        return variables;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setNewVariable(AddedListener<Variable<?>> newVariable) {
        if (newVariable != null) {
            for (Variable<?> variable : variables.values()) {
                newVariable.added(variable);
            }
        }
        this.newVariable = newVariable;
    }

    public ArrayList<ExpressionFactory<?>> getLocalExpressions() {
        if (this instanceof CodeChunk) {
            for (Expression<?> expression : localExpressions) {
                expression.setState((CodeChunk) this);
            }
        }
        return localExpressions;
    }

    public Object getReturnedObject() {
        return returnedObject;
    }

    public void setReturnedObject(Object returnedObject) {
        this.returnedObject = returnedObject;
    }

    public void setLastEvent(Event lastEvent) {
        this.lastEvent = lastEvent;
    }

    public Event getLastEvent() {
        return lastEvent;
    }

    public CodeState duplicateWithoutVariables() {
        return duplicateWithoutVariables(parent);
    }
    public CodeState duplicateWithoutVariables(CodeState parent) {
        CodeState newState = new CodeState();
        newState.setCode(code);
        newState.setParent(parent);
        newState.getLocalExpressions().addAll(localExpressions);
        this.children.forEach(codeState -> newState.getChildren().add(codeState.duplicateWithoutVariables(newState)));
        return newState;
    }

    @Override
    public String toString() {
        return code.equals("") ? super.toString() : code + hashCode();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
