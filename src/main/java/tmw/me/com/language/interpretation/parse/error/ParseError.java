package tmw.me.com.language.interpretation.parse.error;


import java.io.File;
import java.util.Arrays;

public class ParseError {

    // Fields
    private int lineNumber;
    private int errorNoticedAtScan;
    private String line;
    private String errorInfo;
    private String errorPrefix;
    private File file;

    // Constructors
    public ParseError(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public ParseError(int lineNumber, int errorNoticedAtScan) {
        this.lineNumber = lineNumber;
        this.errorNoticedAtScan = errorNoticedAtScan;
    }

    public ParseError(String line) {
        this.line = line;
    }

    public ParseError(String line, String errorMessage) {
        this.line = line;
        this.errorInfo = errorMessage;
    }

    public ParseError(int lineNumber, String line, String errorPrefix, File file) {
        this.lineNumber = lineNumber;
        this.line = line;
        this.errorPrefix = errorPrefix;
        this.file = file;
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


    public String createLongErrorMessage() {
        String topText = (errorPrefix != null ? errorPrefix + " " : "") + "Error: " + (errorInfo != null ? errorInfo : "");
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
        System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }

    public void sendShortError() {
        System.err.println(createShortErrorMessage());
    }

    public void printShort() {
        System.err.println(createShortErrorMessage());
    }

}
