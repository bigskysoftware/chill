package chill.script.parser;

import chill.script.expressions.Expression;
import chill.utils.Pair;

public interface IndirectExpressionParser {
    Expression parse(Pair<ChillScriptParser, Expression> parserAndRoot);
}
