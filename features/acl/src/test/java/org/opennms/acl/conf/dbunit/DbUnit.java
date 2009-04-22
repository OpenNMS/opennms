package org.opennms.acl.conf.dbunit;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DBTestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.opennms.acl.conf.Config;

public abstract class DbUnit extends DBTestCase {

    public DbUnit() {
    }

    public static IDatabaseConnection setUpConnection() {
        System.out.println("DbUnit setUpConnection");
        Config config = new Config();
        if (dbConn == null) {
            Connection jdbcConnection = null;

            try {
                Class.forName(config.getDbDriver());
                jdbcConnection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPass());
                dbConn = new DatabaseConnection(jdbcConnection);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dbConn;

    }

    public IDatabaseConnection getConnection() throws Exception {

        if (null == dbConn) {
            dbConn = setUpConnection();
        }
        return dbConn;
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        Class.forName(config.getDbDriver());
        Connection jdbcConnection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPass());
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection, "public");

        // dtd
        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("src/test/java/it/pronetics/acl/conf/dbunit/database-schema.dtd"));

        // export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("src/test/java/opennms/opennms/acl/conf/dbunit/full.xml"));
    }

    public void prepareDb() {
        try {
            try {
                DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
            } finally {
                // connection.close();
            }
        } catch (DatabaseUnitException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ++operation;
    }

    public void cleanDb() {
        try {

            try {
                DatabaseOperation.DELETE_ALL.execute(getConnection(), getDataSet());
            } finally {
                // connection.close();
            }
        } catch (Exception e) {
            /* do we care?
            System.err.print("An error occurred deleting dataset: ");
            e.printStackTrace();
            */
        }

        ++operation;

    }

    public static void closeConnection() {
        System.out.println("DbUnit closeConnection");
        if (dbConn != null) {
            try {
                dbConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                dbConn = null;
                System.gc();
            }
        }
        System.out.println("Total DbUnit Operation:" + operation);
    }

    private static IDatabaseConnection dbConn = setUpConnection();
    private static int operation;
}
