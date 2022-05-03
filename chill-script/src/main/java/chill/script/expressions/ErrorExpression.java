package chill.script.expressions;

import chill.script.tokenizer.Token;

public class ErrorExpression extends Expression {
    public ErrorExpression(String message, Token token) {
        addError(token, message);
    }
}
