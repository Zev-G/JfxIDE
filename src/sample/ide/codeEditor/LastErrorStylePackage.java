package sample.ide.codeEditor;

class LastErrorStylePackage {

    private final int start;
    private final int end;

    LastErrorStylePackage(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getEnd() {
        return end;
    }
    public int getStart() {
        return start;
    }
}
