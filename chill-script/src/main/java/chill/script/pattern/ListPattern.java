package chill.script.pattern;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;
import chill.utils.NiceList;

import java.util.List;

public class ListPattern extends Pattern {
    final NiceList<Pattern> patterns = new NiceList<>();

    public ListPattern() {}

    public NiceList<Pattern> getPatterns() {
        return patterns;
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        if (value instanceof Object[] objArray) {
            value = List.of(objArray);
        }

        if (value instanceof Iterable iter) {
            var items = iter.iterator();
            var patterns = this.patterns.iterator();

            while (patterns.hasNext()) {
                var pattern = patterns.next();
                if (pattern instanceof SpreadPattern spread) {
                    if (items.hasNext()) {
                        if (spread.getCount() == null) {
                            assert !patterns.hasNext(); // spread w/out count must be last
                            break;
                        }

                        var count = spread.getCount().evaluate(runtime);
                        if (!(count instanceof Number n)) {
                            throw new RuntimeException("count must be a number: " + spread.getCount().getStart());
                        }

                        for (int i = 0; i < n.intValue(); i++) {
                            if (items.hasNext()) {
                                pattern.bind(runtime, items.next());
                            } else {
                                throw new RuntimeException("not enough items in spread: " + spread.getStart());
                            }
                        }
                    } else { /* everything's okay */ }
                } else {
                    if (items.hasNext()) {
                        pattern.bind(runtime, items.next());
                    } else {
                        throw new RuntimeException("not enough items in list: " + pattern.getStart());
                    }
                }
            }
        } else {
            throw new PatternBindingException("do not know how to bind " + value + " to " + this);
        }
    }

    public static ListPattern parse(ChillScriptParser parser) {
        if (!parser.match(TokenType.LEFT_BRACKET)) return null;

        ListPattern rv = new ListPattern();
        rv.setStart(parser.consumeToken());

        while (parser.moreTokens() && !parser.match(TokenType.RIGHT_BRACKET)) {
            Pattern pattern;
            if ((pattern = SpreadPattern.parse(parser)) != null) {
                rv.patterns.add(pattern);
                rv.addChild(pattern);
            } else if ((pattern = Pattern.parsePattern(parser)) != null) {
                rv.patterns.add(pattern);
                rv.addChild(pattern);
            } else {
                rv.addError(parser.consumeToken(), "Expected pattern");
            }
            if (!parser.matchAndConsume(TokenType.COMMA)) {
                break;
            }
        }

        rv.setEnd(parser.require(TokenType.RIGHT_BRACKET, rv, "Expected ']'"));

        return rv;
    }

    static class SpreadPattern extends Pattern {
        private Expression count;

        public SpreadPattern() {}

        public Expression getCount() {
            return count;
        }

        public void setCount(Expression count) {
            this.count = count;
        }

        static SpreadPattern parse(ChillScriptParser parser) {
            if (parser.match(TokenType.DOT_DOT)) {
                SpreadPattern rv = new SpreadPattern();
                rv.setStart(parser.consumeToken());

                if (parser.matchAndConsume(TokenType.COMMA)) {
                    if (!parser.match(TokenType.RIGHT_BRACKET)) {
                        rv.addError(rv.getStart(), ".. cannot be used in the middle of a list without a count (use _ instead)");
                    } else {
                        parser.produceToken();
                    }
                    rv.setEnd(parser.lastMatch());
                } else {
                    rv.setCount(parser.parse("expression"));
                }

                return rv;
            } else {
                return null;
            }
        }
    }
}
