package com.municipal.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        
        if (url == null || username == null || password == null) {
            throw new IllegalStateException("DB_URL, DB_USERNAME ou DB_PASSWORD manquant");
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }
}
