package chill.script.parser;

import chill.script.commands.Command;

public interface CommandParser {
    Command parse(ChillScriptParser parser);
}
