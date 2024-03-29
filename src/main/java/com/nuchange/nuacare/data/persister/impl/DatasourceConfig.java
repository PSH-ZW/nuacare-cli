package com.nuchange.nuacare.data.persister.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatasourceConfig {

    @Bean(name = "openmrsJdbcTemplate")
    public JdbcTemplate openMrsJdbcTemplate(
            @Qualifier("openmrsDatasource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "analyticsJdbcTemplate")
    public JdbcTemplate analyticsJdbcTemplate(
            @Qualifier("analyticsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
