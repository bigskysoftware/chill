package chill.script.tokenizer;

import java.util.HashMap;
import java.util.Map;

public class TokenType {

    // syntax
    public static final  TokenType LEFT_PAREN = new TokenType("LEFT_PAREN");
    public static final  TokenType RIGHT_PAREN = new TokenType("RIGHT_PAREN");
    public static final  TokenType LEFT_BRACE = new TokenType("LEFT_BRACE");
    public static final  TokenType RIGHT_BRACE = new TokenType("RIGHT_BRACE");
    public static final  TokenType LEFT_BRACKET = new TokenType("LEFT_BRACKET");
    public static final  TokenType RIGHT_BRACKET = new TokenType("RIGHT_BRACKET");
    public static final  TokenType COLON = new TokenType("COLON");
    public static final  TokenType COMMA = new TokenType("COMMA");
    public static final  TokenType DOT = new TokenType("DOT");
    public static final  TokenType MINUS = new TokenType("MINUS");
    public static final  TokenType PLUS = new TokenType("PLUS");
    public static final  TokenType SLASH = new TokenType("SLASH");
    public static final  TokenType STAR = new TokenType("STAR");
    public static final  TokenType BANG_EQUAL = new TokenType("BANG_EQUAL");
    public static final  TokenType EQUAL = new TokenType("EQUAL");
    public static final  TokenType EQUAL_EQUAL = new TokenType("EQUAL_EQUAL");
    public static final  TokenType GREATER = new TokenType("GREATER");
    public static final  TokenType GREATER_EQUAL = new TokenType("GREATER_EQUAL");
    public static final  TokenType LESS = new TokenType("LESS");
    public static final  TokenType LESS_EQUAL = new TokenType("LESS_EQUAL");
    public static final  TokenType DOLLAR = new TokenType("DOLLAR");
    public static final  TokenType SHARP = new TokenType("SHARP");
    public static final  TokenType SYMBOL = new TokenType("SYMBOL");
    public static final  TokenType STRING = new TokenType("STRING");
    public static final  TokenType NUMBER = new TokenType("NUMBER");
    public static final  TokenType ABSOLUTE_URL = new TokenType("ABSOLUTE_URL");
    public static final  TokenType PATH = new TokenType("PATH");

    // Template stuff
    public static final  TokenType TEMPLATE = new TokenType("TEMPLATE");

    // Special
    public static final  TokenType EOF = new TokenType("EOF");
    public static final  TokenType ERROR = new TokenType("ERROR");

    private final String name;

    public TokenType(String str) {
        this.name = str;
    }

    @Override
    public String toString() {
        return "TokenType{" +
                "name='" + name + '\'' +
                '}';
    }
}