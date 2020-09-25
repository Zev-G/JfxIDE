package tmw.me.com.betterfx;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CommandConsole extends Console {

    private String cmdPrefix = "/";
    private boolean excludeNonPrefix = true;
    private Text startWithMessageInError = getDefaultText();
    private final ArrayList<Text> errorMessage = new ArrayList<>();
    private final ArrayList<Text> helpMessage = new ArrayList<>();

    private final ArrayList<ConsoleEvent> errorEvent = new ArrayList<>();
    private final ArrayList<ConsoleEvent> helpEvent = new ArrayList<>();

    private final HashMap<String, ConsoleEvent> commandMap = new HashMap<>();

    public CommandConsole(String cmdPrefix) {
        this.cmdPrefix = cmdPrefix;
        init();
    }
    public CommandConsole() {
        init();
    }

    private void init() {
        getOnUserInput().clear();
        addCommand("help", ((inputtedString, eventConsole) -> help(inputtedString)));
        protectedOnUserInput.add(((inputtedString, eventConsole) -> passCommand(inputtedString)));
    }

    public static CommandConsole generateForJava() {

        CommandConsole console = new CommandConsole();
        System.setErr(console.generateNewPrintStream(Color.RED));
        System.setOut(console.getStream());

        return console;
    }

    public void addExampleCommands() {
        addTextsToErrorMessage(getDefaultText(" <-- Invalid command, run ", Color.RED), getDefaultText("" + cmdPrefix + "help ", Color.LIGHTSEAGREEN), getDefaultText("for a list of valid commands.", Color.RED));
        addTextsToHelpMessage(getDefaultText("-----Help-----", Color.ROYALBLUE), getDefaultText("\n" + cmdPrefix + "help", Color.LIGHTSEAGREEN), getDefaultText("-> Displays this.", Color.DARKSEAGREEN),
                getDefaultText("\n" + cmdPrefix + "random", Color.LIGHTSEAGREEN), getDefaultText("-> Gives you a random number!", Color.DARKSEAGREEN), getDefaultText("\n" + cmdPrefix + "CreateCommand NAME_HERE -> MESSAGE_HERE", Color.LIGHTSEAGREEN),
                getDefaultText("-> Creates a new command.", Color.DARKSEAGREEN), getDefaultText("\n" + cmdPrefix + "RemoveCommand NAME_HERE", Color.LIGHTSEAGREEN), getDefaultText("-> Removes a command.", Color.DARKSEAGREEN),
                getDefaultText("\n" + cmdPrefix + "ListCommands", Color.LIGHTSEAGREEN), getDefaultText("-> Lists commands.", Color.DARKSEAGREEN));
        addCommand("random", (inputtedString, eventConsole) -> addTexts(false, getDefaultText("\nGenerated: " + Math.random() * 100, Color.MEDIUMPURPLE)));
        addCommand("CreateCommand", (inputtedString, eventConsole) -> {
            String[] spaceSplit = inputtedString.split(" ");
            if (spaceSplit.length > 3) {
                StringBuilder finalString = new StringBuilder();
                int loops = 0;
                for (String s : spaceSplit) {
                    loops++;
                    if (loops > 3) {
                        finalString.append(s).append(" ");
                    }
                }
                if (commandMap.containsKey(spaceSplit[1])) {
                    addTexts(false, genText("&y\nCommand already exists, remove it with &w/RemoveCommand " + spaceSplit[0]));
//                    addTexts(false, getDefaultText("\nCommand already exists, remove it with ", Color.RED), getDefaultText("" + cmdPrefix + "RemoveCommand " + spaceSplit[1], Color.LIGHTSEAGREEN));
                } else {
                    addCommand(spaceSplit[1], (inputtedString1, eventConsole1) -> addTexts(false, genText("\n&b" + finalString.toString())));
                    addTexts(false, getDefaultText("\nCreated command!", Color.MEDIUMPURPLE));
                }
            } else {
                addTexts(false, getDefaultText("\nIncomplete command. Valid command format: ", Color.RED), getDefaultText(" " + cmdPrefix + "CreateCommand NAME_HERE -> MESSAGE_HERE", Color.LIGHTSEAGREEN));
            }
        });
        addCommand("RemoveCommand", (inputtedString, eventConsole) -> {
            String[] spaceSplit = inputtedString.split(" ");
            if (spaceSplit.length > 1) {
                if (!commandMap.containsKey(spaceSplit[1])) {
                    addTexts(false, getDefaultText("\nCommand doesn't exists, create it with ", Color.RED), getDefaultText("" + cmdPrefix + "CreateCommand " + spaceSplit[1] + " -> MESSAGE_HERE", Color.LIGHTSEAGREEN));
                } else {
                    commandMap.remove(spaceSplit[1]);
                    addTexts(false, getDefaultText("\nRemoved command!", Color.MEDIUMPURPLE));
                }
            } else {
                addTexts(false, getDefaultText("\nIncomplete command. Valid command format: ", Color.RED), getDefaultText(" " + cmdPrefix + "RemoveCommand NAME_HERE", Color.LIGHTSEAGREEN));
            }
        });
        addCommand("ListCommands", (inputtedString, eventConsole) -> {
            addTexts(false, getDefaultText("\n---Commands---", Color.LIGHTSEAGREEN));
            int loopTimes = 0;
            for (String command : commandMap.keySet()) {
                loopTimes++;
                addTexts(false, getDefaultText("\n" + loopTimes + ": ", Color.BLACK), getDefaultText(command, Color.DARKSEAGREEN));
            }
            addTexts(false, getDefaultText("\n---End---", Color.LIGHTSEAGREEN));
        });
    }




    public void setCommandPrefix(String prefix) {
        this.cmdPrefix = prefix;
    }
    public String getCommandPrefix() {
        return this.cmdPrefix;
    }

    public ArrayList<Text> getErrorMessage() { return this.errorMessage; }
    public void setErrorMessage(Text... texts) { errorMessage.clear(); errorMessage.addAll(Arrays.asList(texts)); }
    public void addTextsToErrorMessage(Text... texts) { errorMessage.addAll(Arrays.asList(texts)); }
    public ArrayList<ConsoleEvent> getOnErrorMessage() {
        return errorEvent;
    }
    public void setOnErrorMessage(ConsoleEvent event) {
        errorEvent.clear();
        errorEvent.add(event);
    }
    public void fireErrorSent(String text) {
        for (ConsoleEvent error : errorEvent) {
            error.onInput(text, this);
        }
    }
    public Text getStaringErrorWithMessage() {
        return startWithMessageInError;
    }
    public void setStartWithMessageInError(Text startWithMessageInError) {
        this.startWithMessageInError = startWithMessageInError;
    }

    public ArrayList<Text> getHelpMessage() { return helpMessage; }
    public void setHelpMessage(Text... texts) { helpMessage.clear(); helpMessage.addAll(Arrays.asList(texts)); }
    public void addTextsToHelpMessage(Text... texts) { helpMessage.addAll(Arrays.asList(texts)); }
    public ArrayList<ConsoleEvent> getOnHelpMessage() {
        return helpEvent;
    }
    public void setOnErrorMessageSent(ConsoleEvent event) {
        helpEvent.clear();
        helpEvent.add(event);
    }
    public void fireHelpSent(String text) {
        for (ConsoleEvent error : helpEvent) {
            error.onInput(text, this);
        }
    }

    public boolean isNonPrefixExcluded() {
        return excludeNonPrefix;
    }
    public void setExcludeNonPrefix(boolean input) {
        this.excludeNonPrefix = input;
    }

    public void addCommand(String command, ConsoleEvent run) {
        commandMap.put(command, run);
    }
    public void removeCommand(String command) {
        if (command.equals("help")) { return; }
        commandMap.remove(command);
    }
    public HashMap<String, ConsoleEvent> getCommandMap() {
        return commandMap;
    }

    public void passCommand(String command) {
        if (command.length() == 0) return;
        getConsoleInput().setText("");
        simulateMessageSend(command);
        if (!command.startsWith(cmdPrefix)) {
            if (!excludeNonPrefix) {
                error(command);
            }
            return;
        }
        String subCommand = command.substring(1);
        for (String key : commandMap.keySet()) {
            if (subCommand.startsWith(key)) {
                commandMap.get(key).onInput(subCommand, this);
                return;
            }
        }
        error(command);
    }
    public void error(String command) {
        fireErrorSent(command);
        if (startWithMessageInError != null) {
            startWithMessageInError.setText(command);
            addText(duplicateText(startWithMessageInError), true);
        } else {
            addText("", true);
        }
        ArrayList<Text> errorMessage = new ArrayList<>();
        this.errorMessage.forEach(text -> errorMessage.add(duplicateText(text)));
        addTexts(false, errorMessage.toArray(new Text[0]));
    }
    public void help(String command) {
        fireHelpSent(command);
        addText("", true);
        ArrayList<Text> helpMessage = new ArrayList<>();
        this.helpMessage.forEach(text -> helpMessage.add(duplicateText(text)));
        addTexts(false, helpMessage.toArray(new Text[0]));
    }

}
