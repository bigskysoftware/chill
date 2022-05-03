package chill.config;

import chill.db.ChillRecord;
import chill.env.ChillEnv;
import chill.env.ChillEnvVar;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.Properties;

public class ChillDataSource {

    private HikariDataSource ds;
    HikariConfig config;

    ChillDataSource() {
        ChillEnvVar<String> jsonConfig = ChillEnv.DB_CONNECTION_POOL_CONFIG;
        if (jsonConfig.isSet()) {

            Map props = new Gson().fromJson(jsonConfig.get(), Map.class);
            Properties properties = new Properties();
            properties.putAll(props);
            config = new HikariConfig(properties);
        }

        ds = (config != null ? new HikariDataSource(config) : new HikariDataSource());
        String dbUrl = ChillEnv.DB_URL.require();
        ds.setJdbcUrl(dbUrl);

        if (ChillEnv.DB_DRIVER.isSet())
            ds.setDriverClassName(ChillEnv.DB_DRIVER.get());

        if (ChillEnv.DB_USERNAME.isSet())
            ds.setUsername(ChillEnv.DB_USERNAME.get());

        if (ChillEnv.DB_PASSWORD.isSet())
            ds.setUsername(ChillEnv.DB_PASSWORD.get());
    }

    public static void init() {
        var chillDS = new ChillDataSource();
        ChillRecord.connectionSource = () -> chillDS.ds.getConnection();
    }
}
