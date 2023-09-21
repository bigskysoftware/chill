package chill.script.parser;

import chill.script.expressions.Expression;

public interface ExpressionParser {
    Expression parseExpression(ChillScriptParser parser);
}
