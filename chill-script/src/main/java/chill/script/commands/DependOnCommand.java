package chill.script.commands;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.utils.NiceList;

import java.util.List;

public class DependOnCommand extends Command {
    private String uri;
    private String name;
    private String version;
    private List<Command> selectors = new NiceList<>();

    public DependOnCommand() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Command> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<Command> selectors) {
        this.selectors = selectors;
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        if (!runtime.isGlobalScope()) {
            throw new RuntimeException("imports can only occur at the global scope");
        }
    }

    public static DependOnCommand parse(ChillScriptParser parser) {
        if (parser.matchAndConsumeSequence("depend", "on")) {
            DependOnCommand command = new DependOnCommand();
            command.setStart(parser.lastMatch());

            NiceList<String> links = new NiceList<>();
            do {
                Token ident = parser.require(TokenType.SYMBOL, command, "Expected import uri part");
                links.add(ident.getStringValue());
            } while (parser.matchAndConsume(TokenType.DOT));
            command.setUri(String.join(".", links));

            parser.require(TokenType.COLON, command, "Expected ':' between import uri and module name");

            NiceList<String> nameLinks = new NiceList<>();
            do {
                Token name = parser.require(TokenType.SYMBOL, command, "Expected module name after import uri");
                nameLinks.add(name.getStringValue());
            } while (parser.matchAndConsume(TokenType.MINUS));
            String name = String.join("-", nameLinks);
            command.setName(name);

            parser.require(TokenType.COLON, command, "Expected ':' between module name and version");

            NiceList<String> versionLinks = new NiceList<>();
            do {
                Token link = parser.require(TokenType.NUMBER, command, "Expected version number");
                versionLinks.add(link.getStringValue());
            } while (parser.matchAndConsume(TokenType.DOT));

            String version = String.join(".", versionLinks);
            if (parser.matchAndConsume(TokenType.MINUS)) {
                Token tag = parser.require(TokenType.SYMBOL, command, "Expected version tag");
                version = version + "-" + tag;
            }
            command.setVersion(version);


            while (parser.matchAndConsume("and")) {
                Command selector;
                if ((selector = FromCommand.parse(parser)) != null) {
                    command.selectors.add(selector);
                    command.addChild(selector);
                } else if ((selector = UseCommand.parse(parser)) != null) {
                    command.selectors.add(selector);
                    command.addChild(selector);
                } else {
                    break;
                }
            }

            if (!command.selectors.isEmpty()) {
                parser.require("end", command, "Expected 'end' after use commands");
            }

            command.setEnd(parser.lastMatch());

            return command;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ImportCommand(" + uri + ":" + name + ":" + version + ")";
    }

    public static class UseCommand extends Command {
        private List<String> links = new NiceList<>();

        public UseCommand() {
        }

        public List<String> getLinks() {
            return links;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public void addLink(String link) {
            links.add(link);
        }

        @Override
        public void execute(ChillScriptRuntime runtime) {
        }

        /*
        use com.google.gson.Gson
         */
        public static UseCommand parse(ChillScriptParser parser) {
            if (parser.matchAndConsume("use")) {
                UseCommand command = new UseCommand();
                command.setStart(parser.lastMatch());

                do {
                    StringBuilder link = new StringBuilder();

                    do {
                        if (!link.isEmpty()) {
                            link.append(".");
                        }
                        Token ident = parser.require(TokenType.SYMBOL, command, "Expected import uri part");
                        link.append(ident.getStringValue());

                    } while (parser.matchAndConsume(TokenType.DOT));

                    command.addLink(link.toString());
                } while (parser.match(TokenType.COMMA));

                command.setEnd(parser.lastMatch());
                return command;
            }
            return null;
        }
    }

    public static class FromCommand extends Command {
        final List<String> symbols = new NiceList<>();

        private String uri;

        public FromCommand() {
        }

        public List<String> getSymbols() {
            return symbols;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        @Override
        public void execute(ChillScriptRuntime runtime) {
        }

        /*
        from spark import *
         */
        public static FromCommand parse(ChillScriptParser parser) {
            if (parser.matchAndConsume("from")) {
                FromCommand command = new FromCommand();
                command.setStart(parser.lastMatch());

                NiceList<String> links = new NiceList<>();
                do {
                    Token ident = parser.require(TokenType.SYMBOL, command, "Expected import uri part");
                    links.add(ident.getStringValue());
                } while (parser.matchAndConsume(TokenType.DOT));
                command.setUri(String.join(".", links));

                if (!parser.match("import", "use")) {
                    command.addError(parser.consumeToken(), "Expected 'import' or 'use' after 'from' import uri");
                } else {
                    parser.consumeToken();
                }

                if (parser.matchAndConsume(TokenType.STAR)) {
                    command.symbols.add("*");
                } else {
                    do {
                        Token ident = parser.require(TokenType.SYMBOL, command, "Expected item name");
                        command.symbols.add(ident.getStringValue());
                    } while (parser.matchAndConsume(TokenType.COMMA));
                }

                command.setEnd(parser.lastMatch());
                return command;
            }
            return null;
        }
    }
}
