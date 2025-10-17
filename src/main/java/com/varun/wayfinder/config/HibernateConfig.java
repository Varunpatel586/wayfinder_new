package com.varun.wayfinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.JpaVendorAdapter;

import java.util.Properties;

@Configuration
public class HibernateConfig {
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        // explicitly tell Spring which dialect to use
        adapter.setDatabasePlatform("com.varun.wayfinder.config.SQLiteDialect");
        return adapter;
    }

    @Bean
    public Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", "com.varun.wayfinder.config.SQLiteDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        props.setProperty("hibernate.show_sql", "true");
        return props;
    }
}
