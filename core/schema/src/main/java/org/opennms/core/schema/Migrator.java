package org.opennms.core.schema;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

public class Migrator {

    @Autowired
    DataSource m_dataSource;

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void migrate(Migration migration) throws MigrationException {
        Connection connection;
        Database database;

        SpringFileOpener sfo = new SpringFileOpener();
        sfo.setResourceLoader(getMigrationResourceLoader(migration));

        try {
            connection = m_dataSource.getConnection();
        } catch (Exception e) {
            throw new MigrationException("unable to get a database connection from the datasource", e);
        }

        try {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        } catch (Exception e) {
            safeCloseConnection(connection);
            throw new MigrationException("unable to determine the Liquibase database object", e);
        }

        Liquibase liquibase = new Liquibase( migration.getChangeLog(), sfo, database );
        liquibase.setChangeLogParameterValue("install.database.admin.user", migration.getAdminUser());
        liquibase.setChangeLogParameterValue("install.database.admin.password", migration.getAdminPassword());
        liquibase.setChangeLogParameterValue("install.database.user", migration.getDatabaseUser());

        try {
            liquibase.update("");
        } catch (Exception e) {
            safeCloseConnection(connection);
            throw new MigrationException("unable to update the database", e);
        }

        safeCloseConnection(connection);
    }

    private void safeCloseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log().warn("unable to close migration connection", e);
            }
        }
    }

    protected ResourceLoader getMigrationResourceLoader(Migration migration) {
        File changeLog = new File(migration.getChangeLog());
        List<URL> urls = new ArrayList<URL>();
        try {
            if (changeLog.exists()) {
                urls.add(changeLog.getParentFile().toURL());
            } else {
                log().warn("file " + migration.getChangeLog() + " does not exist");
            }
        } catch (MalformedURLException e) {
            log().warn("unable to figure out URL for " + migration.getChangeLog(), e);
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        return new DefaultResourceLoader(cl);
    }

    private Category log() {
        return ThreadCategory.getInstance(Migrator.class);
    }

}
