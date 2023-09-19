package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.Sqlite;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.utils.NiceList;

import java.sql.SQLException;
import java.util.List;

public class SqlExpression extends Expression {
    private Expression targetDb;
    private NiceList<Statement> statements = new NiceList<>();

    public SqlExpression() {
    }

    public Expression getTargetDb() {
        return targetDb;
    }

    public void setTargetDb(Expression targetDb) {
        this.targetDb = targetDb;
    }

    public NiceList<Statement> getStatements() {
        return statements;
    }

    public void setStatements(NiceList<Statement> statements) {
        this.statements = statements;
    }

    public void addStatement(Statement statement) {
        statements.add(addChild(statement));
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        String database;
        if (getTargetDb() != null) {
            database = (String) getTargetDb().evaluate(runtime);
        } else {
            database = "default"; // todo: make this configurable
        }

        var sqlite = runtime.getMetaData(Sqlite.RT_KEY);

        try (var connection = sqlite.getConnection(database)) {
            final List<String> queries = new NiceList<>();
            final List<List<Object>> valueSets = new NiceList<>();

            for (Statement statement : statements) {
                StringBuilder queryBuilder = new StringBuilder();
                NiceList<Object> values = new NiceList<>();
                for (int i = 0; i < statement.segments.size(); i++) {
                    queryBuilder.append(statement.segments.get(i));
                    if (i < statement.values.size()) {
                        var value = statement.values.get(i).evaluate(runtime);
                        queryBuilder.append("?");
                        values.add(value);
                    }
                }

                queries.add(queryBuilder.toString());
                valueSets.add(values);
            }

            List<Object> results = new NiceList<>();
            for (int i = 0; i < queries.size(); i++) {
                var query = queries.get(i);
                var valueSet = valueSets.get(i);

                try (var stmt = connection.prepareStatement(query)) {
                    for (int j = 0; j < valueSet.size(); j++) {
                        stmt.setObject(j + 1, valueSet.get(j));
                    }

                    if (query.startsWith("SELECT") || query.startsWith("select")) {
                        var set = stmt.executeQuery();
                        List<List<Object>> values = new NiceList<>();
                        while (set.next()) {
                            List<Object> row = new NiceList<>();
                            for (int j = 1; j <= set.getMetaData().getColumnCount(); j++) {
                                row.add(set.getObject(j));
                            }
                            values.add(row);
                        }
                        results.add(values);
                    } else {
                        var count = stmt.executeUpdate();
                        results.add(count);
                    }
                }
            }

            if (results.size() == 1) {
                return results.get(0);
            } else {
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static SqlExpression parse(ChillScriptParser parser) {
        if (parser.matchAndConsume("sql")) {
            SqlExpression expression = new SqlExpression();
            expression.setStart(parser.lastMatch());

            if (parser.matchAndConsume("on")) {
                expression.setTargetDb(parser.parse("expression"));
            }

            do {
                var stmt = Statement.parse(parser);
                expression.addStatement(stmt);
                if (!parser.matchAndConsume(TokenType.SEMICOLON)) {
                    break;
                }
            } while (parser.moreTokens() && !parser.match("end"));

            parser.require("end", expression, "Expected 'end' after sql expression");
            expression.setEnd(parser.lastMatch());

            return expression;
        } else {
            return null;
        }
    }

    public static class Statement extends Expression {
        final List<String> segments = new NiceList<>();
        final List<Expression> values = new NiceList<>();

        public Statement() {
        }

        public void addSegment(String segment) {
            segments.add(segment);
        }

        public void addValue(Expression value) {
            values.add(value);
        }

        public static Statement parse(ChillScriptParser parser) {
            Statement statement = new Statement();
            statement.setStart(parser.lastMatch());

            Token past = parser.currentToken();
            while (parser.moreTokens() && !parser.match(TokenType.SEMICOLON) && !parser.match("end")) {
                if (parser.matchAndConsume(TokenType.DOLLAR)) {
                    String segment = parser.getSubstringBetween(past, parser.lastMatch());
                    statement.addSegment(segment);

                    Expression expr;
                    if (parser.matchAndConsume(TokenType.LEFT_BRACE)) {
                        expr = parser.parse("expression");
                        parser.require(TokenType.RIGHT_BRACE, statement, "Expected '}' after expression");
                    } else {
                        expr = parser.parse("identifier");
                    }
                    statement.addValue(expr);
                    past = parser.currentToken();
                } else {
                    parser.consumeToken();
                }
            }
            String segment = parser.getSubstringBetween(past, parser.currentToken());
            statement.addSegment(segment);
            statement.setEnd(parser.lastMatch());

            return statement;
        }
    }
}
