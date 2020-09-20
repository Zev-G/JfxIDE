package sample.language.interpretation.parse;

import sample.language.interpretation.parse.error.ParseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsedPackage<T> {

    private T value;
    private List<ParseError> parseErrors;

    // Constructors
    public ParsedPackage(T value, List<ParseError> parseErrors) {
        this.value = value;
        this.parseErrors = parseErrors;
    }
    public ParsedPackage(T value, ParseError... parseErrors) {
        this.value = value;
        this.parseErrors = Arrays.asList(parseErrors);
    }
    public ParsedPackage(T value) {
        this.value = value;
    }

    // Getters and Setters
    public void setValue(T value) {
        this.value = value;
    }
    public T getValue() {
        return value;
    }

    public List<ParseError> getParseErrors() {
        if (parseErrors == null) {
            parseErrors = new ArrayList<>();
        }
        return parseErrors;
    }
    public void setParseErrors(List<ParseError> parseErrors) {
        this.parseErrors = parseErrors;
    }
}
