package chill.script.parser;

import chill.script.pattern.Pattern;

public interface PatternParser {
    Pattern parsePattern(ChillScriptParser parser);
}
