package org.opennms.core.schema;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import liquibase.CompositeFileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.commandline.CommandLineFileOpener;
import liquibase.commandline.CommandLineUtils;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;

public class Migrator {

    public void migrate(Migration migration) throws MigrationException {
        Database db;
        try {
            db = CommandLineUtils.createDatabaseObject(
                getMigrationClassLoader(migration),
                migration.getJdbcUrl(),
                migration.getAdminUser(),
                migration.getAdminPassword(),
                migration.getJdbcDriver(),
                "public",
                null);
        } catch (JDBCException e) {
            throw new MigrationException("unable to get database implementation for migration " + migration, e);
        }

        if (db != null) {
            FileSystemFileOpener fsOpener = new FileSystemFileOpener();
            CommandLineFileOpener clOpener = new CommandLineFileOpener(getMigrationClassLoader(migration));
            CompositeFileOpener fileOpener = new CompositeFileOpener(fsOpener, clOpener);
            Liquibase liquibase = new Liquibase(migration.getChangeLog(), fileOpener, db);
            try {
                liquibase.update(null);
            } catch (LiquibaseException e) {
                throw new MigrationException("unable to update the database", e);
            }
        }
        
    }
    
    protected ClassLoader getMigrationClassLoader(Migration migration) {
        File changeLog = new File(migration.getChangeLog());
        List<URL> urls = new ArrayList<URL>();
        try {
            urls.add(changeLog.getParentFile().toURL());
        } catch (MalformedURLException e) {
            log().warn("unable to figure out URL for " + migration.getChangeLog(), e);
        }
        return new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
    }

    private Category log() {
        return ThreadCategory.getInstance(Migrator.class);
    }

}
