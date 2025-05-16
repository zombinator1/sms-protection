package bzabek.sms.protection.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class JooqConfig {

    @Bean
    public Connection connection(DatabaseProperties databaseProperties) {
        try {
            return DriverManager.getConnection(
                    databaseProperties.getUrl(),
                    databaseProperties.getUsername(),
                    databaseProperties.getPassword()
            );
        } catch (SQLException ex) {
            String msg = String.format(
                    "Failed to connect to database during application start, url [%s], username [%s]",
                    databaseProperties.getUrl(), databaseProperties.getUsername());
            throw new RuntimeException(msg, ex);
        }
    }

    @Bean
    public DSLContext dslContext(Connection connection) {
        return DSL.using(connection, SQLDialect.POSTGRES);
    }
}
