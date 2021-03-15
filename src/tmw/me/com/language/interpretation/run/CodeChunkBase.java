package tmw.me.com.language.interpretation.run;

import tmw.me.com.language.syntaxPiece.events.Event;
import tmw.me.com.language.syntaxPiece.expressions.Expression;
import tmw.me.com.language.syntaxPiece.expressions.ExpressionFactory;
import tmw.me.com.language.variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class CodeChunkBase {

    protected Event holder;

    protected String code = "";

    protected final HashMap<String, Variable<?>> variables = new HashMap<>();
    private final ArrayList<ExpressionFactory<?>> localExpressions = new ArrayList<>();
    protected final ArrayList<CodeChunkBase> children = new ArrayList<>();
    protected CodeChunkBase parent;
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
        for (CodeChunkBase state : children) {
            state.addVariable(variable);
        }
        if (newVariable != null) {
            newVariable.added(variable);
        }
    }

    public void copyVariableValuesToChildren() {
        for (CodeChunkBase state : children) {
            copyVariableValuesToChild(state);
        }
    }

    public void copyVariableValuesToChild(CodeChunkBase state) {
        for (Variable<?> variable : variables.values()) {
            state.addVariable(new Variable<>(variable.getValue(), variable.getName(), variable.isGlobal()));
        }
    }

    public void setParent(CodeChunkBase parent) {
        this.parent = parent;
        parent.getChildren().add(this);
        variables.putAll(parent.getVariables());
    }

    public CodeChunkBase getParent() {
        return parent;
    }

    public ArrayList<CodeChunkBase> getChildren() {
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

    public abstract CodeChunkBase duplicateWithoutVariables();

    public abstract CodeChunkBase duplicateWithoutVariables(CodeChunkBase parent, Event holder);

    public void setHolder(Event holder) {
        this.holder = holder;
    }

    public Event getHolder() {
        return holder;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
