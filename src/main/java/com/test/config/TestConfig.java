package com.test.config;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TestConfig {

    @Bean(name = { "defaultEntityManagerFactory", "entityManagerFactory" })
    protected LocalContainerEntityManagerFactoryBean defaultEntityManagerFactory() throws PropertyVetoException, IOException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(defaultDataSource());
        entityManagerFactory.setJpaVendorAdapter(defaultJpaVendorAdapter());
        entityManagerFactory.setPackagesToScan("com.db.model");
        entityManagerFactory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
        entityManagerFactory.setJpaProperties(defaultJpaProperties());

        return entityManagerFactory;
    }
    
    @Bean(name = { "defaultJpaVendorAdapter", "jpaVendorAdapter" })
    protected JpaVendorAdapter defaultJpaVendorAdapter() {
        final HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(false);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.HSQL);

        return hibernateJpaVendorAdapter;
    }
    
    @Bean(name = { "defaultDataSource", "dataSource" })
    public DataSource defaultDataSource() {
        final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.HSQL).build();
    }
    
    @Bean(name = { "defaultTransactionManager", "transactionManager" })
    protected JpaTransactionManager defaultTransactionManager() throws PropertyVetoException {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(null);
        transactionManager.setDataSource(defaultDataSource());

        return transactionManager;
    }
    
    @Bean(name = { "defaultJpaProperties", "jpaProperties" })
    protected Properties defaultJpaProperties() throws IOException {

        final Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.import_files", "/META-INF/import.sql");
        jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("connection.autocommit", "true");
        
        return jpaProperties;
    }

}
