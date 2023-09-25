package chill.db;

import chill.utils.NiceList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ChillQueryResult {
    private List<ChillField<?>> fields;
    private ResultSet results;

    public ChillQueryResult(List<ChillField<?>> fields, ResultSet results) throws SQLException {
        this.fields = fields;
        this.results = results;
    }

    public <T> T one(ChillField<T> id) {
        return null;
    }

    public <T> NiceList<T> get(ChillField<T> id) {
        return null;
    }
}
