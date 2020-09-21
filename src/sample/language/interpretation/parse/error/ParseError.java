package sample.language.interpretation.parse.error;

import sample.language.interpretation.run.CodeState;

import java.io.File;
import java.util.ArrayList;

public class ParseError {

    // Fields
    private int lineNumber;
    private int errorNoticedAtScan;
    private String line;
    private CodeState parent;
    private String errorInfo;
    private String errorPrefix;
    private File file;

    // Constructors
    public ParseError(int lineNumber, int errorNoticedAtScan, String line, CodeState parent) {
        this.lineNumber = lineNumber;
        this.errorNoticedAtScan = errorNoticedAtScan;
        this.line = line;
        this.parent = parent;
    }
    public ParseError(int lineNumber, int errorNoticedAtScan, String line, String errorMessage, CodeState parent) {
        this.lineNumber = lineNumber;
        this.errorNoticedAtScan = errorNoticedAtScan;
        this.line = line;
        this.errorInfo = errorMessage;
        this.parent = parent;
    }
    public ParseError(int lineNumber, int errorNoticedAtScan, String line, String errorMessage, CodeState parent, File file) {
        this.lineNumber = lineNumber;
        this.errorNoticedAtScan = errorNoticedAtScan;
        this.line = line;
        this.errorInfo = errorMessage;
        this.parent = parent;
        this.file = file;
    }
    public ParseError(int lineNumber, String line, CodeState parent) {
        this.lineNumber = lineNumber;
        this.line = line;
        this.parent = parent;
    }
    public ParseError(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }
    public ParseError(int lineNumber, int errorNoticedAtScan) {
        this.lineNumber = lineNumber;
        this.errorNoticedAtScan = errorNoticedAtScan;
    }
    public ParseError(String line, CodeState parent) {
        this.line = line;
        this.parent = parent;
    }
    public ParseError(String line) {
        this.line = line;
    }
    public ParseError(CodeState parent) {
        this.parent = parent;
    }
    public ParseError(String line, String errorMessage) {
        this.line = line;
        this.errorInfo = errorMessage;
    }


    // Getters and Setters
    public int getLineNumber() {
        return lineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getErrorNoticedAtScan() {
        return errorNoticedAtScan;
    }
    public void setErrorNoticedAtScan(int errorNoticedAtScan) {
        this.errorNoticedAtScan = errorNoticedAtScan;
    }

    public String getLine() {
        return line;
    }
    public void setLine(String line) {
        this.line = line;
    }

    public CodeState getParent() {
        return parent;
    }
    public void setParent(CodeState parent) {
        this.parent = parent;
    }

    public String getErrorInfo() {
        return errorInfo;
    }
    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public File getFile() {
        return file;
    }

    public String getErrorPrefix() {
        return errorPrefix;
    }
    public void setErrorPrefix(String errorPrefix) {
        this.errorPrefix = errorPrefix;
    }


    // Methods
    /**
     * Simple algorithm that gathers a chain of parents from a {@link CodeState}
     * @return An {@link ArrayList} of all the parents of the ParseError's CodeState.
     */
    public ArrayList<CodeState> getAllParentStates() {
        if (parent == null) { return new ArrayList<>(); }
        ArrayList<CodeState> codeStates = new ArrayList<>();
        CodeState parent = this.parent;
        while (parent.getParent() != null) {
            parent = parent.getParent();
            codeStates.add(parent);
        }
        return codeStates;
    }

    public String createLongErrorMessage() {
        String topText = (errorPrefix != null ? errorInfo : "") + "Error: " + (errorInfo != null ? errorInfo : "un defined name");
        String lineText = "\tOn Line: " + line + (lineNumber > 0 ? "\n\t\tNumber: " + lineNumber + "" : "");
        String fileText = "\tIn File: " + (file != null ? file.getName() : "Undefined file.");
        return topText + "\n" + lineText + "\n" + fileText;
    }
    public String createShortErrorMessage() {
        String title = (errorPrefix != null ? errorInfo : "") + "Error: " + (errorInfo != null ? errorInfo : "nameless");
        String lineText = (file != null ? "(" + file.getName() + ") " : "") + line + "(" + lineNumber + ")";
        return title + "\n\t" + lineText;
    }

    public void sendError() {
        System.err.println(createLongErrorMessage());
    }
    public void print() {
        System.err.println(createLongErrorMessage());
    }

    public void sendShortError() {
        System.err.println(createShortErrorMessage());
    }
    public void printShort() {
        System.err.println(createShortErrorMessage());
    }

}
