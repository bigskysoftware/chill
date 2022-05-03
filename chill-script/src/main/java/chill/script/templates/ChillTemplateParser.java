package chill.script.templates;

import chill.script.expressions.*;
import chill.script.parser.ChillScriptParseException;
import chill.script.parser.ChillScriptParser;
import chill.script.parser.ParseElement;
import chill.script.templates.commands.*;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.tokenizer.Tokenizer;
import chill.script.parser.ErrorType;

import java.util.LinkedList;

public class ChillTemplateParser extends ChillScriptParser {

    ChillTemplate currentTemplate;

    @Override
    protected Tokenizer getTokenizer(String src) {
        return new Tokenizer(src, Tokenizer.Mode.TEMPLATE);
    }

    public ChillTemplate parseTemplate(String src) {
        initTokens(src);
        Token start = currentToken();
        currentTemplate = new ChillTemplate();
        currentTemplate.setStart(start);
        var body = new LinkedList<ChillTemplateCommand>();
        while (moreTokens()) {
            body.add(parseTemplateElement(true));
        }
        currentTemplate.setBody(body);
        currentTemplate.setEnd(lastMatch());
        if (currentTemplate.isValid()) {
            return currentTemplate;
        } else {
            throw new ChillScriptParseException(currentTemplate);
        }
    }

    private ChillTemplateCommand parseTemplateElement() {
        return parseTemplateElement(false);
    }

    private ChillTemplateCommand parseTemplateElement(boolean topLevel) {

        var templateFragment = parseTemplateFragment();
        if (templateFragment != null) {
            return templateFragment;
        }

        var ifCmd = parseIfCommand(false);
        if (ifCmd != null) {
            return ifCmd;
        }

        var forCmd = parseForCommand();
        if (forCmd != null) {
            return forCmd;
        }

        var includeCmd = parseIncludeCommand();
        if (includeCmd != null) {
            return includeCmd;
        }

        var contentCmd = parseContentCommand();
        if (contentCmd != null) {
            return contentCmd;
        }

        var layoutCommand = parseLayoutCommand();
        if (layoutCommand != null) {
            if (topLevel) {
                currentTemplate.setLayout((ChillTemplateLayoutCommand) layoutCommand);
            } else {
                layoutCommand.addError(lastMatch(), "Layouts must be at the top level of the template");
            }
            return layoutCommand;
        }

        ChillTemplateCommand fragmentCommand = parseFragmentCommand();
        if (fragmentCommand != null) {
            return fragmentCommand;
        }

        ChillTemplateCommand expressionElement = parseExpressionElement();
        if (expressionElement != null) {
            return expressionElement;
        }

        ErrorTemplateCommand errorTemplate = new ErrorTemplateCommand("Syntax Error: " + currentToken().getStringValue(), currentToken());
        // scan to the next template element
        while (moreTokens() && !currentToken().getType().equals(TokenType.TEMPLATE)) {
            consumeToken();
        }
        return errorTemplate;
    }

    private ChillTemplateContentElement parseTemplateFragment() {
        if (match(TokenType.TEMPLATE)) {
            Token template = consumeToken();
            return new ChillTemplateContentElement(template);
        }
        return null;
    }

    private ChillTemplateCommand parseExpressionElement() {
        if (match(TokenType.DOLLAR)) {
            Token dollar = consumeToken();
            var exprElt = new ChillTemplateExpressionElement();
            exprElt.setStart(dollar);

            require(TokenType.LEFT_BRACE, exprElt, ErrorType.UNEXPECTED_TOKEN.toString());

            exprElt.setExpr(parse("expression"));

            if (matchAndConsume("if")) {
                exprElt.setConditional(parse("expression"));
            }

            boolean hasBody = matchAndConsume("do");

            require(TokenType.RIGHT_BRACE, exprElt, ErrorType.UNEXPECTED_TOKEN.toString());

            exprElt.setEnd(lastMatch());

            if (hasBody) {
                var body = new LinkedList<ChillTemplateCommand>();
                while (moreTokens()) {
                    if (matchEndExpr(exprElt)) {
                        break;
                    }
                    ChillTemplateCommand e = parseTemplateElement();
                    body.add(e);
                }
                exprElt.setBody(body);
            }

            return exprElt;
        }
        return null;
    }

    private boolean matchEndExpr(ParseElement elt) {
        if (matchSequence(TokenType.DOLLAR, TokenType.LEFT_BRACE, "end")) {
            matchAndConsume(TokenType.DOLLAR);
            matchAndConsume(TokenType.LEFT_BRACE);
            matchAndConsume("end");
            require(TokenType.RIGHT_BRACE, elt, "end must be by itself");
            return true;
        }
        return false;
    }


    private ChillTemplateCommand parseIfCommand(boolean allowElseIf) {
        if (matchCommand("if") || (allowElseIf && matchCommand("elseif"))) {
            var ifCmd = new ChillTemplateIfCommand();
            Token command = consumeCommand();
            ifCmd.setStart(command);
            Expression testValue = parse("expression");
            ifCmd.setExpr(testValue);
            var body = new LinkedList<ChillTemplateCommand>();
            while (moreTokens()) {
                if (matchCommand("end") || matchCommand("else") || matchCommand("elseif")) {
                    break;
                }
                body.add(parseTemplateElement());
            }
            ifCmd.setBody(body);

            if (matchCommand("end")) {
                consumeCommand();
            } else if (matchCommand("else")) {
                ifCmd.setElseCmd(parseElseCommand());
            } else if (matchCommand("elseif")) {
                ifCmd.setElseCmd(parseIfCommand(true));
            }

            ifCmd.setEnd(lastMatch());
            return ifCmd;
        }
        return null;
    }

    private ChillTemplateCommand parseFragmentCommand() {
        if (matchCommand("fragment")) {
            var fragmentCommand = new ChillTemplateFragmentCommand();
            fragmentCommand.setStart(consumeCommand());

            fragmentCommand.setName(require(TokenType.SYMBOL, fragmentCommand, "Fragments must have a name"));

            var body = new LinkedList<ChillTemplateCommand>();
            while (moreTokens()) {
                if (matchCommand("end")) {
                    break;
                }
                body.add(parseTemplateElement());
            }
            fragmentCommand.setBody(body);
            currentTemplate.addFragment(fragmentCommand);

            if (matchCommand("end")) { // TODO require
                consumeCommand();
            }
            fragmentCommand.setEnd(lastMatch());
            return fragmentCommand;
        }
        return null;
    }


    private ChillTemplateCommand parseForCommand() {
        if (matchCommand("for")) {
            var forCmd = new ChillTemplateForCommand();
            Token command = consumeCommand();

            if (match(TokenType.SYMBOL)) {
                forCmd.setIdentifier(consumeToken());
            } else {
                forCmd.addError(ErrorType.UNEXPECTED_TOKEN, consumeToken());
            }

            if (match("in")) {
                consumeToken();
                Expression iteratorExpr = parse("expression");
                forCmd.setExpr(iteratorExpr);
            } else {
                forCmd.addError(ErrorType.UNEXPECTED_TOKEN, consumeToken());
            }

            if (match("index")) {
                consumeToken();
                if (match(TokenType.SYMBOL)) {
                    forCmd.setIdentifier(consumeToken());
                } else {
                    forCmd.addError(ErrorType.UNEXPECTED_TOKEN, consumeToken());
                }
            }

            forCmd.setBody(parseTemplateCommandList());

            forCmd.setStart(command);
            forCmd.setEnd(lastMatch());
            return forCmd;
        }
        return null;
    }

    private ChillTemplateCommand parseIncludeCommand() {
        if (matchCommand("include")) {
            var includeCmd = new ChillTemplateIncludeCommand();
            includeCmd.setStart(consumeCommand());
            includeCmd.setExpr((PathLiteralExpression) requireExpression(includeCmd, "pathLiteral"));
            includeCmd.setEnd(lastMatch());
            return includeCmd;
        }
        return null;
    }

    private ChillTemplateCommand parseContentCommand() {
        if (matchCommand("content")) {
            var includeCmd = new ChillTemplateContentCommand();
            includeCmd.setStart(consumeCommand());
            includeCmd.setEnd(lastMatch());
            return includeCmd;
        }
        return null;
    }

    private ChillTemplateCommand parseLayoutCommand() {
        if (matchCommand("layout")) {
            var includeCmd = new ChillTemplateLayoutCommand();
            includeCmd.setStart(consumeCommand());
            includeCmd.setExpr((PathLiteralExpression) requireExpression(includeCmd, "pathLiteral"));
            includeCmd.setEnd(lastMatch());
            return includeCmd;
        }
        return null;
    }

    private LinkedList<ChillTemplateCommand> parseTemplateCommandList() {
        var body = new LinkedList<ChillTemplateCommand>();
        while (moreTokens()) {
            if (matchCommand("end")) {
                break;
            }
            body.add(parseTemplateElement());
        }
        if (matchCommand("end")) {
            consumeCommand();
        }
        return body;
    }

    private ChillTemplateElseCommand parseElseCommand() {
        if (matchCommand("else")) {
            var elseCmd = new ChillTemplateElseCommand();
            Token command = consumeCommand();
            elseCmd.setStart(command);
            elseCmd.setBody(parseTemplateCommandList());
            elseCmd.setEnd(lastMatch());
            return elseCmd;
        } else {
            return null;
        }
    }

    //===============================================
    // Tokenizer Helpers
    //===============================================

    private boolean matchCommand(String cmd) {
        return match(TokenType.SHARP) && matchNext(cmd);
    }

    private Token consumeCommand() {
        consumeToken(); // sharp
        return consumeToken(); // identifier
    }
}
